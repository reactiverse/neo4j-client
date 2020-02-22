/*
 * Copyright 2019 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.kotlin.ext.neo4j

import io.vertx.ext.neo4j.Neo4jTransaction
import io.vertx.kotlin.coroutines.awaitResult
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.neo4j.driver.summary.ResultSummary

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jTransaction.commit]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jTransaction] using Vert.x codegen.
 */
suspend fun Neo4jTransaction.commitAwait(): Unit {
  return awaitResult {
    this.commit(io.vertx.core.Handler { ar -> it.handle(ar.mapEmpty()) })
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jTransaction.rollback]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jTransaction] using Vert.x codegen.
 */
suspend fun Neo4jTransaction.rollbackAwait(): Unit {
  return awaitResult {
    this.rollback(io.vertx.core.Handler { ar -> it.handle(ar.mapEmpty()) })
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jTransaction.query]
 *
 * @param query the cypher query
 * @param parameters the cypher parameters
 * @return [ResultSummary]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jTransaction] using Vert.x codegen.
 */
suspend fun Neo4jTransaction.queryAwait(query: String, parameters: Value): ResultSummary {
  return awaitResult {
    this.query(query, parameters, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jTransaction.query]
 *
 * @param query the cypher statement
 * @return [ResultSummary]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jTransaction] using Vert.x codegen.
 */
suspend fun Neo4jTransaction.queryAwait(query: Query): ResultSummary {
  return awaitResult {
    this.query(query, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jTransaction.readQuery]
 *
 * @param query the cypher statement
 * @return [List<Record>]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jTransaction] using Vert.x codegen.
 */
suspend fun Neo4jTransaction.readQueryAwait(query: Query): List<Record> {
  return awaitResult {
    this.readQuery(query, it)
  }
}

