package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * ViewCSVHandler Handles requests to viewcsv endpoint. Takes in no params and saves parse to
 * ParserState.
 */
public class ViewCSVHandler implements Route {
  private final ParserState parserState;

  /**
   * ViewCSVHandler constructor saves ParserState
   *
   * @param parserState is the parser for the server
   */
  public ViewCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  /**
   * handle manages request and response to endpoint
   *
   * @param request is the request to the endpoint
   * @param response is the response from the endpoint
   * @return Object response to request
   */
  @Override
  public Object handle(Request request, Response response) {
    if (this.parserState.getParser() == null) {
      return ResponseBuilder.buildException(
          "error_bad_json",
          400,
          "File has yet to be loaded. " + "You must first use loadcsv.",
          new HashMap<>());
    }
    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code", 200);
    responseMap.put("result", "success");
    responseMap.put("data", this.parserState.getParser().getParsed());
    return ResponseBuilder.mapToJson(responseMap);
  }
}
