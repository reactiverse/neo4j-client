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
import io.vertx.ext.neo4j.VisibleForTesting;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_CONNECTION_ACQUISITION_TIMEOUT;
import static org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_POOL_SIZE;
import static org.neo4j.driver.internal.handlers.pulln.FetchSizeUtil.DEFAULT_FETCH_SIZE;

public class DriverSupplier implements Supplier<Driver> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverSupplier.class);

    public static final String DEFAULT_SINGLE_NODE_URL = "bolt://localhost:7687";
    public static final String CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM = "routingURIs";
    public static final String USERNAME_CONFIG_PARAM = "username";
    public static final String PASSWORD_CONFIG_PARAM = "password";
    public static final String MAX_CONNECTION_POOL_SIZE_CONFIG_PARAM = "maxConnectionPoolSize";
    public static final String CONNECTION_ACQUISITION_TIMEOUT_PARAM = "connectionAcquisitionTimeout";
    public static final String NUMBER_OF_EVENT_LOOP_THREADS_PARAM = "numberOfEventLoopThreads";
    public static final String LOG_LEAKED_SESSIONS = "logLeakedSessions";
    public static final String SINGLE_NODE_URL_CONFIG_PARAM = "url";
    public static final String FETCH_SIZE = "fetchSize";
    public static final boolean DEFAULT_LOG_LEAKED_SESSIONS = true;
    public static final int DEFAULT_NUMBER_OF_EVENT_LOOP_THREADS = 2 * Runtime.getRuntime().availableProcessors();

    private final JsonObject config;

    DriverSupplier(JsonObject config) {
        this.config = requireNonNull(config, "Neo4j config should not be null");
    }

    @Override
    public Driver get() {
        JsonArray clusterNodeURIs = config.getJsonArray(CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM);
        String singleNodeURL = config.getString(SINGLE_NODE_URL_CONFIG_PARAM, DEFAULT_SINGLE_NODE_URL);
        String username = config.getString(USERNAME_CONFIG_PARAM);
        String password = config.getString(PASSWORD_CONFIG_PARAM);

        if (isClusterMode()) {
            LOG.info("Provided Neo4j Cluster mode config");
            return clusterNodeDriver(clusterNodeURIs, username, password);
        } else {
            LOG.info("Provided Neo4j Single node mode config");
            return singleNodeDriver(singleNodeURL, username, password);
        }
    }

    @VisibleForTesting Driver singleNodeDriver(String singleNodeURL, String username, String password) {
        if (noAuth()) {
            LOG.warn("Provided Neo4j config without credentials : should be acceptable in TEST mode only");
            return GraphDatabase.driver(TO_URI.apply(singleNodeURL), Config.builder().withLogging(Logging.slf4j()).withLeakedSessionsLogging().build());
        }

        return GraphDatabase.driver(TO_URI.apply(singleNodeURL), AuthTokens.basic(username, password), buildConfig());
    }

    @VisibleForTesting Driver clusterNodeDriver(JsonArray clusterNodeURIs, String username, String password) {
        return GraphDatabase.routingDriver(clusterNodeURIs.stream().map(TO_URI).collect(toList()), AuthTokens.basic(username, password), buildConfig());
    }

    private Config buildConfig() {
        Integer maxConnectionPoolSize = config.getInteger(MAX_CONNECTION_POOL_SIZE_CONFIG_PARAM, DEFAULT_MAX_CONNECTION_POOL_SIZE);
        Long connectionAcquisitionTimeout = config.getLong(CONNECTION_ACQUISITION_TIMEOUT_PARAM, DEFAULT_CONNECTION_ACQUISITION_TIMEOUT);
        Integer numberOfEventLoopThreads = config.getInteger(NUMBER_OF_EVENT_LOOP_THREADS_PARAM, DEFAULT_NUMBER_OF_EVENT_LOOP_THREADS);
        Boolean shouldLogLeakedSessions = config.getBoolean(LOG_LEAKED_SESSIONS, DEFAULT_LOG_LEAKED_SESSIONS);
        Long fetchSize = config.getLong(FETCH_SIZE, DEFAULT_FETCH_SIZE);

        LOG.info(
                "Driver initialized with " +
                        "maxConnectionPoolSize {}, " +
                        "connectionAcquisitionTimeout {} ms, " +
                        "numberOfEventLoopThreads {}, " +
                        "logLeakedSessions {}, " +
                        "fetchSize {}",
                maxConnectionPoolSize,
                connectionAcquisitionTimeout,
                numberOfEventLoopThreads,
                shouldLogLeakedSessions,
                fetchSize
        );

        Config.ConfigBuilder configBuilder = Config.builder()
                .withMaxConnectionPoolSize(maxConnectionPoolSize)
                .withConnectionAcquisitionTimeout(connectionAcquisitionTimeout, TimeUnit.MILLISECONDS)
                .withEventLoopThreads(numberOfEventLoopThreads)
                .withFetchSize(fetchSize)
                .withEncryption()
                .withTrustStrategy(Config.TrustStrategy.trustAllCertificates().withoutHostnameVerification())
                .withLogging(Logging.slf4j());

        if (shouldLogLeakedSessions) {
            configBuilder.withLeakedSessionsLogging();
        }

        return configBuilder.build();
    }

    private boolean isClusterMode() {
        return config.getJsonArray(CLUSTER_NODES_ROUTING_URIS_CONFIG_PARAM) != null;
    }

    private boolean noAuth() {
        return config.getString(USERNAME_CONFIG_PARAM) == null || config.getString(PASSWORD_CONFIG_PARAM) == null;
    }

    private static final Function<Object, URI> TO_URI = uri -> {
        try {
            return new URI(uri.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Neo4j Cluster Node URI", e);
        }
    };
}
