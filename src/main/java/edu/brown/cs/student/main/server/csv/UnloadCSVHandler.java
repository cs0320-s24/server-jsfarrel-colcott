package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * LoadCSVHandler Handles requests to loadcsv endpoint. Takes in params: filepath and saves parse to
 * ParserState.
 */
public class UnloadCSVHandler implements Route {
  private final ParserState parserState;

  /**
   * UnloadCSVHandler constructor saves ParserState
   *
   * @param parserState is the parser for the server
   */
  public UnloadCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  /**
   * handle manages request and response to endpoint
   *
   * @param request is the request to the endpoint. Includes filepath parameter which must be
   *     defined.
   * @param response is the response from the endpoint
   * @return Object response to request
   */
  @Override
  public Object handle(Request request, Response response) {
    this.parserState.setParser(null);
    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("result", "success");
    responseMap.put("code", 200);
    return ResponseBuilder.mapToJson(responseMap);
  }
}
