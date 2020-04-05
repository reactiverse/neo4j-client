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

import io.reactiverse.neo4j.Neo4jTransaction;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.async.AsyncTransaction;
import org.neo4j.driver.async.ResultCursor;
import org.neo4j.driver.summary.ResultSummary;

import java.util.List;

import static io.reactiverse.neo4j.Util.fromCompletionStage;
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
}
