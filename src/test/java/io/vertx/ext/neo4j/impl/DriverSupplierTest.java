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

package io.vertx.ext.neo4j.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.v1.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;

import static io.vertx.ext.neo4j.impl.DriverSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_POOL_SIZE;
import static org.neo4j.driver.v1.Config.LoadBalancingStrategy;
import static org.neo4j.driver.v1.Config.LoadBalancingStrategy.LEAST_CONNECTED;
import static org.neo4j.driver.v1.Config.LoadBalancingStrategy.ROUND_ROBIN;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.apache.logging.*", "org.slf4j.*"}) // https://github.com/powermock/powermock/issues/864
@PrepareForTest(GraphDatabase.class)
public class DriverSupplierTest {

    @Test public void should_get_single_node_driver_without_auth() {
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);
        when(GraphDatabase.driver(any(URI.class), any(Config.class))).thenReturn(driverMock);

        DriverSupplier supplier = new DriverSupplier(new JsonObject());
        Driver result = supplier.get();

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(uriCaptor.capture(), any(Config.class));
        assertThat(result).isEqualTo(driverMock);
        assertThat(uriCaptor.getValue().toString()).isEqualTo(DEFAULT_SINGLE_NODE_URL);
    }

    @Test public void should_get_single_node_driver_with_specific_values() {
        String givenUsername = "username";
        String givenPassword = "password";
        String givenURL = "bolt://localhost:5555";
        int givenMaxConnectionPoolSize = 15;

        JsonObject config = new JsonObject()
                .put(USERNAME_CONFIG_PARAM, givenUsername)
                .put(PASSWORD_CONFIG_PARAM, givenPassword)
                .put(SINGLE_NODE_URL_CONFIG_PARAM, givenURL)
                .put(MAX_CONNECTION_POOL_SIZE_CONFIG_PARAM, givenMaxConnectionPoolSize);

        should_get_single_node_driver(config, givenURL, givenUsername, givenPassword, givenMaxConnectionPoolSize);
    }

    @Test public void should_get_single_node_driver_with_default_values() {
        String givenUsername = "username";
        String givenPassword = "password";

        JsonObject config = new JsonObject()
                .put(USERNAME_CONFIG_PARAM, givenUsername)
                .put(PASSWORD_CONFIG_PARAM, givenPassword);

        should_get_single_node_driver(config, DEFAULT_SINGLE_NODE_URL, givenUsername, givenPassword, DEFAULT_MAX_CONNECTION_POOL_SIZE);
    }

    @Test public void should_get_cluster_node_driver_with_default_values() {
        String givenUsername = "username";
        String givenPassword = "password";
        JsonArray givenRoutingURIs = new JsonArray().add("bolt+routing://localhost:7687").add("bolt+routing://localhost:8687").add("bolt+routing://localhost:9687");

        JsonObject config = new JsonObject()
                .put(USERNAME_CONFIG_PARAM, givenUsername)
                .put(PASSWORD_CONFIG_PARAM, givenPassword)
                .put(CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM, givenRoutingURIs);

        should_get_cluster_node_driver(config, givenUsername, givenPassword, DEFAULT_MAX_CONNECTION_POOL_SIZE, LEAST_CONNECTED, 3);
    }

    @Test public void should_get_cluster_node_driver_with_specific_values() {
        String givenUsername = "username";
        String givenPassword = "password";
        JsonArray givenRoutingURIs = new JsonArray().add("bolt+routing://localhost:6687").add("bolt+routing://localhost:7687").add("bolt+routing://localhost:8687").add("bolt+routing://localhost:9687");
        int givenMaxConnectionPoolSize = 15;
        LoadBalancingStrategy givenLoadBalancingStrategy = ROUND_ROBIN;

        JsonObject config = new JsonObject()
                .put(USERNAME_CONFIG_PARAM, givenUsername)
                .put(PASSWORD_CONFIG_PARAM, givenPassword)
                .put(CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM, givenRoutingURIs)
                .put(MAX_CONNECTION_POOL_SIZE_CONFIG_PARAM, givenMaxConnectionPoolSize)
                .put(LOAD_BALANCING_STRATEGY_CONFIG_PARAM, givenLoadBalancingStrategy);

        should_get_cluster_node_driver(config, givenUsername, givenPassword, givenMaxConnectionPoolSize, givenLoadBalancingStrategy, 4);
    }

    @Test public void should_throw_exception_when_config_is_null() {
        assertThatThrownBy(() -> new DriverSupplier(null)).hasMessage("Neo4j config should not be null");
    }

    @Test public void should_throw_exception_when_single_node_url_is_invalid() {
        String invalidSingleNodeURL = "bolt: //localhost:7687";

        JsonObject config = new JsonObject().put(SINGLE_NODE_URL_CONFIG_PARAM, invalidSingleNodeURL);

        DriverSupplier supplier = new DriverSupplier(config);

        assertThatThrownBy(supplier::get).hasMessage("Invalid Neo4j Cluster Node URI");
    }

    @Test public void should_throw_exception_when_one_of_routing_url_is_invalid() {
        String invalidRoutingNodeURL = "bolt+routing: //localhost:8687";

        JsonObject config = new JsonObject().put(CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM, new JsonArray().add("bolt+routing://localhost:7687").add(invalidRoutingNodeURL).add("bolt+routing://localhost:9687"));

        DriverSupplier supplier = new DriverSupplier(config);

        assertThatThrownBy(supplier::get).hasMessage("Invalid Neo4j Cluster Node URI");
    }

    private void should_get_cluster_node_driver(JsonObject config, String expectedUsername, String expectedPassword, int expectedMaxConnectionPoolSize, LoadBalancingStrategy expectedLoadBalancingStrategy, int expectedRoutingURIListSize) {
        ArgumentCaptor<Iterable<URI>> routingURICaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<AuthToken> authTokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        ArgumentCaptor<Config> configCaptor = ArgumentCaptor.forClass(Config.class);

        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);

        when(GraphDatabase.routingDriver(anyCollection(), any(AuthToken.class), any(Config.class))).thenReturn(driverMock);

        DriverSupplier supplier = new DriverSupplier(config);
        Driver result = supplier.get();

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.routingDriver(routingURICaptor.capture(), authTokenCaptor.capture(), configCaptor.capture());
        assertThat(result).isEqualTo(driverMock);
        assertThat(authTokenCaptor.getValue()).isEqualTo(AuthTokens.basic(expectedUsername, expectedPassword));
        assertThat(configCaptor.getValue().maxConnectionPoolSize()).isEqualTo(expectedMaxConnectionPoolSize);
        assertThat(configCaptor.getValue().loadBalancingStrategy()).isEqualTo(expectedLoadBalancingStrategy);
        assertThat(routingURICaptor.getValue()).hasSize(expectedRoutingURIListSize);
    }

    private void should_get_single_node_driver(JsonObject config, String expectedURL, String expectedUsername, String expectedPassword, int expectedMaxConnectionPoolSize) {
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<AuthToken> authTokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        ArgumentCaptor<Config> configCaptor = ArgumentCaptor.forClass(Config.class);
        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);

        when(GraphDatabase.driver(any(URI.class), any(AuthToken.class), any(Config.class))).thenReturn(driverMock);

        DriverSupplier supplier = new DriverSupplier(config);
        Driver result = supplier.get();

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(uriCaptor.capture(), authTokenCaptor.capture(), configCaptor.capture());
        assertThat(result).isEqualTo(driverMock);
        assertThat(uriCaptor.getValue().toString()).isEqualTo(expectedURL);
        assertThat(authTokenCaptor.getValue()).isEqualTo(AuthTokens.basic(expectedUsername, expectedPassword));
        assertThat(configCaptor.getValue().maxConnectionPoolSize()).isEqualTo(expectedMaxConnectionPoolSize);
    }
}