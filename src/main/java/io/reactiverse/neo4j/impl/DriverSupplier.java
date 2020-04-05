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

import io.reactiverse.neo4j.VisibleForTesting;
import io.reactiverse.neo4j.options.Neo4jClientOptions;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DriverSupplier implements Supplier<Driver> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverSupplier.class);

    private final Neo4jClientOptions config;

    DriverSupplier(Neo4jClientOptions config) {
        this.config = requireNonNull(config, "Neo4j config should not be null");
    }

    @Override
    public Driver get() {
        AuthToken authToken = config.authToken();
        Config driverConfig = config.neo4jConfig();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Driver initialized with parameters : {}", config.toJson());
        }

        if (isClusterMode()) {
            LOG.info("Provided Neo4j Cluster mode config");
            return clusterNodeDriver(config.getClusterNodeURIs(), authToken, driverConfig);
        } else {
            LOG.info("Provided Neo4j Single node mode config");
            return singleNodeDriver(getSingleNodeUrl(), authToken, driverConfig);
        }
    }

    @VisibleForTesting Driver singleNodeDriver(String singleNodeURL, AuthToken authToken, Config config) {
        return GraphDatabase.driver(TO_URI.apply(singleNodeURL), authToken, config);
    }

    @VisibleForTesting Driver clusterNodeDriver(Set<String> clusterNodeURIs, AuthToken authToken, Config config) {
        return GraphDatabase.routingDriver(clusterNodeURIs.stream().map(TO_URI).collect(toList()), authToken, config);
    }

    private boolean isClusterMode() {
        return !config.getClusterNodeURIs().isEmpty();
    }

    public String getSingleNodeUrl() {
        return "bolt://" + config.getHost() + ":" + config.getPort();
    }

    private static final Function<Object, URI> TO_URI = uri -> {
        try {
            return new URI(uri.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Neo4j Cluster Node URI", e);
        }
    };
}
