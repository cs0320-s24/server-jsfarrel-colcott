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

/**
 * BroadbandHandler Handles requests to broadband endpoint. Takes in params: state, county and saves
 * source in BroadbandSource.
 */
public class BroadbandHandler implements Route {

  private final BroadbandSource source;

  /**
   * BroadbandHandler constructor saves BroadbandSource
   *
   * @param source is a BroadbandSource where we get broadband data from
   */
  public BroadbandHandler(BroadbandSource source) {
    this.source = source;
  }

  /**
   * handle manages request and response to endpoint
   *
   * @param request is the request to the endpoint. Includes state and county which must be defined.
   * @param response is the response from the endpoint
   * @return Object response to request
   */
  public Object handle(Request request, Response response) {
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("state", state);
    paramMap.put("county", county);

    if (state == null || county == null) {
      return ResponseBuilder.buildException(
          "error_bad_request", 400, "Missing params. Please include state and county.", paramMap);
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
      responseMap.put("result", "success");
      responseMap.put("code", 200);
      for (String key : paramMap.keySet()) {
        responseMap.put(key, paramMap.get(key));
      }
      return ResponseBuilder.mapToJson(responseMap);
    } catch (DatasourceException e) {
      if (e.getHelperFields() != null) {
        paramMap.putAll(e.getHelperFields());
      }
      return ResponseBuilder.buildException("error_datasource", 400, e.getMessage(), paramMap);
    }
  }
}
