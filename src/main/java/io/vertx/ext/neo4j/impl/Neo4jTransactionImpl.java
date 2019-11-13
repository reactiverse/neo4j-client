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

package io.vertx.ext.neo4j.impl;

import io.vertx.core.*;
import io.vertx.ext.neo4j.Neo4jTransaction;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class Neo4jTransactionImpl implements Neo4jTransaction {

    private final Vertx vertx;
    private final Transaction tx;
    private final Session session;

    public Neo4jTransactionImpl(Vertx vertx, Transaction tx, Session session) {
        this.vertx = vertx;
        this.tx = tx;
        this.session = session;
    }

    @Override
    public Neo4jTransaction query(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler) {
        doQuery(tx.runAsync(query, parameters), resultHandler);
        return this;
    }

    @Override
    public Neo4jTransaction query(Statement statement, Handler<AsyncResult<ResultSummary>> resultHandler) {
        doQuery(tx.runAsync(statement), resultHandler);
        return this;
    }

    @Override
    public Neo4jTransaction readQuery(Statement statement, Handler<AsyncResult<List<Record>>> resultHandler) {
        doReadQuery(tx.runAsync(statement), resultHandler);
        return this;
    }

    @Override
    public Neo4jTransaction commit(Handler<AsyncResult<Void>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.commitAsync().whenComplete((result, error) -> {
                context.runOnContext(v -> {
                    if (error != null) {
                        resultHandler.handle(Future.failedFuture(error));
                    } else {
                        resultHandler.handle(Future.succeededFuture(result));
                    }
                });
            })
            .thenCompose(ignore -> session.closeAsync());
        return this;
    }

    @Override
    public Neo4jTransaction rollback(Handler<AsyncResult<Void>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        tx.rollbackAsync().whenComplete((result, error) -> {
            context.runOnContext(v -> {
                if (error != null) {
                    resultHandler.handle(Future.failedFuture(error));
                } else {
                    resultHandler.handle(Future.succeededFuture(result));
                }
            });
        })
        .thenCompose(ignore -> session.closeAsync());
        return this;
    }

    private CompletionStage<Void> doQuery(CompletionStage<StatementResultCursor> executeQuery, Handler<AsyncResult<ResultSummary>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        return executeQuery
                .thenCompose(StatementResultCursor::summaryAsync)
                .thenApply(summary -> {
                    context.runOnContext(v -> resultHandler.handle(Future.succeededFuture(summary)));
                    return false;
                })
                .exceptionally(error -> {
                    context.runOnContext(v -> resultHandler.handle(Future.failedFuture(Optional.ofNullable(error.getCause()).orElse(error)))); // cause of completion exception or itself
                    return true;
                })
                .thenCompose(rollback -> rollback ? tx.rollbackAsync().thenCompose(ignore -> session.closeAsync()) : completedFuture(null));
    }

    private CompletionStage<Void> doReadQuery(CompletionStage<StatementResultCursor> executeQuery, Handler<AsyncResult<List<Record>>> resultHandler) {
        Context context = vertx.getOrCreateContext();
        return executeQuery
                .thenCompose(StatementResultCursor::listAsync)
                .thenApply(records -> {
                    context.runOnContext(v -> resultHandler.handle(Future.succeededFuture(records)));
                    return false;
                })
                .exceptionally(error -> {
                    context.runOnContext(v -> resultHandler.handle(Future.failedFuture(Optional.ofNullable(error.getCause()).orElse(error)))); // cause of completion exception or itself
                    return true;
                }).thenCompose(ignore -> completedFuture(null));
    }
}
