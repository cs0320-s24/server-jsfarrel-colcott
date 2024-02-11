package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.csv.ParserState;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
  private final ParserState parserState;

  public ViewCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  @Override
  public Object handle(Request request, Response response) {
    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code", 200);
    responseMap.put("CSV data", this.parserState.getParser().getParsed());
    return ResponseBuilder.mapToJson(responseMap);
  }
}
