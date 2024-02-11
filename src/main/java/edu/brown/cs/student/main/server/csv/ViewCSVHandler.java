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
    // todo: throw error if parserState isn't defined/loadcsv endpoint has yet to be called
    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("code", 200);
    responseMap.put("status", "success");
    responseMap.put("csv", this.parserState.getParser().getParsed());
    return ResponseBuilder.mapToJson(responseMap);
  }
}
