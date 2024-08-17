package com.jelly.mightyminerv2.Util.helper.graph;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
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
      map.add(context.serialize(entry.getKey()).toString(), context.serialize(entry.getValue()));
    }
    res.add("map", map);
    return res;
  }

  @Override
  public Graph<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    Graph<T> graph = new Graph<>();
    JsonObject map = json.getAsJsonObject().getAsJsonObject("map");
    for (Entry<String, JsonElement> entry : map.entrySet()) {
      Type keyType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
      Type valueType =  $Gson$Types.newParameterizedTypeWithOwner(null, List.class, keyType);

      T key = context.deserialize(new JsonParser().parse(entry.getKey()), keyType);
      List<T> value = context.deserialize(entry.getValue(), valueType);

      graph.map.put(key, value);
    }
    return graph;
  }
}
