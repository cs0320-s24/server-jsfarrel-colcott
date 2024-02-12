package edu.brown.cs.student.main.server.broadband;

import edu.brown.cs.student.main.exception.DatasourceException;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private BroadbandSource source;

  public BroadbandHandler(BroadbandSource source) {
    this.source = source;
  }

  public Object handle(Request request, Response response) {
    String state = request.queryParams("state");
    String county = request.queryParams("county");

    if (state == null || county == null) {
      return ResponseBuilder.buildException(
          400, "Missing params. Please include state and county.");
    }

    try {
      BroadbandData broadbandData = this.source.getBroadBand(state, county);
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("state", state);
      responseMap.put("county", county);
      responseMap.put("percent", broadbandData.percentBroadband());
      // src:
      // https://stackoverflow.com/questions/2942857/how-to-convert-current-date-into-string-in-java
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
      responseMap.put("date", dateFormat.format(new Date()) + " EST");
      responseMap.put("type", "success");
      responseMap.put("code", 200);
      return ResponseBuilder.mapToJson(responseMap);
    } catch (DatasourceException e) {
      return ResponseBuilder.buildException(400, e.getMessage());
    }
  }
}
