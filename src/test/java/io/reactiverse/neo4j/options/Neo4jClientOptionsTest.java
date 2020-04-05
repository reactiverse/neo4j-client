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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;

import static io.reactiverse.neo4j.options.Neo4jClientOptions.DEFAULT_SINGLE_NODE_HOST;
import static io.reactiverse.neo4j.options.Neo4jClientOptions.DEFAULT_SINGLE_NODE_PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.internal.async.pool.PoolSettings.*;
import static org.neo4j.driver.internal.handlers.pulln.FetchSizeUtil.DEFAULT_FETCH_SIZE;

public class Neo4jClientOptionsTest {

    @Test public void should_have_default_configuration() {
        // When
        Neo4jClientOptions options = new Neo4jClientOptions();

        // Then
        assertThat(options.getClusterNodeURIs()).isEmpty();
        assertThat(options.getHost()).isEqualTo(DEFAULT_SINGLE_NODE_HOST);
        assertThat(options.getPort()).isEqualTo(DEFAULT_SINGLE_NODE_PORT);
        assertThat(options.getAuthOptions()).isNotNull();
        assertThat(options.getEncryptionOptions()).isNotNull();
        assertThat(options.isLogLeakedSessions()).isFalse();
        assertThat(options.isEncrypted()).isFalse();
        assertThat(options.isMetricsEnabled()).isFalse();
        assertThat(options.getConnectionAcquisitionTimeoutMillis()).isEqualTo(DEFAULT_CONNECTION_ACQUISITION_TIMEOUT);
        assertThat(options.getMaxConnectionPoolSize()).isEqualTo(DEFAULT_MAX_CONNECTION_POOL_SIZE);
        assertThat(options.getFetchSize()).isEqualTo(DEFAULT_FETCH_SIZE);
        assertThat(options.getMaxConnectionLifetimeMillis()).isEqualTo(DEFAULT_MAX_CONNECTION_LIFETIME);
        assertThat(options.getIdleTimeBeforeConnectionTest()).isEqualTo(DEFAULT_IDLE_TIME_BEFORE_CONNECTION_TEST);
        assertThat(options.getEventLoopThreads()).isEqualTo(0);
    }

    @Test public void should_check_default_neo4j_config() {
        // Given
        Neo4jClientOptions options = new Neo4jClientOptions();

        // When
        Config config = options.neo4jConfig();

        // Then
        assertThat(config.isMetricsEnabled()).isEqualTo(options.isMetricsEnabled());
        assertThat(config.trustStrategy()).isNotNull();
        assertThat(config.encrypted()).isEqualTo(options.isEncrypted());
        assertThat(config.fetchSize()).isEqualTo(options.getFetchSize());
        assertThat(config.logLeakedSessions()).isEqualTo(options.isLogLeakedSessions());
        assertThat(config.eventLoopThreads()).isEqualTo(options.getEventLoopThreads());
        assertThat(config.maxConnectionPoolSize()).isEqualTo(options.getMaxConnectionPoolSize());
        assertThat(config.logging()).isInstanceOf(Logging.slf4j().getClass());
        assertThat(config.connectionAcquisitionTimeoutMillis()).isEqualTo(options.getConnectionAcquisitionTimeoutMillis());
        assertThat(config.maxConnectionLifetimeMillis()).isEqualTo(options.getMaxConnectionLifetimeMillis());
        assertThat(config.idleTimeBeforeConnectionTest()).isEqualTo(options.getIdleTimeBeforeConnectionTest());
    }

    @Test public void should_have_specific_configuration() {
        // When
        Neo4jClientOptions options = new Neo4jClientOptions()
                .setHost("12.4.5.6")
                .setPort(9999)
                .setEncrypted(true)
                .setLogLeakedSessions(true)
                .setConnectionAcquisitionTimeoutMillis(5000)
                .setMaxConnectionLifetimeMillis(8000)
                .setIdleTimeBeforeConnectionTest(7000)
                .setMetricsEnabled(true)
                .setFetchSize(5000)
                .setMaxConnectionPoolSize(200)
                .setEventLoopThreads(12)
                .addClusterNodeURI("bolt+routing://198.67.88.11:8888")
                .addClusterNodeURI("bolt+routing://177.66.1.2:7777")
                .setAuthOptions(new Neo4jClientAuthOptions())
                .setEncryptionOptions(new Neo4jClientEncryptionOptions());

        // Then
        assertThat(options.getClusterNodeURIs()).containsExactlyInAnyOrder("bolt+routing://198.67.88.11:8888", "bolt+routing://177.66.1.2:7777");
        assertThat(options.getHost()).isEqualTo("12.4.5.6");
        assertThat(options.getPort()).isEqualTo(9999);
        assertThat(options.getAuthOptions()).isNotNull();
        assertThat(options.getEncryptionOptions()).isNotNull();
        assertThat(options.getEncryptionOptions()).isNotNull();
        assertThat(options.isLogLeakedSessions()).isTrue();
        assertThat(options.isEncrypted()).isTrue();
        assertThat(options.isMetricsEnabled()).isTrue();
        assertThat(options.getConnectionAcquisitionTimeoutMillis()).isEqualTo(5000);
        assertThat(options.getMaxConnectionLifetimeMillis()).isEqualTo(8000);
        assertThat(options.getIdleTimeBeforeConnectionTest()).isEqualTo(7000);
        assertThat(options.getMaxConnectionPoolSize()).isEqualTo(200);
        assertThat(options.getFetchSize()).isEqualTo(5000);
        assertThat(options.getEventLoopThreads()).isEqualTo(12);
    }

