/*
 * Copyright 2020 Olympe SA.
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
import io.vertx.core.impl.ContextInternal;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ResultSummary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.reactiverse.neo4j.Util.setHandler;

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
        Future<ResultSummary> resultSummaryFuture = query(query, parameters);
        setHandler(resultSummaryFuture, resultHandler);
        return this;
    }

    @Override
    public Future<ResultSummary> query(String query, Value parameters) {
        return fromCompletionStage(tx.runAsync(query, parameters).thenCompose(ResultCursor::consumeAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jTransaction query(Query query, Handler<AsyncResult<ResultSummary>> resultHandler) {
        Future<ResultSummary> resultSummaryFuture = query(query);
        setHandler(resultSummaryFuture, resultHandler);
        return this;
    }

    @Override
    public Future<ResultSummary> query(Query query) {
        return fromCompletionStage(tx.runAsync(query).thenCompose(ResultCursor::consumeAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jTransaction readQuery(Query query, Handler<AsyncResult<List<Record>>> resultHandler) {
        Future<List<Record>> listFuture = readQuery(query);
        setHandler(listFuture, resultHandler);
        return this;
    }

    @Override
    public Future<List<Record>> readQuery(Query query) {
        return fromCompletionStage(tx.runAsync(query).thenCompose(ResultCursor::listAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jTransaction commit(Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> commitFuture = commit();
        setHandler(commitFuture, resultHandler);
        return this;
    }

    @Override
    public Future<Void> commit() {
        return Future.fromCompletionStage(tx.commitAsync().whenComplete((ignore, error) -> session.closeAsync()), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jTransaction rollback(Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> rollbackFuture = rollback();
        setHandler(rollbackFuture, resultHandler);
        return this;
    }

    @Override
    public Future<Void> rollback() {
        return Future.fromCompletionStage(tx.rollbackAsync().whenComplete((ignore, error) -> session.closeAsync()), vertx.getOrCreateContext());
    }

    static <T> Future<T> fromCompletionStage(CompletionStage<T> completionStage, Context context) {
        Promise<T> promise = ((ContextInternal) context).promise();
        completionStage.whenComplete((value, err) -> {
            if (err != null) {
                promise.fail(Optional.ofNullable(err.getCause()).orElse(err));
            } else {
                promise.complete(value);
            }
        });
        return promise.future();
    }
}
