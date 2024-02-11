package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.ResponseBuilder;
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
    if (this.parserState.getParser() == null) {
      return ResponseBuilder.buildException(
          400, "File has yet to be loaded. " + "You must first use loadcsv.");
    }
    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code", 200);
    responseMap.put("type", "success");
    responseMap.put("data", this.parserState.getParser().getParsed());
    return ResponseBuilder.mapToJson(responseMap);
  }
}
