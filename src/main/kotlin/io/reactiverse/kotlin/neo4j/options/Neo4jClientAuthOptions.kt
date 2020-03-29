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

import io.reactiverse.neo4j.options.Neo4jClientAuthOptions
import io.reactiverse.neo4j.options.AuthSchemeOption

fun neo4jClientAuthOptionsOf(
  authScheme: AuthSchemeOption? = null,
  base64EncodedTicket: String? = null,
  parameters: Map<String, Any>? = null,
  password: String? = null,
  realm: String? = null,
  scheme: String? = null,
  username: String? = null): Neo4jClientAuthOptions = io.reactiverse.neo4j.options.Neo4jClientAuthOptions().apply {

  if (authScheme != null) {
    this.setAuthScheme(authScheme)
  }
  if (base64EncodedTicket != null) {
    this.setBase64EncodedTicket(base64EncodedTicket)
  }
  if (parameters != null) {
    for (item in parameters) {
      this.addParameter(item.key, item.value)
    }
  }
  if (password != null) {
    this.setPassword(password)
  }
  if (realm != null) {
    this.setRealm(realm)
  }
  if (scheme != null) {
    this.setScheme(scheme)
  }
  if (username != null) {
    this.setUsername(username)
  }
}

@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("neo4jClientAuthOptionsOf(authScheme, base64EncodedTicket, parameters, password, realm, scheme, username)")
)
fun Neo4jClientAuthOptions(
  authScheme: AuthSchemeOption? = null,
  base64EncodedTicket: String? = null,
  parameters: Map<String, Any>? = null,
  password: String? = null,
  realm: String? = null,
  scheme: String? = null,
  username: String? = null): Neo4jClientAuthOptions = io.reactiverse.neo4j.options.Neo4jClientAuthOptions().apply {

  if (authScheme != null) {
    this.setAuthScheme(authScheme)
  }
  if (base64EncodedTicket != null) {
    this.setBase64EncodedTicket(base64EncodedTicket)
  }
  if (parameters != null) {
    for (item in parameters) {
      this.addParameter(item.key, item.value)
    }
  }
  if (password != null) {
    this.setPassword(password)
  }
  if (realm != null) {
    this.setRealm(realm)
  }
  if (scheme != null) {
    this.setScheme(scheme)
  }
  if (username != null) {
    this.setUsername(username)
  }
}

