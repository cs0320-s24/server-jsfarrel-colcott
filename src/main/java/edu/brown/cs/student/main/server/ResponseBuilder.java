package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** ResponseBuilder contains methods to help create API responses. */
public class ResponseBuilder {

  /**
   * createAdapter creates a JsonAdapter
   *
   * @return a moshi JsonAdapter
   */
  public static JsonAdapter<Map<String, Object>> createAdapter() {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    return moshi.adapter(mapStringObject);
  }

  /**
   * mapToJson takes in String to Object map, and creates a String representation (JSON) of map
   *
   * @param map is a String to Object map
   * @return String representation (JSON) of map
   */
  public static String mapToJson(Map<String, Object> map) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Object> adapter = moshi.adapter(mapStringObject);
    return adapter.toJson(map);
  }

  /**
   * buildException takes in an error code and description and returns a String representation
   * (JSON) of the exception.
   *
   * @param code the error code for the exception
   * @param description a description of the exception
   * @return a String representation (JSON) of the exception with code, description, and type
   */
  public static String buildException(
      String errorCode, int code, String description, Map<String, Object> params) {
    JsonAdapter<Map<String, Object>> adapter = createAdapter();
    Map<String, Object> responseMap = new HashMap<>();

    responseMap.put("result", errorCode);
    responseMap.put("code", code);
    responseMap.put("description", description);
    for (String key : params.keySet()) {
      responseMap.put(key, params.get(key));
    }
    return adapter.toJson(responseMap);
  }
}
