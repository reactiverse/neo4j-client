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

import io.reactiverse.neo4j.Neo4jTransaction;
import io.vertx.core.*;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ResultSummary;

import java.util.List;
import java.util.Optional;

import static io.reactiverse.neo4j.Util.wrapCallback;

public class Neo4jTransactionImpl implements Neo4jTransaction {

    private final Vertx vertx;
    private final AsyncTransaction tx;
    private final AsyncSession session;

    public Neo4jTransactionImpl(Vertx vertx, AsyncTransaction tx, AsyncSession session) {
        this.vertx = vertx;
        this.tx = tx;
        this.session = session;
    }

    @Override
    public Neo4jTransaction query(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler) {
        query(new Query(query, parameters), resultHandler);
        return this;
    }

    @Override
    public Neo4jTransaction query(Query query, Handler<AsyncResult<ResultSummary>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.runAsync(query).thenCompose(ResultCursor::consumeAsync)
        .thenAccept(resultSummary -> context.runOnContext(v -> resultHandler.handle(Future.succeededFuture(resultSummary))))
        .exceptionally(error -> {
            context.runOnContext(v -> resultHandler.handle(Future.failedFuture(Optional.ofNullable(error.getCause()).orElse(error))));
            return null;
        });
        return this;
    }

    @Override
    public Neo4jTransaction readQuery(Query query, Handler<AsyncResult<List<Record>>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.runAsync(query).thenCompose(ResultCursor::listAsync)
        .thenAccept(records -> context.runOnContext(v -> resultHandler.handle(Future.succeededFuture(records))))
        .exceptionally(error -> {
            context.runOnContext(v -> resultHandler.handle(Future.failedFuture(Optional.ofNullable(error.getCause()).orElse(error))));
            return null;
        });
        return this;
    }

    @Override
    public Neo4jTransaction commit(Handler<AsyncResult<Void>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.commitAsync().whenComplete(wrapCallback(context, resultHandler))
            .thenCompose(ignore -> session.closeAsync());
        return this;
    }

    @Override
    public Neo4jTransaction rollback(Handler<AsyncResult<Void>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.rollbackAsync().whenComplete(wrapCallback(context, resultHandler))
        .thenCompose(ignore -> session.closeAsync());
        return this;
    }
}
