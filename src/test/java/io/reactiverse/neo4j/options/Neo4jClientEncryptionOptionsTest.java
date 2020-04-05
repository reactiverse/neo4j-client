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
import org.neo4j.driver.Config;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.Config.TrustStrategy.Strategy.*;

public class Neo4jClientEncryptionOptionsTest {

    @Test public void should_have_default_configuration() {
        // When
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions();

        // Then
        assertThat(options.getStrategy()).isEqualTo(TRUST_SYSTEM_CA_SIGNED_CERTIFICATES);
        assertThat(options.getHostnameVerification()).isTrue();
        assertThat(options.getCertificateFilePath()).isNull();
    }

    @Test public void should_resolve_default_trust_strategy() {
        // Given
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions();

        // When
        Config.TrustStrategy trustStrategy = options.toTrustStrategy();

        // Then
        assertThat(trustStrategy.strategy()).isEqualTo(TRUST_SYSTEM_CA_SIGNED_CERTIFICATES);
        assertThat(trustStrategy.isHostnameVerificationEnabled()).isTrue();
        assertThat(trustStrategy.certFile()).isNull();
    }

    @Test public void should_have_specific_configuration() {
        // When
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions()
                .setStrategy(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES)
                .setHostnameVerification(false)
                .setCertificateFilePath("/path/to/certificate");

        // Then
        assertThat(options.getStrategy()).isEqualTo(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES);
        assertThat(options.getHostnameVerification()).isFalse();
        assertThat(options.getCertificateFilePath()).isEqualTo("/path/to/certificate");
    }

    @Test public void should_resolve_custom_ca_signed_trust_strategy() {
        // Given
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions()
                .setStrategy(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES)
                .setHostnameVerification(false)
                .setCertificateFilePath("/path/to/certificate");

        // When
        Config.TrustStrategy trustStrategy = options.toTrustStrategy();

        // Then
        assertThat(trustStrategy.strategy()).isEqualTo(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES);
        assertThat(trustStrategy.isHostnameVerificationEnabled()).isFalse();
        assertThat(trustStrategy.certFile()).isEqualTo(new File("/path/to/certificate"));
    }

    @Test public void should_resolve_trust_all_strategy() {
        // Given
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions()
                .setStrategy(TRUST_ALL_CERTIFICATES)
                .setHostnameVerification(false);

        // When
        Config.TrustStrategy trustStrategy = options.toTrustStrategy();

        // Then
        assertThat(trustStrategy.strategy()).isEqualTo(TRUST_ALL_CERTIFICATES);
        assertThat(trustStrategy.isHostnameVerificationEnabled()).isFalse();
    }

    @Test public void should_convert_to_json() {
        // Given
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions()
                .setStrategy(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES)
                .setHostnameVerification(false)
                .setCertificateFilePath("/path/to/certificate");

        // When
        JsonObject jsonObject = options.toJson();

        // Then
        assertThat(jsonObject.getString("strategy")).isEqualTo(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES.toString());
        assertThat(jsonObject.getString("certificateFilePath")).isEqualTo("/path/to/certificate");
        assertThat(jsonObject.getBoolean("hostnameVerification")).isFalse();
    }

    @Test public void should_build_from_json() {
        // Given
        JsonObject jsonObject = new JsonObject()
                .put("strategy", TRUST_CUSTOM_CA_SIGNED_CERTIFICATES.toString())
                .put("certificateFilePath", "/path/to/certificate")
                .put("hostnameVerification", false);

        // When
        Neo4jClientEncryptionOptions options = new Neo4jClientEncryptionOptions(jsonObject);

        // Then
        assertThat(options.getStrategy()).isEqualTo(TRUST_CUSTOM_CA_SIGNED_CERTIFICATES);
        assertThat(options.isHostnameVerification()).isFalse();
        assertThat(options.getCertificateFilePath()).isEqualTo("/path/to/certificate");
    }
}