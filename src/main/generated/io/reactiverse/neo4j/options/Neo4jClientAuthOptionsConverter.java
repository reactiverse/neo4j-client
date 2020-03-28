package io.reactiverse.neo4j.options;

import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link io.reactiverse.neo4j.options.Neo4jClientAuthOptions}.
 * NOTE: This class has been automatically generated from the {@link io.reactiverse.neo4j.options.Neo4jClientAuthOptions} original class using Vert.x codegen.
 */
public class Neo4jClientAuthOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Neo4jClientAuthOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "authScheme":
          if (member.getValue() instanceof String) {
            obj.setAuthScheme(io.reactiverse.neo4j.options.AuthSchemeOption.valueOf((String)member.getValue()));
          }
          break;
        case "base64EncodedTicket":
          if (member.getValue() instanceof String) {
            obj.setBase64EncodedTicket((String)member.getValue());
          }
          break;
        case "parameters":
          if (member.getValue() instanceof JsonObject) {
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof Object)
                obj.addParameter(entry.getKey(), entry.getValue());
            });
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "realm":
          if (member.getValue() instanceof String) {
            obj.setRealm((String)member.getValue());
          }
          break;
        case "scheme":
          if (member.getValue() instanceof String) {
            obj.setScheme((String)member.getValue());
          }
          break;
        case "username":
          if (member.getValue() instanceof String) {
            obj.setUsername((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(Neo4jClientAuthOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Neo4jClientAuthOptions obj, java.util.Map<String, Object> json) {
    if (obj.getAuthScheme() != null) {
      json.put("authScheme", obj.getAuthScheme().name());
    }
    if (obj.getBase64EncodedTicket() != null) {
      json.put("base64EncodedTicket", obj.getBase64EncodedTicket());
    }
    if (obj.getParameters() != null) {
      JsonObject map = new JsonObject();
      obj.getParameters().forEach((key, value) -> map.put(key, value));
      json.put("parameters", map);
    }
    if (obj.getRealm() != null) {
      json.put("realm", obj.getRealm());
    }
    if (obj.getScheme() != null) {
      json.put("scheme", obj.getScheme());
    }
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
  }
}
