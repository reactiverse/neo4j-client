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
            obj.setAuthOptions(new io.reactiverse.neo4j.options.Neo4jClientAuthOptions((JsonObject)member.getValue()));
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
        case "connectionAcquisitionTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectionAcquisitionTimeout(((Number)member.getValue()).longValue());
          }
          break;
        case "driverMetricsEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setDriverMetricsEnabled((Boolean)member.getValue());
          }
          break;
        case "encryptionEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setEncryptionEnabled((Boolean)member.getValue());
          }
          break;
        case "encryptionOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setEncryptionOptions(new io.reactiverse.neo4j.options.Neo4jClientEncryptionOptions((JsonObject)member.getValue()));
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
        case "logLeakedSessions":
          if (member.getValue() instanceof Boolean) {
            obj.setLogLeakedSessions((Boolean)member.getValue());
          }
          break;
        case "maxConnectionPoolSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxConnectionPoolSize(((Number)member.getValue()).intValue());
          }
          break;
        case "numberOfEventLoopThreads":
          if (member.getValue() instanceof Number) {
            obj.setNumberOfEventLoopThreads(((Number)member.getValue()).intValue());
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
    json.put("connectionAcquisitionTimeout", obj.getConnectionAcquisitionTimeout());
    json.put("driverMetricsEnabled", obj.isDriverMetricsEnabled());
    json.put("encryptionEnabled", obj.isEncryptionEnabled());
    if (obj.getEncryptionOptions() != null) {
      json.put("encryptionOptions", obj.getEncryptionOptions().toJson());
    }
    json.put("fetchSize", obj.getFetchSize());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("logLeakedSessions", obj.isLogLeakedSessions());
    json.put("maxConnectionPoolSize", obj.getMaxConnectionPoolSize());
    json.put("numberOfEventLoopThreads", obj.getNumberOfEventLoopThreads());
    json.put("port", obj.getPort());
  }
}
