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

import io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions
import org.neo4j.driver.Config.TrustStrategy.Strategy

fun neo4jClientEncryptionOptionsOf(
  certificateFilePath: String? = null,
  hostnameVerification: Boolean? = null,
  trustStrategy: Strategy? = null): Neo4jClientEncryptionOptions = io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions().apply {

  if (certificateFilePath != null) {
    this.setCertificateFilePath(certificateFilePath)
  }
  if (hostnameVerification != null) {
    this.setHostnameVerification(hostnameVerification)
  }
  if (trustStrategy != null) {
    this.setTrustStrategy(trustStrategy)
  }
}

@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("neo4jClientEncryptionOptionsOf(certificateFilePath, hostnameVerification, trustStrategy)")
)
fun Neo4jClientEncryptionOptions(
  certificateFilePath: String? = null,
  hostnameVerification: Boolean? = null,
  trustStrategy: Strategy? = null): Neo4jClientEncryptionOptions = io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions().apply {

  if (certificateFilePath != null) {
    this.setCertificateFilePath(certificateFilePath)
  }
  if (hostnameVerification != null) {
    this.setHostnameVerification(hostnameVerification)
  }
  if (trustStrategy != null) {
    this.setTrustStrategy(trustStrategy)
  }
}

