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

import static io.reactiverse.neo4j.Util.fromCompletionStage;
import static io.reactiverse.neo4j.Util.setHandler;
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
        return execute(query, EMPTY, resultHandler);
    }

    @Override
    public Future<ResultSummary> execute(String query) {
        return execute(query, EMPTY);
    }

    @Override
    public Neo4jClient execute(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler) {
        Future<ResultSummary> resultSummaryFuture = execute(query, parameters);
        setHandler(resultSummaryFuture, resultHandler);
        return this;
    }

    @Override
    public Future<ResultSummary> execute(String query, Value parameters) {
        return fromCompletionStage(executeWriteTransaction(query, parameters, ResultCursor::consumeAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient delete(String query, Handler<AsyncResult<List<Record>>> resultHandler) {
        return delete(query, EMPTY, resultHandler);
    }

    @Override
    public Future<List<Record>> delete(String query) {
        return delete(query, EMPTY);
    }

    @Override
    public Neo4jClient delete(String query, Value parameters, Handler<AsyncResult<List<Record>>> resultHandler) {
        Future<List<Record>> resultRecordFuture = delete(query, parameters);
        setHandler(resultRecordFuture, resultHandler);
        return this;
    }

    @Override
    public Future<List<Record>> delete(String query, Value parameters) {
        return fromCompletionStage(executeWriteTransaction(query, parameters, ResultCursor::listAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient findOne(String query, Handler<AsyncResult<Record>> resultHandler) {
        return findOne(query, EMPTY, resultHandler);
    }

    @Override
    public Future<Record> findOne(String query) {
        return findOne(query, EMPTY);
    }

    @Override
    public Neo4jClient findOne(String query, Value parameters, Handler<AsyncResult<Record>> resultHandler) {
        Future<Record> recordFuture = findOne(query, parameters);
        setHandler(recordFuture, resultHandler);
        return this;
    }

    @Override
    public Future<Record> findOne(String query, Value parameters) {
        return fromCompletionStage(executeReadTransaction(query, parameters, ResultCursor::singleAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient find(String query, Handler<AsyncResult<List<Record>>> resultHandler) {
        return find(query, EMPTY, resultHandler);
    }

    @Override
    public Future<List<Record>> find(String query) {
        return find(query, EMPTY);
    }

    @Override
    public Neo4jClient find(String query, Value parameters, Handler<AsyncResult<List<Record>>> resultHandler) {
        Future<List<Record>> listFuture = find(query, parameters);
        setHandler(listFuture, resultHandler);
        return this;
    }

    @Override
    public Future<List<Record>> find(String query, Value parameters) {
        return fromCompletionStage(executeReadTransaction(query, parameters, ResultCursor::listAsync), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient bulkWrite(List<Query> queries, Handler<AsyncResult<SummaryCounters>> resultHandler) {
        Future<SummaryCounters> summaryCountersFuture = bulkWrite(queries);
        setHandler(summaryCountersFuture, resultHandler);
        return this;
    }

    @Override
    public Future<SummaryCounters> bulkWrite(List<Query> queries) {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        return fromCompletionStage(session.writeTransactionAsync(tx -> {
            CompletionStage<SummaryCounters> stage = CompletableFuture.completedFuture(EMPTY_STATS);

            for (Query query : queries) {
                stage = stage.thenCompose(previousCounter -> tx.runAsync(query)
                        .thenCompose(ResultCursor::consumeAsync)
                        .thenApply(ResultSummary::counters)
                        .thenApply(nextCounter -> AGGREGATE_COUNTERS.apply(previousCounter, nextCounter)));
            }

            return stage;
        })
        .whenComplete((ignore, error) -> session.closeAsync()), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient begin(Handler<AsyncResult<Neo4jTransaction>> resultHandler) {
        Future<Neo4jTransaction> beginFuture = begin();
        setHandler(beginFuture, resultHandler);
        return this;
    }

    @Override
    public Future<Neo4jTransaction> begin() {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        return fromCompletionStage(session.beginTransactionAsync()
                .<Neo4jTransaction>thenApply(tx -> new Neo4jTransactionImpl(vertx, tx, session))
                .exceptionally(error -> {
                    session.closeAsync();
                    return null;
                }), vertx.getOrCreateContext());
    }

    @Override
    public Neo4jClient queryStream(String query, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler) {
        return queryStream(query, EMPTY, recordStreamHandler);
    }

    @Override
    public Future<Neo4jRecordStream> queryStream(String query) {
        return queryStream(query, EMPTY);
    }

    @Override
    public Neo4jClient queryStream(String query, Value parameters, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler) {
        Future<Neo4jRecordStream> neo4jRecordStreamFuture = queryStream(query, parameters);
        setHandler(neo4jRecordStreamFuture, recordStreamHandler);
        return this;
    }

    @Override
    public Future<Neo4jRecordStream> queryStream(String query, Value parameters) {
        AsyncSession session = driver.asyncSession(DEFAULT_READ_SESSION_CONFIG);
        Context context = vertx.getOrCreateContext();
        return fromCompletionStage(session.beginTransactionAsync().<Neo4jRecordStream>thenCompose(tx -> tx.runAsync(query, parameters).thenApply(cursor -> {
            return new Neo4jRecordStreamImpl(context, tx, session, new ResultCursorImpl(cursor, vertx));
        }))
        .exceptionally(error -> {
            session.closeAsync();
            return null;
        }), context);
    }

    private <T> CompletionStage<T> executeWriteTransaction(String query, Value parameters, Function<ResultCursor, CompletionStage<T>> resultFunction) {
        AsyncSession session = driver.asyncSession(DEFAULT_WRITE_SESSION_CONFIG);
        return session.writeTransactionAsync(tx -> tx.runAsync(query, parameters)
                .thenCompose(resultFunction))
                .whenComplete((ignore, error) -> session.closeAsync());
    }

    private <T> CompletionStage<T> executeReadTransaction(String query, Value parameters, Function<ResultCursor, CompletionStage<T>> resultFunction) {
        AsyncSession session = driver.asyncSession(DEFAULT_READ_SESSION_CONFIG);
        return session.readTransactionAsync(tx -> tx.runAsync(query, parameters)
                .thenCompose(resultFunction))
                .whenComplete((ignore, error) -> session.closeAsync());
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