    @Test public void should_convert_to_json() {
        // Given
        Neo4jClientOptions options = new Neo4jClientOptions()
                .setHost("12.4.5.6")
                .setPort(9999)
                .setEncrypted(true)
                .setLogLeakedSessions(true)
                .setConnectionAcquisitionTimeoutMillis(5000)
                .setMaxConnectionLifetimeMillis(8000)
                .setIdleTimeBeforeConnectionTest(7000)
                .setMetricsEnabled(true)
                .setFetchSize(6000)
                .setMaxConnectionPoolSize(200)
                .setEventLoopThreads(12)
                .addClusterNodeURI("bolt+routing://198.67.88.11:8888")
                .addClusterNodeURI("bolt+routing://177.66.1.2:7777")
                .setAuthOptions(new Neo4jClientAuthOptions())
                .setEncryptionOptions(new Neo4jClientEncryptionOptions());

        // When
        JsonObject jsonObject = options.toJson();

        // Then
        assertThat(jsonObject.getString("host")).isEqualTo("12.4.5.6");
        assertThat(jsonObject.getInteger("port")).isEqualTo(9999);
        assertThat(jsonObject.getBoolean("encrypted")).isTrue();
        assertThat(jsonObject.getBoolean("logLeakedSessions")).isTrue();
        assertThat(jsonObject.getLong("connectionAcquisitionTimeoutMillis")).isEqualTo(5000);
        assertThat(jsonObject.getLong("maxConnectionLifetimeMillis")).isEqualTo(8000);
        assertThat(jsonObject.getLong("idleTimeBeforeConnectionTest")).isEqualTo(7000);
        assertThat(jsonObject.getBoolean("metricsEnabled")).isTrue();
        assertThat(jsonObject.getLong("fetchSize")).isEqualTo(6000);
        assertThat(jsonObject.getInteger("maxConnectionPoolSize")).isEqualTo(200);
        assertThat(jsonObject.getInteger("eventLoopThreads")).isEqualTo(12);
        assertThat(jsonObject.getJsonArray("clusterNodeURIs")).containsExactlyInAnyOrder("bolt+routing://198.67.88.11:8888", "bolt+routing://177.66.1.2:7777");
        assertThat(jsonObject.getJsonObject("authOptions")).isNotNull();
        assertThat(jsonObject.getJsonObject("encryptionOptions")).isNotNull();
    }

    @Test public void should_convert_from_json() {
        // Given
        JsonObject jsonObject = new JsonObject()
            .put("host", "12.4.5.6")
            .put("port", 9999)
            .put("encrypted", true)
            .put("logLeakedSessions", true)
            .put("connectionAcquisitionTimeoutMillis", 5000)
            .put("maxConnectionLifetimeMillis", 8000)
            .put("idleTimeBeforeConnectionTest", 7000)
            .put("metricsEnabled", true)
            .put("fetchSize", 6000)
            .put("maxConnectionPoolSize", 200)
            .put("eventLoopThreads", 12)
            .put("clusterNodeURIs", new JsonArray().add("bolt+routing://198.67.88.11:8888").add("bolt+routing://177.66.1.2:7777"))
            .put("authOptions", new JsonObject())
            .put("encryptionOptions", new JsonObject());

        // When
        Neo4jClientOptions options = new Neo4jClientOptions(jsonObject);

        // Then
        assertThat(options.getClusterNodeURIs()).containsExactlyInAnyOrder("bolt+routing://198.67.88.11:8888", "bolt+routing://177.66.1.2:7777");
        assertThat(options.getHost()).isEqualTo("12.4.5.6");
        assertThat(options.getPort()).isEqualTo(9999);
        assertThat(options.getAuthOptions()).isNotNull();
        assertThat(options.getEncryptionOptions()).isNotNull();
        assertThat(options.getEncryptionOptions()).isNotNull();
        assertThat(options.isLogLeakedSessions()).isTrue();
        assertThat(options.isEncrypted()).isTrue();
        assertThat(options.isMetricsEnabled()).isTrue();
        assertThat(options.getConnectionAcquisitionTimeoutMillis()).isEqualTo(5000);
        assertThat(options.getMaxConnectionLifetimeMillis()).isEqualTo(8000);
        assertThat(options.getIdleTimeBeforeConnectionTest()).isEqualTo(7000);
        assertThat(options.getMaxConnectionPoolSize()).isEqualTo(200);
        assertThat(options.getFetchSize()).isEqualTo(6000);
        assertThat(options.getEventLoopThreads()).isEqualTo(12);
    }
}