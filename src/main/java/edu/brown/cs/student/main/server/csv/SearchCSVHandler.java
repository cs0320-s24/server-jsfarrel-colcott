package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.CSVSearcher;
import edu.brown.cs.student.main.csv.CSVSearcher.ColumnSpecified;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * SearchCSVHandler Handles requests to searchcsv endpoint. Takes in params: toSearch,
 * columnSpecifier, columnIdentifier, hasHeader and saves parse to ParserState.
 */
public class SearchCSVHandler implements Route {
  private final ParserState parserState;
  private ColumnSpecified columnSpecifier;
  private List<String> columnHeaders;

  /**
   * SearchCSVHandler constructor saves ParserState
   *
   * @param parserState is the parser for the server
   */
  public SearchCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  /**
   * undefinedHandling determines whether there is an issue with the inputs to SearchCSVHandler
   *
   * @param toSearch - the value being searched for
   * @param columnSpecifierString - the specification for column
   * @param columnIdentifier - the column being identified
   * @param headerParam - weather csv has headers
   * @return the status code depending on whether there's an issue with defined/undefined inputs
   */
  private StatusCode undefinedHandling(
      String toSearch, String columnSpecifierString, String columnIdentifier, String headerParam) {
    if (toSearch == null) {
      return new StatusCode(400, "No search value provided.");
    }
    if (headerParam == null) {
      return new StatusCode(400, "No header value provided.");
    }

    if (!headerParam.equals("false") && !headerParam.equals("true")) {
      return new StatusCode(400, "hasHeaders param must equal true or false.");
    }
    if (headerParam.equals("false")
        && columnSpecifierString != null
        && columnSpecifierString.equals("name")) {
      return new StatusCode(400, "hasHeaders must be true for columnSpecifier to be name.");
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

  /**
   * handle manages request and response to endpoint
   *
   * @param request is the request to the endpoint. Includes searching params.
   * @param response is the response from the endpoint
   * @return Object response to request
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> paramMap = new HashMap<>();
    String toSearch = request.queryParams("toSearch");
    String columnSpecifierString = request.queryParams("columnSpecifier");
    String columnIdentifier = request.queryParams("columnIdentifier");
    String headerParam = request.queryParams("hasHeaders");
    paramMap.put("toSearch", toSearch);
    paramMap.put("columnSpecifier", columnSpecifierString);
    paramMap.put("columnIdentifier", columnIdentifier);
    paramMap.put("hasHeaders", headerParam);
    if (this.parserState.getParser() == null) {
      return ResponseBuilder.buildException(
          "error_bad_json",
          400,
          "File has yet to be loaded. " + "You must first use loadcsv.",
          paramMap);
    }
    try {
      StatusCode status =
          this.undefinedHandling(toSearch, columnSpecifierString, columnIdentifier, headerParam);
      if (status.code() != 200) {
        return ResponseBuilder.buildException(
            "error_bad_request", status.code(), status.message(), paramMap);
      }
      boolean hasHeaders = headerParam.equals("true");
      CSVSearcher searcher = new CSVSearcher(this.parserState.getParser(), hasHeaders);
      if (hasHeaders) {
        this.columnHeaders = searcher.getColumHeaders();
      }
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("code", 200);
      responseMap.put("result", "success");
      responseMap.put("data", searcher.search(toSearch, columnIdentifier, this.columnSpecifier));
      for (String key : paramMap.keySet()) {
        responseMap.put(key, paramMap.get(key));
      }
      return ResponseBuilder.mapToJson(responseMap);
    } catch (FactoryFailureException e) {
      return ResponseBuilder.buildException(
          "error_datasource", 400, "File has inconsistent number of entries in columns.", paramMap);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("Column not found.")) {
        paramMap.put("valid-columns", this.columnHeaders);
      }
      return ResponseBuilder.buildException("error_bad_request", 400, e.getMessage(), paramMap);
    }
  }
}
