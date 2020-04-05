/*
 * Copyright (c) 2018-2020 Olympe S.A.
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

import io.reactiverse.neo4j.Neo4jClient;
import io.reactiverse.neo4j.Neo4jRecordStream;
import io.reactiverse.neo4j.Neo4jTransaction;
import io.reactiverse.neo4j.VisibleForTesting;
import io.reactiverse.neo4j.options.Neo4jClientOptions;
import io.vertx.core.*;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import org.neo4j.driver.*;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.internal.summary.InternalSummaryCounters;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.reactiverse.neo4j.Util.wrapCallback;
import static java.util.Objects.requireNonNull;
import static org.neo4j.driver.AccessMode.READ;
import static org.neo4j.driver.AccessMode.WRITE;
import static org.neo4j.driver.internal.summary.InternalSummaryCounters.EMPTY_STATS;

public class Neo4jClientImpl implements Neo4jClient {

    private static final String NEO4J_CLIENT_MAP_NAME = "__vertx.Neo4jClient.datasources";
    private static final SessionConfig DEFAULT_WRITE_SESSION_CONFIG = SessionConfig.builder().withDefaultAccessMode(WRITE).build();
    private static final SessionConfig DEFAULT_READ_SESSION_CONFIG = SessionConfig.builder().withDefaultAccessMode(READ).build();

    private final Vertx vertx;

    private Neo4jHolder neo4jHolder;

    private Driver driver;

    private static final Value EMPTY = Values.parameters();

    public Neo4jClientImpl(Vertx vertx, Neo4jClientOptions config, String dataSourceName) {
        requireNonNull(vertx);
        requireNonNull(config);
        requireNonNull(dataSourceName);

        this.vertx = vertx;
        this.neo4jHolder = lookupHolder(config, dataSourceName);
        this.driver = this.neo4jHolder.neo4jDriver();
    }

    @Override
    public Neo4jClient execute(String query, Handler<AsyncResult<ResultSummary>> resultHandler) {
        execute(query, EMPTY, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient execute(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler) {
        executeWriteTransaction(query, parameters, ResultCursor::consumeAsync, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient delete(String query, Handler<AsyncResult<List<Record>>> resultHandler) {
        return delete(query, EMPTY, resultHandler);
    }

    @Override
    public Neo4jClient delete(String query, Value parameters, Handler<AsyncResult<List<Record>>> resultHandler) {
        executeWriteTransaction(query, parameters, ResultCursor::listAsync, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient findOne(String query, Handler<AsyncResult<Record>> resultHandler) {
        findOne(query, EMPTY, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient findOne(String query, Value parameters, Handler<AsyncResult<Record>> resultHandler) {
        executeReadTransaction(query, parameters, ResultCursor::singleAsync, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient find(String query, Handler<AsyncResult<List<Record>>> resultHandler) {
        find(query, EMPTY, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient find(String query, Value parameters, Handler<AsyncResult<List<Record>>> resultHandler) {
        executeReadTransaction(query, parameters, ResultCursor::listAsync, resultHandler);
        return this;
    }

    @Override
    public Neo4jClient bulkWrite(List<Query> queries, Handler<AsyncResult<SummaryCounters>> resultHandler) {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        session.writeTransactionAsync(tx -> {
            CompletionStage<SummaryCounters> stage = CompletableFuture.completedFuture(EMPTY_STATS);

            for (Query query : queries) {
                stage = stage.thenCompose(previousCounter -> tx.runAsync(query)
                        .thenCompose(ResultCursor::consumeAsync)
                        .thenApply(ResultSummary::counters)
                        .thenApply(nextCounter -> AGGREGATE_COUNTERS.apply(previousCounter, nextCounter)));
            }

            return stage;
        })
        .whenComplete(wrapCallback(context, resultHandler))
        .thenCompose(ignore -> session.closeAsync());
        return this;
    }

    @Override
    public Neo4jClient begin(Handler<AsyncResult<Neo4jTransaction>> resultHandler) {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        session.beginTransactionAsync().thenAccept(tx -> {
            context.runOnContext(v -> resultHandler.handle(Future.succeededFuture(new Neo4jTransactionImpl(vertx, tx, session))));
        }).exceptionally(error -> {
            context.runOnContext(v -> resultHandler.handle(Future.failedFuture(error)));
            session.closeAsync();
            return null;
        });
        return this;
    }

    @Override
    public Neo4jClient queryStream(String query, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler) {
        return queryStream(query, EMPTY, recordStreamHandler);
    }

    @Override
    public Neo4jClient queryStream(String query, Value parameters, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler) {
        AsyncSession session = driver.asyncSession(DEFAULT_READ_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        session.beginTransactionAsync().thenAccept(tx -> tx.runAsync(query, parameters).thenAccept(cursor -> {
            context.runOnContext(v -> recordStreamHandler.handle(Future.succeededFuture(new Neo4jRecordStreamImpl(context, tx, session, new ResultCursorImpl(cursor, vertx)))));
        }))
        .exceptionally(error -> {
            context.runOnContext(v -> recordStreamHandler.handle(Future.failedFuture(error)));
            session.closeAsync();
            return null;
        });
        return this;

    }

    private <T> void executeWriteTransaction(String query, Value parameters, Function<ResultCursor, CompletionStage<T>> resultFunction, Handler<AsyncResult<T>> resultHandler) {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        session.writeTransactionAsync(tx -> tx.runAsync(query, parameters)
                .thenCompose(resultFunction))
                .whenComplete(wrapCallback(context, resultHandler))
                .thenCompose(ignore -> session.closeAsync());
    }

    private <T> void executeReadTransaction(String query, Value parameters, Function<ResultCursor, CompletionStage<T>> resultFunction, Handler<AsyncResult<T>> resultHandler) {
        AsyncSession session = driver.asyncSession(DEFAULT_READ_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        session.readTransactionAsync(tx -> tx.runAsync(query, parameters)
                .thenCompose(resultFunction))
                .whenComplete(wrapCallback(context, resultHandler))
                .thenCompose(ignore -> session.closeAsync());
    }

    @VisibleForTesting
    static final BinaryOperator<SummaryCounters> AGGREGATE_COUNTERS = (summaryCounters, summaryCounters2) -> new InternalSummaryCounters(
            summaryCounters.nodesCreated() + summaryCounters2.nodesCreated(),
            summaryCounters.nodesDeleted() + summaryCounters2.nodesDeleted(),
            summaryCounters.relationshipsCreated() + summaryCounters2.relationshipsCreated(),
            summaryCounters.relationshipsDeleted() + summaryCounters2.relationshipsDeleted(),
            summaryCounters.propertiesSet() + summaryCounters2.propertiesSet(),
            summaryCounters.labelsAdded() + summaryCounters2.labelsAdded(),
            summaryCounters.labelsRemoved() + summaryCounters2.labelsRemoved(),
            summaryCounters.indexesAdded() + summaryCounters2.indexesAdded(),
            summaryCounters.indexesRemoved() + summaryCounters2.indexesRemoved(),
            summaryCounters.constraintsAdded() + summaryCounters2.constraintsAdded(),
            summaryCounters.constraintsRemoved() + summaryCounters2.constraintsRemoved(),
            summaryCounters.systemUpdates() + summaryCounters2.systemUpdates()
    );

    @Override
    public void close() {
        this.neo4jHolder.close();
    }

    private static class Neo4jHolder implements Shareable {
        Driver driver;
        Neo4jClientOptions config;
        Runnable closeRunner;
        int refCount = 1;

        Neo4jHolder(Neo4jClientOptions config, Runnable closeRunner) {
            this.config = config;
            this.closeRunner = closeRunner;
        }

        synchronized Driver neo4jDriver() {
            if (driver == null) {
                Supplier<Driver> driverSupplier = new DriverSupplier(config);
                Driver givenDriver = driverSupplier.get();
                givenDriver.verifyConnectivity();
                this.driver = givenDriver;
            }

            return driver;
        }

        synchronized void incRefCount() {
            refCount++;
        }

        synchronized void close() {
            if (--refCount == 0) {
                if (driver != null) {
                    driver.close();
                }
                if (closeRunner != null) {
                    closeRunner.run();
                }
            }
        }
    }

    private Neo4jHolder lookupHolder(Neo4jClientOptions config, String dataSourceName) {
        synchronized (vertx) {
            LocalMap<String, Neo4jHolder> map = vertx.sharedData().getLocalMap(NEO4J_CLIENT_MAP_NAME);
            Neo4jHolder theHolder = map.get(dataSourceName);
            if (theHolder == null) {
                theHolder = new Neo4jHolder(config, () -> removeFromMap(map, dataSourceName));
                map.put(dataSourceName, theHolder);
            } else {
                theHolder.incRefCount();
            }
            return theHolder;
        }
    }

    private void removeFromMap(LocalMap<String, Neo4jHolder> map, String dataSourceName) {
        synchronized (vertx) {
            map.remove(dataSourceName);
            if (map.isEmpty()) {
                map.close();
            }
        }
    }

}
