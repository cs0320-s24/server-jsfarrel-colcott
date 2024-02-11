package edu.brown.cs.student.main.server.broadband;

import edu.brown.cs.student.main.server.ResponseBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private BroadbandSource source;

  public BroadbandHandler(BroadbandSource source) {
    this.source = source;
  }

  HashMap<String, String> states = new HashMap<>();

  public Object handle(Request request, Response response) {
    String state = request.queryParams("state");
    String county = request.queryParams("county");

    try {
      BroadbandData broadbandData = this.source.getBroadBand(state, county);
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("state", state);
      responseMap.put("county", county);
      responseMap.put("percent", broadbandData.percentBroadband());
      responseMap.put("date", new Date());
      return ResponseBuilder.mapToJson(responseMap);
    } catch (Exception e) {
      return ResponseBuilder.buildException(400, e.getMessage());
    }
  }
}
