package io.reactiverse.neo4j.options;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.reactiverse.neo4j.options.Neo4jClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.reactiverse.neo4j.options.Neo4jClientOptions} original class using Vert.x codegen.
 */
public class Neo4jClientOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Neo4jClientOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "authOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setAuthOptions(new io.reactiverse.neo4j.options.Neo4jClientAuthOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "clusterNodeURIs":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addClusterNodeURI((String)item);
            });
          }
          break;
        case "connectionAcquisitionTimeoutMillis":
          if (member.getValue() instanceof Number) {
            obj.setConnectionAcquisitionTimeoutMillis(((Number)member.getValue()).longValue());
          }
          break;
        case "encrypted":
          if (member.getValue() instanceof Boolean) {
            obj.setEncrypted((Boolean)member.getValue());
          }
          break;
        case "encryptionOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setEncryptionOptions(new io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "eventLoopThreads":
          if (member.getValue() instanceof Number) {
            obj.setEventLoopThreads(((Number)member.getValue()).intValue());
          }
          break;
        case "fetchSize":
          if (member.getValue() instanceof Number) {
            obj.setFetchSize(((Number)member.getValue()).longValue());
          }
          break;
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "idleTimeBeforeConnectionTest":
          if (member.getValue() instanceof Number) {
            obj.setIdleTimeBeforeConnectionTest(((Number)member.getValue()).longValue());
          }
          break;
        case "logLeakedSessions":
          if (member.getValue() instanceof Boolean) {
            obj.setLogLeakedSessions((Boolean)member.getValue());
          }
          break;
        case "maxConnectionLifetimeMillis":
          if (member.getValue() instanceof Number) {
            obj.setMaxConnectionLifetimeMillis(((Number)member.getValue()).longValue());
          }
          break;
        case "maxConnectionPoolSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxConnectionPoolSize(((Number)member.getValue()).intValue());
          }
          break;
        case "metricsEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setMetricsEnabled((Boolean)member.getValue());
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(Neo4jClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Neo4jClientOptions obj, java.util.Map<String, Object> json) {
    if (obj.getAuthOptions() != null) {
      json.put("authOptions", obj.getAuthOptions().toJson());
    }
    if (obj.getClusterNodeURIs() != null) {
      JsonArray array = new JsonArray();
      obj.getClusterNodeURIs().forEach(item -> array.add(item));
      json.put("clusterNodeURIs", array);
    }
    json.put("connectionAcquisitionTimeoutMillis", obj.getConnectionAcquisitionTimeoutMillis());
    json.put("encrypted", obj.isEncrypted());
    if (obj.getEncryptionOptions() != null) {
      json.put("encryptionOptions", obj.getEncryptionOptions().toJson());
    }
    json.put("eventLoopThreads", obj.getEventLoopThreads());
    json.put("fetchSize", obj.getFetchSize());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("idleTimeBeforeConnectionTest", obj.getIdleTimeBeforeConnectionTest());
    json.put("logLeakedSessions", obj.isLogLeakedSessions());
    json.put("maxConnectionLifetimeMillis", obj.getMaxConnectionLifetimeMillis());
    json.put("maxConnectionPoolSize", obj.getMaxConnectionPoolSize());
    json.put("metricsEnabled", obj.isMetricsEnabled());
    json.put("port", obj.getPort());
  }
}
