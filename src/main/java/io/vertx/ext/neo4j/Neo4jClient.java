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

package io.vertx.ext.neo4j;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.neo4j.impl.Neo4jClientImpl;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;

import java.util.List;
import java.util.UUID;

@VertxGen
public interface Neo4jClient {

    /**
     * The default shared client name.
     */
    String DEFAULT_POOL_NAME = "DEFAULT_POOL";

    /**
     * Creates a Neo4j client that shares its driver session with any other client having the same name.
     *
     * @param vertx  the Vert.x instance
     * @param config  the driver configuration
     *
     * @return the client
     */
    static Neo4jClient createShared(Vertx vertx, JsonObject config) {
        return new Neo4jClientImpl(vertx, config, DEFAULT_POOL_NAME);
    }

    /**
     * Creates a Neo4j client which maintains its own driver session.
     * <p>
     * It is not recommended to create several non shared clients in an application.
     *
     * @param vertx   the Vert.x instance
     * @param config  the driver configuration
     * @return the client
     */
    static Neo4jClient createNonShared(Vertx vertx, JsonObject config) {
        return new Neo4jClientImpl(vertx, config, UUID.randomUUID().toString());
    }

    /**
     * Executes a write transaction
     *
     * @param query  the cypher query
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient execute(String query, Handler<AsyncResult<ResultSummary>> resultHandler);

    /**
     * Executes a write transaction
     *
     * @param query  the cypher query
     * @param parameters  the cypher parameters
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient execute(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler);

    /**
     * Finds exactly one record
     *
     * @param query  the cypher query
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient findOne(String query, Handler<AsyncResult<Record>> resultHandler);

    /**
     * Finds exactly one record
     *
     * @param query  the cypher query
     * @param parameters  the cypher parameters
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient findOne(String query, Value parameters, Handler<AsyncResult<Record>> resultHandler);

    /**
     * Finds a list of records
     *
     * @param query  the cypher query
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient find(String query, Handler<AsyncResult<List<Record>>> resultHandler);

    /**
     * Finds a list of records
     *
     * @param query  the cypher query
     * @param parameters  the cypher parameters
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient find(String query, Value parameters, Handler<AsyncResult<List<Record>>> resultHandler);

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient bulkWrite(List<Statement> statements, Handler<AsyncResult<SummaryCounters>> resultHandler);

    /**
     * Begins a new transaction
     *
     * @param resultHandler  the handler to be called when the transaction has begun
     * @return the current Neo4jClient instance
     */
    @Fluent
    Neo4jClient begin(Handler<AsyncResult<io.vertx.ext.neo4j.Neo4jTransaction>> resultHandler);

    /**
     * Opens a new stream of records
     *
     * @param query  the cypher query
     * @param recordStreamHandler  the handler to be called when the stream of records is available
     * @return the current Neo4jClient instance
     */
    @Fluent
    Neo4jClient queryStream(String query, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler);

    /**
     * Opens a new stream of records
     *
     * @param query  the cypher query
     * @param parameters  the cypher parameters
     * @param recordStreamHandler  the handler to be called when the stream of records is available
     * @return the current Neo4jClient instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jClient queryStream(String query, Value parameters, Handler<AsyncResult<Neo4jRecordStream>> recordStreamHandler);

    /**
     * Closes this client
     */
    void close();
}
