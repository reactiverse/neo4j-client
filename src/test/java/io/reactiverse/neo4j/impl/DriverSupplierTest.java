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

package io.reactiverse.neo4j.impl;

import io.reactiverse.neo4j.options.AuthSchemeOption;
import io.reactiverse.neo4j.options.Neo4jClientAuthOptions;
import io.reactiverse.neo4j.options.Neo4jClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.security.InternalAuthToken;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.driver.internal.security.InternalAuthToken.SCHEME_KEY;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.apache.logging.*", "org.slf4j.*"}) // https://github.com/powermock/powermock/issues/864
@PrepareForTest(GraphDatabase.class)
public class DriverSupplierTest {

    @Test public void should_get_single_node_driver() {
        // Given
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<InternalAuthToken> authTokenCaptor = ArgumentCaptor.forClass(InternalAuthToken.class);
        ArgumentCaptor<Config> configCaptor = ArgumentCaptor.forClass(Config.class);
        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);

        when(GraphDatabase.driver(any(URI.class), any(InternalAuthToken.class), any(Config.class))).thenReturn(driverMock);

        // When
        DriverSupplier supplier = new DriverSupplier(new Neo4jClientOptions());
        Driver result = supplier.get();

        // Then
        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(uriCaptor.capture(), authTokenCaptor.capture(), configCaptor.capture());
        assertThat(result).isEqualTo(driverMock);
        assertThat(uriCaptor.getValue().toString()).isEqualTo("bolt://localhost:7687");
        assertThat(authTokenCaptor.getValue().toMap()).containsEntry(SCHEME_KEY, Values.value("basic"));
    }

    @Test public void should_get_single_node_driver_without_auth() {
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<InternalAuthToken> authTokenCaptor = ArgumentCaptor.forClass(InternalAuthToken.class);
        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);
        when(GraphDatabase.driver(any(URI.class), any(InternalAuthToken.class), any(Config.class))).thenReturn(driverMock);

        DriverSupplier supplier = new DriverSupplier(new Neo4jClientOptions().setAuthOptions(new Neo4jClientAuthOptions().setAuthScheme(AuthSchemeOption.NONE)));
        Driver result = supplier.get();

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(uriCaptor.capture(), authTokenCaptor.capture(), any(Config.class));
        assertThat(result).isEqualTo(driverMock);
        assertThat(uriCaptor.getValue().toString()).isEqualTo("bolt://localhost:7687");
        assertThat(authTokenCaptor.getValue().toMap()).containsEntry(SCHEME_KEY, Values.value("none"));
    }

    @Test public void should_get_cluster_node_driver() {
        ArgumentCaptor<Iterable<URI>> routingURICaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<InternalAuthToken> authTokenCaptor = ArgumentCaptor.forClass(InternalAuthToken.class);
        ArgumentCaptor<Config> configCaptor = ArgumentCaptor.forClass(Config.class);

        PowerMockito.mockStatic(GraphDatabase.class);
        Driver driverMock = mock(Driver.class);

        when(GraphDatabase.routingDriver(anyCollection(), any(InternalAuthToken.class), any(Config.class))).thenReturn(driverMock);

        DriverSupplier supplier = new DriverSupplier(new Neo4jClientOptions()
                .addClusterNodeURI("bolt+routing://localhost:7687")
                .addClusterNodeURI("bolt+routing://localhost:8687")
                .addClusterNodeURI("bolt+routing://localhost:9687"));
        Driver result = supplier.get();

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.routingDriver(routingURICaptor.capture(), authTokenCaptor.capture(), configCaptor.capture());
        assertThat(result).isEqualTo(driverMock);
        assertThat(routingURICaptor.getValue()).hasSize(3);
        assertThat(authTokenCaptor.getValue().toMap()).containsEntry(SCHEME_KEY, Values.value("basic"));
    }
}