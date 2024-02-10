package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.csv.ParserState;
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
    return "unimplemented";
  }
}
