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

import io.vertx.ext.neo4j.Neo4jClient
import io.vertx.ext.neo4j.Neo4jRecordStream
import io.vertx.ext.neo4j.Neo4jTransaction
import io.vertx.kotlin.coroutines.awaitResult
import org.neo4j.driver.Query
import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.neo4j.driver.summary.ResultSummary
import org.neo4j.driver.summary.SummaryCounters

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.begin]
 *
 * @return [Neo4jTransaction]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.beginAwait(): Neo4jTransaction {
  return awaitResult {
    this.begin(it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.queryStream]
 *
 * @param query the cypher query
 * @return [Neo4jRecordStream]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.queryStreamAwait(query: String): Neo4jRecordStream {
  return awaitResult {
    this.queryStream(query, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.execute]
 *
 * @param query the cypher query
 * @return [ResultSummary]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.executeAwait(query: String): ResultSummary {
  return awaitResult {
    this.execute(query, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.execute]
 *
 * @param query the cypher query
 * @param parameters the cypher parameters
 * @return [ResultSummary]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.executeAwait(query: String, parameters: Value): ResultSummary {
  return awaitResult {
    this.execute(query, parameters, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.findOne]
 *
 * @param query the cypher query
 * @return [Record]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.findOneAwait(query: String): Record {
  return awaitResult {
    this.findOne(query, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.findOne]
 *
 * @param query the cypher query
 * @param parameters the cypher parameters
 * @return [Record]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.findOneAwait(query: String, parameters: Value): Record {
  return awaitResult {
    this.findOne(query, parameters, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.find]
 *
 * @param query the cypher query
 * @return [List<Record>]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.findAwait(query: String): List<Record> {
  return awaitResult {
    this.find(query, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.find]
 *
 * @param query the cypher query
 * @param parameters the cypher parameters
 * @return [List<Record>]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.findAwait(query: String, parameters: Value): List<Record> {
  return awaitResult {
    this.find(query, parameters, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.bulkWrite]
 *
 * @param queries the list of queries to execute
 * @return [SummaryCounters]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.bulkWriteAwait(queries: List<Query>): SummaryCounters {
  return awaitResult {
    this.bulkWrite(queries, it)
  }
}

/**
 * Suspending version of method [io.vertx.ext.neo4j.Neo4jClient.queryStream]
 *
 * @param query the cypher query
 * @param parameters the cypher parameters
 * @return [Neo4jRecordStream]
 *
 * NOTE: This function has been automatically generated from [io.vertx.ext.neo4j.Neo4jClient] using Vert.x codegen.
 */
suspend fun Neo4jClient.queryStreamAwait(query: String, parameters: Value): Neo4jRecordStream {
  return awaitResult {
    this.queryStream(query, parameters, it)
  }
}

