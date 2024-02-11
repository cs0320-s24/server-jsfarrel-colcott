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
  private ColumnSpecified columnSpecifier;

  public SearchCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  private StatusCode undefinedHandling(
      String toSearch, String columnSpecifierString, String columnIdentifier, String headerParam) {
    if (toSearch == null) {
      return new StatusCode(400, "No search value provided.");
    }
    if (headerParam == null) {
      return new StatusCode(400, "No header value provided.");
    }
    if (columnSpecifierString == null) {
      if (columnIdentifier == null) {
        this.columnSpecifier = ColumnSpecified.UNSPECIFIED;
      } else {
        return new StatusCode(
            400,
            "Column identifier provided, "
                + "but no column specifier. You must provide a column specifier ('index' or 'name')");
      }
    } else if (columnSpecifierString.equals("index")) {
      this.columnSpecifier = ColumnSpecified.INDEX;
      if (columnIdentifier == null) {
        return new StatusCode(
            400,
            "Column identifier left unspecified, "
                + "yet a column specifier was provided. To search with a column, "
                + "set a column identifier ");
      }
    } else if (columnSpecifierString.equals("name")) {
      this.columnSpecifier = ColumnSpecified.NAME;
      if (columnIdentifier == null) {
        return new StatusCode(
            400,
            "Column identifier left unspecified, "
                + "yet a column specifier was provided. To search with a column, "
                + "set a column identifier ");
      }
    } else {
      this.columnSpecifier = ColumnSpecified.UNSPECIFIED;
      if (columnIdentifier != null) {
        return new StatusCode(
            400,
            "Column specifier left unspecified, "
                + "yet a column identifier was provided. To search with a column, set column specifier "
                + "to 'index' or 'name'");
      }
    }
    return new StatusCode(200, "");
  }

  @Override
  public Object handle(Request request, Response response) {
    if (this.parserState.getParser() == null) {
      return ResponseBuilder.buildException(
          400, "File has yet to be loaded. " + "You must first use loadcsv.");
    }
    try {
      String toSearch = request.queryParams("toSearch");
      String columnSpecifierString = request.queryParams("columnSpecifier");
      String columnIdentifier = request.queryParams("columnIdentifier");
      String headerParam = request.queryParams("hasHeaders");
      StatusCode status =
          this.undefinedHandling(toSearch, columnSpecifierString, columnIdentifier, headerParam);
      if (status.code() != 200) {
        return ResponseBuilder.buildException(status.code(), status.message());
      }
      boolean hasHeaders = headerParam.equals("true");
      CSVSearcher searcher = new CSVSearcher(this.parserState.getParser(), hasHeaders);
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("code", 200);
      responseMap.put("status", "success");
      responseMap.put("results", searcher.search(toSearch, columnIdentifier, this.columnSpecifier));
      return ResponseBuilder.mapToJson(responseMap);
    } catch (FactoryFailureException e) {
      return ResponseBuilder.buildException(
          400, "File has inconsistent number of entries in columns.");
    } catch (IllegalArgumentException e) {
      return ResponseBuilder.buildException(400, e.getMessage());
    }
  }
}
