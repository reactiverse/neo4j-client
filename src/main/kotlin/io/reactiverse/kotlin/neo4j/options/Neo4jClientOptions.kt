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

fun neo4jClientOptionsOf(
  authOptions: io.reactiverse.neo4j.options.Neo4jClientAuthOptions? = null,
  clusterNodeURIs: Iterable<String>? = null,
  connectionAcquisitionTimeout: Long? = null,
  driverMetricsEnabled: Boolean? = null,
  encryptionEnabled: Boolean? = null,
  encryptionOptions: io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions? = null,
  fetchSize: Long? = null,
  host: String? = null,
  logLeakedSessions: Boolean? = null,
  maxConnectionPoolSize: Int? = null,
  numberOfEventLoopThreads: Int? = null,
  port: Int? = null): Neo4jClientOptions = io.reactiverse.neo4j.options.Neo4jClientOptions().apply {

  if (authOptions != null) {
    this.setAuthOptions(authOptions)
  }
  if (clusterNodeURIs != null) {
    for (item in clusterNodeURIs) {
      this.addClusterNodeURI(item)
    }
  }
  if (connectionAcquisitionTimeout != null) {
    this.setConnectionAcquisitionTimeout(connectionAcquisitionTimeout)
  }
  if (driverMetricsEnabled != null) {
    this.setDriverMetricsEnabled(driverMetricsEnabled)
  }
  if (encryptionEnabled != null) {
    this.setEncryptionEnabled(encryptionEnabled)
  }
  if (encryptionOptions != null) {
    this.setEncryptionOptions(encryptionOptions)
  }
  if (fetchSize != null) {
    this.setFetchSize(fetchSize)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (logLeakedSessions != null) {
    this.setLogLeakedSessions(logLeakedSessions)
  }
  if (maxConnectionPoolSize != null) {
    this.setMaxConnectionPoolSize(maxConnectionPoolSize)
  }
  if (numberOfEventLoopThreads != null) {
    this.setNumberOfEventLoopThreads(numberOfEventLoopThreads)
  }
  if (port != null) {
    this.setPort(port)
  }
}

@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("neo4jClientOptionsOf(authOptions, clusterNodeURIs, connectionAcquisitionTimeout, driverMetricsEnabled, encryptionEnabled, encryptionOptions, fetchSize, host, logLeakedSessions, maxConnectionPoolSize, numberOfEventLoopThreads, port)")
)
fun Neo4jClientOptions(
  authOptions: io.reactiverse.neo4j.options.Neo4jClientAuthOptions? = null,
  clusterNodeURIs: Iterable<String>? = null,
  connectionAcquisitionTimeout: Long? = null,
  driverMetricsEnabled: Boolean? = null,
  encryptionEnabled: Boolean? = null,
  encryptionOptions: io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions? = null,
  fetchSize: Long? = null,
  host: String? = null,
  logLeakedSessions: Boolean? = null,
  maxConnectionPoolSize: Int? = null,
  numberOfEventLoopThreads: Int? = null,
  port: Int? = null): Neo4jClientOptions = io.reactiverse.neo4j.options.Neo4jClientOptions().apply {

  if (authOptions != null) {
    this.setAuthOptions(authOptions)
  }
  if (clusterNodeURIs != null) {
    for (item in clusterNodeURIs) {
      this.addClusterNodeURI(item)
    }
  }
  if (connectionAcquisitionTimeout != null) {
    this.setConnectionAcquisitionTimeout(connectionAcquisitionTimeout)
  }
  if (driverMetricsEnabled != null) {
    this.setDriverMetricsEnabled(driverMetricsEnabled)
  }
  if (encryptionEnabled != null) {
    this.setEncryptionEnabled(encryptionEnabled)
  }
  if (encryptionOptions != null) {
    this.setEncryptionOptions(encryptionOptions)
  }
  if (fetchSize != null) {
    this.setFetchSize(fetchSize)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (logLeakedSessions != null) {
    this.setLogLeakedSessions(logLeakedSessions)
  }
  if (maxConnectionPoolSize != null) {
    this.setMaxConnectionPoolSize(maxConnectionPoolSize)
  }
  if (numberOfEventLoopThreads != null) {
    this.setNumberOfEventLoopThreads(numberOfEventLoopThreads)
  }
  if (port != null) {
    this.setPort(port)
  }
}

