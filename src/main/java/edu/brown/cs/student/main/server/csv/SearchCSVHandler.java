package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.CSVSearcher;
import edu.brown.cs.student.main.csv.CSVSearcher.ColumnSpecified;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {
  private final ParserState parserState;

  public SearchCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  @Override
  public Object handle(Request request, Response response) {
    // todo: throw error if parserState isn't defined/loadcsv endpoint has yet to be called

    try {
      String toSearch = request.queryParams("toSearch");
      String columnSpecifierString = request.queryParams("columnSpecifier");
      String columnIdentifier = request.queryParams("columnIdentifier");
      String headerParam = request.queryParams("hasHeaders");
      /* todo: throw error if any inputs == null, unless both columnSpecifier and columnIdentifier
          aren't defined */
      ColumnSpecified columnSpecifier;
      // todo: i think this will break if columnSpecifierString isn't defined
      //  we should allow it to not be defined when we want columnSpecifier = ColumnSpecified.UNSPECIFIED;
      if (columnSpecifierString.equals("index")) {
        columnSpecifier = ColumnSpecified.INDEX;
      } else if (columnSpecifierString.equals("name")) {
        columnSpecifier = ColumnSpecified.NAME;
      } else {
        columnSpecifier = ColumnSpecified.UNSPECIFIED;
      }
      boolean hasHeaders = headerParam.equals("true");
      CSVSearcher searcher = new CSVSearcher(this.parserState.getParser(), hasHeaders);
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("code", 200);
      responseMap.put("type", "success");
      responseMap.put("results", searcher.search(toSearch, columnIdentifier, columnSpecifier));
      return ResponseBuilder.mapToJson(responseMap);
    } catch (FactoryFailureException e) {
      return ResponseBuilder.buildException(400, "File has malformed csv data.");
    } catch (IllegalArgumentException e) {
      // TODO: make that message more helpful (include the argument, etc.)
      return ResponseBuilder.buildException(400, e.getMessage());
    }
  }
}
