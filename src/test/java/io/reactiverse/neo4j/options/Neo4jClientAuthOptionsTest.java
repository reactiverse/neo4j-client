/*
 * Copyright (c) 2018-2020 Olympe S.A.
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

import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.security.InternalAuthToken;

import java.util.HashMap;
import java.util.Map;

import static io.reactiverse.neo4j.options.AuthSchemeOption.*;
import static io.reactiverse.neo4j.options.Neo4jClientAuthOptions.DEFAULT_PASSWORD;
import static io.reactiverse.neo4j.options.Neo4jClientAuthOptions.DEFAULT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.neo4j.driver.internal.security.InternalAuthToken.*;

public class Neo4jClientAuthOptionsTest {

    @Test public void should_have_default_configuration() {
        // When
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions();

        // Then
        assertThat(options.getAuthScheme()).isEqualTo(BASIC);
        assertThat(options.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(options.getScheme()).isNull();
        assertThat(options.getBase64EncodedTicket()).isNull();
        assertThat(options.getParameters()).isEmpty();
        assertThat(options.getRealm()).isNull();
        assertThat(options).extracting("password").containsExactly(DEFAULT_PASSWORD);
    }

    @Test public void should_have_default_auth_token() {
        // Given
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions();

        // When
        InternalAuthToken authToken = (InternalAuthToken) options.authToken();

        // Then
        assertThat(authToken.toMap()).extracting(
                PRINCIPAL_KEY,
                CREDENTIALS_KEY,
                SCHEME_KEY,
                REALM_KEY,
                PARAMETERS_KEY)
                .containsExactly(Values.value(DEFAULT_USERNAME), Values.value(DEFAULT_PASSWORD), Values.value("basic"), null, null);
    }

    @Test public void should_have_specific_configuration() {
        // When
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions()
                .setUsername("user1")
                .setPassword("pwd1234")
                .setRealm("customRealm")
                .setScheme("customScheme")
                .setAuthScheme(CUSTOM)
                .addParameter("key1", "value1")
                .addParameter("key2", 3);

        // Then
        assertThat(options.getAuthScheme()).isEqualTo(CUSTOM);
        assertThat(options.getUsername()).isEqualTo("user1");
        assertThat(options.getScheme()).isEqualTo("customScheme");
        assertThat(options.getBase64EncodedTicket()).isNull();
        assertThat(options.getParameters()).containsExactly(entry("key1", "value1"), entry("key2", 3));
        assertThat(options.getRealm()).isEqualTo("customRealm");
        assertThat(options).extracting("password").containsExactly("pwd1234");
    }

    @Test public void should_have_custom_auth_token() {
        // Given
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions()
                .setUsername("user1")
                .setPassword("pwd1234")
                .setRealm("customRealm")
                .setScheme("customScheme")
                .setAuthScheme(CUSTOM)
                .addParameter("key1", "value1")
                .addParameter("key2", 3);

        // When
        InternalAuthToken authToken = (InternalAuthToken) options.authToken();

        // Then
        Map<String, Object> expectedParameters = new HashMap<>();
        expectedParameters.put("key1", "value1");
        expectedParameters.put("key2", 3);

        assertThat(authToken.toMap()).extracting(
                PRINCIPAL_KEY,
                CREDENTIALS_KEY,
                SCHEME_KEY,
                REALM_KEY,
                PARAMETERS_KEY)
                .containsExactly(Values.value("user1"), Values.value("pwd1234"), Values.value("customScheme"), Values.value("customRealm"), Values.value(expectedParameters));
    }

    @Test public void should_have_kerberos_auth_token() {
        // Given
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions()
                .setAuthScheme(KERBEROS)
                .setBase64EncodedTicket("base64Ticket");

        // When
        InternalAuthToken authToken = (InternalAuthToken) options.authToken();

        // Then
        assertThat(authToken.toMap()).extracting(
                PRINCIPAL_KEY,
                CREDENTIALS_KEY,
                SCHEME_KEY)
                .containsExactly(Values.value(""), Values.value("base64Ticket"), Values.value("kerberos"));
    }

    @Test public void should_convert_to_json() {
        // Given
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions()
                .setUsername("user1")
                .setPassword("pwd1234")
                .setRealm("customRealm")
                .setScheme("customScheme")
                .setAuthScheme(CUSTOM)
                .addParameter("key1", "value1")
                .addParameter("key2", 3);

        // When
        JsonObject jsonObject = options.toJson();

        // Then
        assertThat(jsonObject.getString("username")).isEqualTo("user1");
        assertThat(jsonObject.getString("realm")).isEqualTo("customRealm");
        assertThat(jsonObject.getString("scheme")).isEqualTo("customScheme");
        assertThat(jsonObject.getString("authScheme")).isEqualTo(CUSTOM.toString());
        assertThat(jsonObject.getJsonObject("parameters").getString("key1")).isEqualTo("value1");
        assertThat(jsonObject.getJsonObject("parameters").getInteger("key2")).isEqualTo(3);
        assertThat(jsonObject.getString("password")).isNull();
    }

    @Test public void should_convert_from_json() {
        // Given
        JsonObject jsonObject = new JsonObject()
            .put("username", "user1")
            .put("realm", "customRealm")
            .put("scheme", "customScheme")
            .put("authScheme", CUSTOM.toString())
            .put("parameters", new JsonObject().put("key1", "value1").put("key2", 3))
            .put("password", "pass1234");

        // When
        Neo4jClientAuthOptions options = new Neo4jClientAuthOptions(jsonObject);

        // Then
        assertThat(options.getAuthScheme()).isEqualTo(CUSTOM);
        assertThat(options.getUsername()).isEqualTo("user1");
        assertThat(options.getScheme()).isEqualTo("customScheme");
        assertThat(options.getBase64EncodedTicket()).isNull();
        assertThat(options.getParameters()).containsExactly(entry("key1", "value1"), entry("key2", 3));
        assertThat(options.getRealm()).isEqualTo("customRealm");
        assertThat(options).extracting("password").containsExactly("pass1234");
    }
}