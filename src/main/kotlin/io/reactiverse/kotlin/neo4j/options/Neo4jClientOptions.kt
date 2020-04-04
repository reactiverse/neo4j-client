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
package io.reactiverse.kotlin.neo4j.options

import io.reactiverse.neo4j.options.Neo4jClientOptions
import io.reactiverse.neo4j.options.Neo4jClientAuthOptions
import io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions

fun neo4jClientOptionsOf(
  authOptions: io.reactiverse.neo4j.options.Neo4jClientAuthOptions? = null,
  clusterNodeURIs: Iterable<String>? = null,
  connectionAcquisitionTimeoutMillis: Long? = null,
  encrypted: Boolean? = null,
  encryptionOptions: io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions? = null,
  eventLoopThreads: Int? = null,
  fetchSize: Long? = null,
  host: String? = null,
  idleTimeBeforeConnectionTest: Long? = null,
  logLeakedSessions: Boolean? = null,
  maxConnectionLifetimeMillis: Long? = null,
  maxConnectionPoolSize: Int? = null,
  metricsEnabled: Boolean? = null,
  port: Int? = null): Neo4jClientOptions = io.reactiverse.neo4j.options.Neo4jClientOptions().apply {

  if (authOptions != null) {
    this.setAuthOptions(authOptions)
  }
  if (clusterNodeURIs != null) {
    for (item in clusterNodeURIs) {
      this.addClusterNodeURI(item)
    }
  }
  if (connectionAcquisitionTimeoutMillis != null) {
    this.setConnectionAcquisitionTimeoutMillis(connectionAcquisitionTimeoutMillis)
  }
  if (encrypted != null) {
    this.setEncrypted(encrypted)
  }
  if (encryptionOptions != null) {
    this.setEncryptionOptions(encryptionOptions)
  }
  if (eventLoopThreads != null) {
    this.setEventLoopThreads(eventLoopThreads)
  }
  if (fetchSize != null) {
    this.setFetchSize(fetchSize)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (idleTimeBeforeConnectionTest != null) {
    this.setIdleTimeBeforeConnectionTest(idleTimeBeforeConnectionTest)
  }
  if (logLeakedSessions != null) {
    this.setLogLeakedSessions(logLeakedSessions)
  }
  if (maxConnectionLifetimeMillis != null) {
    this.setMaxConnectionLifetimeMillis(maxConnectionLifetimeMillis)
  }
  if (maxConnectionPoolSize != null) {
    this.setMaxConnectionPoolSize(maxConnectionPoolSize)
  }
  if (metricsEnabled != null) {
    this.setMetricsEnabled(metricsEnabled)
  }
  if (port != null) {
    this.setPort(port)
  }
}

@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("neo4jClientOptionsOf(authOptions, clusterNodeURIs, connectionAcquisitionTimeoutMillis, encrypted, encryptionOptions, eventLoopThreads, fetchSize, host, idleTimeBeforeConnectionTest, logLeakedSessions, maxConnectionLifetimeMillis, maxConnectionPoolSize, metricsEnabled, port)")
)
fun Neo4jClientOptions(
  authOptions: io.reactiverse.neo4j.options.Neo4jClientAuthOptions? = null,
  clusterNodeURIs: Iterable<String>? = null,
  connectionAcquisitionTimeoutMillis: Long? = null,
  encrypted: Boolean? = null,
  encryptionOptions: io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions? = null,
  eventLoopThreads: Int? = null,
  fetchSize: Long? = null,
  host: String? = null,
  idleTimeBeforeConnectionTest: Long? = null,
  logLeakedSessions: Boolean? = null,
  maxConnectionLifetimeMillis: Long? = null,
  maxConnectionPoolSize: Int? = null,
  metricsEnabled: Boolean? = null,
  port: Int? = null): Neo4jClientOptions = io.reactiverse.neo4j.options.Neo4jClientOptions().apply {

  if (authOptions != null) {
    this.setAuthOptions(authOptions)
  }
  if (clusterNodeURIs != null) {
    for (item in clusterNodeURIs) {
      this.addClusterNodeURI(item)
    }
  }
  if (connectionAcquisitionTimeoutMillis != null) {
    this.setConnectionAcquisitionTimeoutMillis(connectionAcquisitionTimeoutMillis)
  }
  if (encrypted != null) {
    this.setEncrypted(encrypted)
  }
  if (encryptionOptions != null) {
    this.setEncryptionOptions(encryptionOptions)
  }
  if (eventLoopThreads != null) {
    this.setEventLoopThreads(eventLoopThreads)
  }
  if (fetchSize != null) {
    this.setFetchSize(fetchSize)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (idleTimeBeforeConnectionTest != null) {
    this.setIdleTimeBeforeConnectionTest(idleTimeBeforeConnectionTest)
  }
  if (logLeakedSessions != null) {
    this.setLogLeakedSessions(logLeakedSessions)
  }
  if (maxConnectionLifetimeMillis != null) {
    this.setMaxConnectionLifetimeMillis(maxConnectionLifetimeMillis)
  }
  if (maxConnectionPoolSize != null) {
    this.setMaxConnectionPoolSize(maxConnectionPoolSize)
  }
  if (metricsEnabled != null) {
    this.setMetricsEnabled(metricsEnabled)
  }
  if (port != null) {
    this.setPort(port)
  }
}

