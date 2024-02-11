package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {

  public static JsonAdapter createAdapter() {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    return moshi.adapter(mapStringObject);
  }

  public static String mapToJson(Map<String, Object> map) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter adapter = moshi.adapter(mapStringObject);
    return adapter.toJson(map);
  }

  public static String buildException(int code, String description) {
    JsonAdapter<Map<String, Object>> adapter = createAdapter();
    Map<String, Object> responseMap = new HashMap<>();

    responseMap.put("type", "error");
    responseMap.put("code", code);
    responseMap.put("description", description);
    return adapter.toJson(responseMap);
  }
}
