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

package io.reactiverse.neo4j.options;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;

import java.util.HashMap;
import java.util.Map;

import static io.reactiverse.neo4j.options.AuthSchemeOption.BASIC;

@DataObject(generateConverter = true)
public class Neo4jClientAuthOptions {

    public static final String DEFAULT_USERNAME = "neo4j";
    public static final String DEFAULT_PASSWORD = "neo4j";

    private AuthSchemeOption authScheme;
    private String username;
    private String password;
    private String scheme;
    private String realm;
    private Map<String, Object> parameters;
    private String base64EncodedTicket;

    public Neo4jClientAuthOptions() {
        init();
    }

    public Neo4jClientAuthOptions(JsonObject json) {
        this();
        Neo4jClientAuthOptionsConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        Neo4jClientAuthOptionsConverter.toJson(this, json);
        return json;
    }

    private void init() {
        authScheme = BASIC;
        username = DEFAULT_USERNAME;
        password = DEFAULT_PASSWORD;
        parameters = new HashMap<>();
    }

    public AuthToken authToken() {
        switch (authScheme) {
            case NONE: return AuthTokens.none();
            case CUSTOM: return AuthTokens.custom(username, password, realm, scheme, parameters);
            case KERBEROS: return AuthTokens.kerberos(base64EncodedTicket);
            default: return AuthTokens.basic(username, password);
        }
    }

    public AuthSchemeOption getAuthScheme() {
        return authScheme;
    }

    public Neo4jClientAuthOptions setAuthScheme(AuthSchemeOption authScheme) {
        this.authScheme = authScheme;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Neo4jClientAuthOptions setUsername(String username) {
        this.username = username;
        return this;
    }

    public Neo4jClientAuthOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public Neo4jClientAuthOptions setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getRealm() {
        return realm;
    }

    public Neo4jClientAuthOptions setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Neo4jClientAuthOptions addParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    public String getBase64EncodedTicket() {
        return base64EncodedTicket;
    }

    public Neo4jClientAuthOptions setBase64EncodedTicket(String base64EncodedTicket) {
        this.base64EncodedTicket = base64EncodedTicket;
        return this;
    }
}
