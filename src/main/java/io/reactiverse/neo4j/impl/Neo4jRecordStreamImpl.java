/*
 * Copyright 2019 Olympe SA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactiverse.neo4j.impl;

import io.reactiverse.neo4j.Neo4jRecordStream;
import io.reactiverse.neo4j.ResultCursor;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.impl.InboundBuffer;
import org.neo4j.driver.Record;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;

public class Neo4jRecordStreamImpl implements Neo4jRecordStream {

    private enum State {
        IDLE, STARTED, EXHAUSTED, STOPPED
    }

    private final Context context;
    private final AsyncTransaction tx;
    private final AsyncSession session;
    private final ResultCursor cursor;
    private final InboundBuffer<Record> internalQueue;

    private State state;
    private int inFlight;
    private Handler<Record> handler;
    private Handler<Throwable> exceptionHandler;
    private Handler<Void> endHandler;

    public Neo4jRecordStreamImpl(Context context, AsyncTransaction tx, AsyncSession session, ResultCursor cursor) {
        this.context = context;
        this.tx = tx;
        this.session = session;
        this.cursor = cursor;
        internalQueue = new InboundBuffer<Record>(context)
                .exceptionHandler(this::handleException)
                .drainHandler(v -> fetchRecord());
        state = State.IDLE;
    }

    @Override
    public synchronized Neo4jRecordStream exceptionHandler(Handler<Throwable> handler) {
        if (state != State.STOPPED) {
            exceptionHandler = handler;
        }
        return this;
    }

    @Override
    public synchronized Neo4jRecordStream handler(Handler<Record> handler) {
        if (state == State.STOPPED) {
            return this;
        }
        if (handler == null) {
            stop();
            if (context != Vertx.currentContext()) {
                context.runOnContext(v -> handleEnd());
            } else {
                handleEnd();
            }
        } else {
            this.handler = handler;
            internalQueue.handler(this::handleRecord);
            if (state == State.IDLE) {
                state = State.STARTED;
                if (context != Vertx.currentContext()) {
                    context.runOnContext(v -> fetchRecord());
                } else {
                    fetchRecord();
                }
            }
        }
        return this;
    }

    @Override
    public synchronized Neo4jRecordStream pause() {
        if (state != State.STOPPED) {
            internalQueue.pause();
        }
        return this;
    }

    @Override
    public synchronized Neo4jRecordStream resume() {
        if (state != State.STOPPED) {
            internalQueue.resume();
        }
        return this;
    }

    @Override
    public synchronized Neo4jRecordStream endHandler(Handler<Void> handler) {
        if (state != State.STOPPED) {
            endHandler = handler;
        }
        return this;
    }

    @Override
    public Neo4jRecordStream fetch(long l) {
        if (state != State.STOPPED) {
            internalQueue.fetch(l);
        }
        return this;
    }

    private synchronized void fetchRecord() {
        if (state == State.STOPPED) {
            return;
        }

        cursor.one(ar -> {
            if (ar.succeeded()) {
                handleFetched(ar.result());
            } else {
                handleException(ar.cause());
            }
        });
    }

    private synchronized void handleFetched(Record record) {
        if (state == State.STOPPED) {
            return;
        }
        if (record != null) {
            inFlight++;
            if (internalQueue.write(record)) {
                fetchRecord();
            }
        } else {
            state = State.EXHAUSTED;
            if (inFlight == 0) {
                stop();
                handleEnd();
            }
        }
    }

    private void handleRecord(Record record) {
        synchronized (this) {
            if (state == State.STOPPED) {
                return;
            }
            inFlight--;
        }
        handler.handle(record);
        synchronized (this) {
            if (state == State.EXHAUSTED && inFlight == 0) {
                stop();
                handleEnd();
            }
        }
    }

    private void handleException(Throwable cause) {
        Handler<Throwable> h;
        synchronized (this) {
            if (state != State.STOPPED) {
                stop();
                h = exceptionHandler;
            } else {
                h = null;
            }
        }
        if (h != null) {
            h.handle(cause);
        }
    }

    private synchronized void handleEnd() {
        Handler<Void> h;
        synchronized (this) {
            h = endHandler;
        }
        if (h != null) {
            h.handle(null);
        }
    }

    private synchronized void stop() {
        state = State.STOPPED;
        internalQueue.handler(null).drainHandler(null);
        tx.commitAsync().thenCompose(ignore -> session.closeAsync());
    }
}
