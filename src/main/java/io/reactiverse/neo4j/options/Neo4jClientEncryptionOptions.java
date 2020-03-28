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
import org.neo4j.driver.Config;
import org.neo4j.driver.Config.TrustStrategy;
import org.neo4j.driver.Config.TrustStrategy.Strategy;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@DataObject(generateConverter = true)
public class Neo4jClientEncryptionOptions {

    private static final Config DEFAULT_CONFIG = Config.defaultConfig();

    private Strategy trustStrategy;
    private String certificateFilePath;
    private boolean hostnameVerification;

    public Neo4jClientEncryptionOptions() {
        init();
    }

    public Neo4jClientEncryptionOptions(JsonObject json) {
        this();
        Neo4jClientEncryptionOptionsConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        Neo4jClientEncryptionOptionsConverter.toJson(this, json);
        return json;
    }

    private void init() {
        trustStrategy = DEFAULT_CONFIG.trustStrategy().strategy();
        certificateFilePath = Optional.ofNullable(DEFAULT_CONFIG.trustStrategy().certFile()).map(File::getAbsolutePath).orElse(null);
        hostnameVerification = DEFAULT_CONFIG.trustStrategy().isHostnameVerificationEnabled();
    }

    public Strategy getTrustStrategy() {
        return trustStrategy;
    }

    public Neo4jClientEncryptionOptions setTrustStrategy(Strategy trustStrategy) {
        this.trustStrategy = trustStrategy;
        return this;
    }

    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    public Neo4jClientEncryptionOptions setCertificateFilePath(String certificateFilePath) {
        this.certificateFilePath = certificateFilePath;
        return this;
    }

    public boolean getHostnameVerification() {
        return hostnameVerification;
    }

    public Neo4jClientEncryptionOptions setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
        return this;
    }

    public static TrustStrategy resolveTrustStrategy(Neo4jClientEncryptionOptions neo4jClientEncryptionOptions) {
        switch (neo4jClientEncryptionOptions.getTrustStrategy()) {
            case TRUST_ALL_CERTIFICATES: return toTrustAllCertificatesStrategy.apply(neo4jClientEncryptionOptions);
            case TRUST_CUSTOM_CA_SIGNED_CERTIFICATES: return toTrustCustomCaSignedCertificatesStrategy.apply(neo4jClientEncryptionOptions);
            default: return toTrustSystemCertificates.apply(neo4jClientEncryptionOptions);
        }
    }

    private static Function<Neo4jClientEncryptionOptions, UnaryOperator<TrustStrategy>> mightAddHostnameVerification =
            encryptionOptions -> trustStrategy -> encryptionOptions.getHostnameVerification() ? trustStrategy.withHostnameVerification() : trustStrategy.withoutHostnameVerification();

    private static Function<Neo4jClientEncryptionOptions, TrustStrategy> toTrustCustomCaSignedCertificatesStrategy =
            encryptionOptions -> mightAddHostnameVerification.apply(encryptionOptions).apply(TrustStrategy.trustCustomCertificateSignedBy(new File(Objects.requireNonNull(encryptionOptions.getCertificateFilePath()))));

    private static Function<Neo4jClientEncryptionOptions, TrustStrategy> toTrustAllCertificatesStrategy =
            encryptionOptions -> mightAddHostnameVerification.apply(encryptionOptions).apply(TrustStrategy.trustAllCertificates());

    private static Function<Neo4jClientEncryptionOptions, TrustStrategy> toTrustSystemCertificates =
            encryptionOptions -> mightAddHostnameVerification.apply(encryptionOptions).apply(TrustStrategy.trustSystemCertificates());
}
