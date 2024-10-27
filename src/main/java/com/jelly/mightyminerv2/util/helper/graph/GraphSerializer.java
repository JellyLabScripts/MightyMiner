package com.jelly.mightyminerv2.util.helper.graph;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.$Gson$Types;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;
import net.minecraft.util.BlockPos;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

public class GraphSerializer implements JsonSerializer<Graph<RouteWaypoint>>, JsonDeserializer<Graph<RouteWaypoint>> {

  @Override
  public JsonElement serialize(Graph<RouteWaypoint> src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject res = new JsonObject();
    JsonObject map = new JsonObject();

    for (Entry<RouteWaypoint, List<RouteWaypoint>> entry : src.map.entrySet()) {
      RouteWaypoint waypoint = entry.getKey();
      String keyString = waypoint.getX() + "," + waypoint.getY() + "," + waypoint.getZ() + "," + waypoint.getTransportMethod().name();

      JsonElement valueElement = context.serialize(entry.getValue());
      map.add(keyString, valueElement);
    }

    res.add("map", map);
    return res;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Graph<RouteWaypoint> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    Graph<RouteWaypoint> graph = new Graph<>();
    JsonObject map = json.getAsJsonObject().getAsJsonObject("map");

    for (Entry<String, JsonElement> entry : map.entrySet()) {
      try {
        // The key is a string like "33,119,419,WALK" so we need to manually parse it into RouteWaypoint
        String[] keyParts = entry.getKey().split(",");
        if (keyParts.length != 4) {
          throw new JsonParseException("Invalid RouteWaypoint key format: " + entry.getKey());
        }

        int x = Integer.parseInt(keyParts[0]);
        int y = Integer.parseInt(keyParts[1]);
        int z = Integer.parseInt(keyParts[2]);
        TransportMethod transportMethod = TransportMethod.valueOf(keyParts[3]);
        RouteWaypoint key = new RouteWaypoint(x, y, z, transportMethod);

        List<RouteWaypoint> value = context.deserialize(entry.getValue(), new TypeToken<List<RouteWaypoint>>() {}.getType());

        graph.map.put(key, value);
      } catch (JsonParseException | NumberFormatException e) {
        System.out.println("Error deserializing entry for key: " + entry.getKey());
        e.printStackTrace();
      }
    }

    return graph;
  }
}
