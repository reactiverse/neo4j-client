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
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.util.List;

/**
 * Eclipse Vert.x Neo4j client.
 *
 */
@VertxGen
public interface Neo4jTransaction {

    /**
     * Executes a query in a transaction
     *
     * @param query  the cypher query
     * @param parameters  the cypher parameters
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jTransaction instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jTransaction query(String query, Value parameters, Handler<AsyncResult<ResultSummary>> resultHandler);

    /**
     * Executes a query in a transaction
     *
     * @param statement  the cypher statement
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jTransaction instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jTransaction query(Statement statement, Handler<AsyncResult<ResultSummary>> resultHandler);

    /**
     * Executes a read query in a transaction
     *
     * @param statement  the cypher statement
     * @param resultHandler  the handler to be called when the query has completed
     * @return the current Neo4jTransaction instance
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Fluent
    Neo4jTransaction readQuery(Statement statement, Handler<AsyncResult<List<Record>>> resultHandler);

    /**
     * Commits the transaction
     *
     * @param resultHandler  the handler to be notified when the transaction commit has completed
     * @return the current Neo4jTransaction instance
     */
    @Fluent
    Neo4jTransaction commit(Handler<AsyncResult<Void>> resultHandler);

    /**
     * Rollbacks the transaction
     *
     * @param resultHandler  the handler to be notified when the transaction rollback has completed
     * @return the current Neo4jTransaction instance
     */
    @Fluent
    Neo4jTransaction rollback(Handler<AsyncResult<Void>> resultHandler);
}
