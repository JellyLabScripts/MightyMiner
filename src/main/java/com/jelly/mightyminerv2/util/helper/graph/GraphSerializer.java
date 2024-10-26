package com.jelly.mightyminerv2.util.helper.graph;

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

public class GraphSerializer<T> implements JsonSerializer<Graph<T>>, JsonDeserializer<Graph<T>> {

  @Override
  public JsonElement serialize(Graph<T> src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject res = new JsonObject();
    JsonObject map = new JsonObject();

    for (Entry<T, List<T>> entry : src.map.entrySet()) {
      RouteWaypoint waypoint = (RouteWaypoint) entry.getKey();
      String keyString = waypoint.getX() + "," + waypoint.getY() + "," + waypoint.getZ() + "," + waypoint.getTransportMethod().name();

      JsonElement valueElement = context.serialize(entry.getValue());
      map.add(keyString, valueElement);
    }

    res.add("map", map);
    return res;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Graph<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    // Get the actual type argument (T) for the Graph<T>
    Type keyType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];

    Type valueType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, keyType);
    Graph<T> graph = new Graph<>();
    JsonObject map = json.getAsJsonObject().getAsJsonObject("map");


    for (Entry<String, JsonElement> entry : map.entrySet()) {
      try {
        // The key is a string like "33,119,419,WALK" so we need to manually parse it into RouteWaypoint
        String[] keyParts = entry.getKey().split(",");
        if (keyParts.length != 4) {
          throw new JsonParseException("Invalid RouteWaypoint key format: " + entry.getKey());
        }

        // Parse the key as RouteWaypoint
        int x = Integer.parseInt(keyParts[0]);
        int y = Integer.parseInt(keyParts[1]);
        int z = Integer.parseInt(keyParts[2]);
        TransportMethod transportMethod = TransportMethod.valueOf(keyParts[3]);
        T key = (T) new RouteWaypoint(new BlockPos(x, y, z), transportMethod);  // Cast to T

        // Deserialize the value (list of waypoints)
        List<T> value = context.deserialize(entry.getValue(), valueType);

        // Add to the graph map
        graph.map.put(key, value);
      } catch (JsonParseException | NumberFormatException e) {
        // Log the exception and continue processing other entries
        System.out.println("Error deserializing entry for key: " + entry.getKey());
        e.printStackTrace();
      }
    }

    return graph;
  }
}
