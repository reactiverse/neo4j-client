package io.reactiverse.neo4j.options;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions}.
 * NOTE: This class has been automatically generated from the {@link io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions} original class using Vert.x codegen.
 */
public class Neo4jClientEncryptionOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Neo4jClientEncryptionOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "certificateFilePath":
          if (member.getValue() instanceof String) {
            obj.setCertificateFilePath((String)member.getValue());
          }
          break;
        case "hostnameVerification":
          if (member.getValue() instanceof Boolean) {
            obj.setHostnameVerification((Boolean)member.getValue());
          }
          break;
        case "strategy":
          if (member.getValue() instanceof String) {
            obj.setStrategy(org.neo4j.driver.Config.TrustStrategy.Strategy.valueOf((String)member.getValue()));
          }
          break;
      }
    }
  }

  public static void toJson(Neo4jClientEncryptionOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Neo4jClientEncryptionOptions obj, java.util.Map<String, Object> json) {
    if (obj.getCertificateFilePath() != null) {
      json.put("certificateFilePath", obj.getCertificateFilePath());
    }
    json.put("hostnameVerification", obj.getHostnameVerification());
    if (obj.getStrategy() != null) {
      json.put("strategy", obj.getStrategy().name());
    }
  }
}
