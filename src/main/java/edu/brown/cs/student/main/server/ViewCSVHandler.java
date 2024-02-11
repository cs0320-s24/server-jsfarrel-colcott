package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.csv.CSVSearcher;
import edu.brown.cs.student.main.csv.CSVSearcher.ColumnSpecified;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.IOException;
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
    try {
      // create response
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("code", 200);
      responseMap.put("CSV data", this.parserState.getParser().parse());
      return ResponseBuilder.mapToJson(responseMap);
    }catch (FactoryFailureException e){
      return ResponseBuilder.buildException(404, "File has malformed csv data.");
    } catch (IOException e) {
      //TODO: put a useful message
      return ResponseBuilder.buildException(404, "Unable to read from file.");
    }
  }
}
