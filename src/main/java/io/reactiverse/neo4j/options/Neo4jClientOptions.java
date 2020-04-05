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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Config.ConfigBuilder;
import org.neo4j.driver.Logging;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@DataObject(generateConverter = true)
public class Neo4jClientOptions {

    private static final Config DEFAULT_CONFIG = Config.defaultConfig();

    private ConfigBuilder builder;

    public static final String DEFAULT_SINGLE_NODE_HOST = "localhost";
    public static final int DEFAULT_SINGLE_NODE_PORT = 7687;

    // single node parameters
    private String host;
    private int port;

    // cluster parameters
    private Set<String> clusterNodeURIs;

    private int maxConnectionPoolSize;
    private long connectionAcquisitionTimeoutMillis;
    private long idleTimeBeforeConnectionTest;
    private long maxConnectionLifetimeMillis;
    private int eventLoopThreads;
    private boolean logLeakedSessions;
    private long fetchSize;
    private boolean encrypted;
    private boolean isMetricsEnabled;

    // auth parameters
    private Neo4jClientAuthOptions authOptions;
    private Neo4jClientEncryptionOptions encryptionOptions;

    public Neo4jClientOptions() {
        this(Config.builder());
    }

    public Neo4jClientOptions(ConfigBuilder builder) {
        this.builder = builder;
        init();
    }

    public Neo4jClientOptions(JsonObject json) {
        this();
        Neo4jClientOptionsConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        Neo4jClientOptionsConverter.toJson(this, json);
        return json;
    }

    private void init() {
        host = DEFAULT_SINGLE_NODE_HOST;
        port = DEFAULT_SINGLE_NODE_PORT;
        clusterNodeURIs = new HashSet<>();
        maxConnectionPoolSize = DEFAULT_CONFIG.maxConnectionPoolSize();
        connectionAcquisitionTimeoutMillis = DEFAULT_CONFIG.connectionAcquisitionTimeoutMillis();
        eventLoopThreads = DEFAULT_CONFIG.eventLoopThreads();
        logLeakedSessions = DEFAULT_CONFIG.logLeakedSessions();
        fetchSize = DEFAULT_CONFIG.fetchSize();
        encrypted = DEFAULT_CONFIG.encrypted();
        isMetricsEnabled = DEFAULT_CONFIG.isMetricsEnabled();
        idleTimeBeforeConnectionTest = DEFAULT_CONFIG.idleTimeBeforeConnectionTest();
        maxConnectionLifetimeMillis = DEFAULT_CONFIG.maxConnectionLifetimeMillis();
        authOptions = new Neo4jClientAuthOptions();
        encryptionOptions = new Neo4jClientEncryptionOptions();
        builder.withLogging(Logging.slf4j()); // TODO : support other loggers ?
    }

    public Config neo4jConfig() {
        return builder.build();
    }

    public AuthToken authToken() {
        return authOptions.authToken();
    }

    public String getHost() {
        return host;
    }

    public Neo4jClientOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Neo4jClientOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public Set<String> getClusterNodeURIs() {
        return clusterNodeURIs;
    }

    public Neo4jClientOptions addClusterNodeURI(String clusterNodeURI) {
        this.clusterNodeURIs.add(clusterNodeURI);
        return this;
    }

    public Neo4jClientAuthOptions getAuthOptions() {
        return authOptions;
    }

    public Neo4jClientOptions setAuthOptions(Neo4jClientAuthOptions authOptions) {
        this.authOptions = authOptions;
        return this;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public Neo4jClientOptions setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
        builder.withMaxConnectionPoolSize(maxConnectionPoolSize);
        return this;
    }

    public long getConnectionAcquisitionTimeoutMillis() {
        return connectionAcquisitionTimeoutMillis;
    }

    public Neo4jClientOptions setConnectionAcquisitionTimeoutMillis(long connectionAcquisitionTimeoutMillis) {
        this.connectionAcquisitionTimeoutMillis = connectionAcquisitionTimeoutMillis;
        builder.withConnectionAcquisitionTimeout(connectionAcquisitionTimeoutMillis, TimeUnit.MILLISECONDS);
        return this;
    }

    public long getMaxConnectionLifetimeMillis() {
        return maxConnectionLifetimeMillis;
    }

    public Neo4jClientOptions setMaxConnectionLifetimeMillis(long maxConnectionLifetimeMillis) {
        this.maxConnectionLifetimeMillis = maxConnectionLifetimeMillis;
        builder.withMaxConnectionLifetime(maxConnectionLifetimeMillis, TimeUnit.MILLISECONDS);
        return this;
    }

    public long getIdleTimeBeforeConnectionTest() {
        return idleTimeBeforeConnectionTest;
    }

    public Neo4jClientOptions setIdleTimeBeforeConnectionTest(long idleTimeBeforeConnectionTest) {
        this.idleTimeBeforeConnectionTest = idleTimeBeforeConnectionTest;
        builder.withConnectionLivenessCheckTimeout(idleTimeBeforeConnectionTest, TimeUnit.MILLISECONDS);
        return this;
    }

    public int getEventLoopThreads() {
        return eventLoopThreads;
    }

    public Neo4jClientOptions setEventLoopThreads(int eventLoopThreads) {
        this.eventLoopThreads = eventLoopThreads;
        builder.withEventLoopThreads(eventLoopThreads);
        return this;
    }

    public boolean isLogLeakedSessions() {
        return logLeakedSessions;
    }

    public Neo4jClientOptions setLogLeakedSessions(boolean enabled) {
        this.logLeakedSessions = enabled;
        if (enabled) builder.withLeakedSessionsLogging();
        return this;
    }

    public long getFetchSize() {
        return fetchSize;
    }

    public Neo4jClientOptions setFetchSize(long fetchSize) {
        this.fetchSize = fetchSize;
        builder.withFetchSize(fetchSize);
        return this;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public Neo4jClientOptions setEncrypted(boolean enabled) {
        this.encrypted = enabled;
        if (enabled) {
            builder.withEncryption();
        } else {
            builder.withoutEncryption();
        }
        return this;
    }

    public boolean isMetricsEnabled() {
        return isMetricsEnabled;
    }

    public Neo4jClientOptions setMetricsEnabled(boolean enabled) {
        this.isMetricsEnabled = enabled;
        if (enabled) {
            builder.withDriverMetrics();
        } else {
            builder.withoutDriverMetrics();
        }
        return this;
    }

    public Neo4jClientEncryptionOptions getEncryptionOptions() {
        return encryptionOptions;
    }

    public Neo4jClientOptions setEncryptionOptions(Neo4jClientEncryptionOptions encryptionOptions) {
        this.encryptionOptions = encryptionOptions;
        builder.withTrustStrategy(encryptionOptions.toTrustStrategy());
        return this;
    }
}
