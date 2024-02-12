package edu.brown.cs.student.main.server.csv;

import edu.brown.cs.student.main.csv.CSVParser;
import edu.brown.cs.student.main.csv.CreatorFromRow;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import edu.brown.cs.student.main.server.ResponseBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * LoadCSVHandler
 * Handles requests to loadcsv endpoint.
 * Takes in params: filepath and saves parse to ParserState.
 */
public class LoadCSVHandler implements Route {
  private final ParserState parserState;

  /**
   * LoadCSVHandler constructor saves ParserState
   * @param parserState is the parser for the server
   */
  public LoadCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  /**
   * handle manages request and response to endpoint
   * @param request is the request to the endpoint. Includes filepath parameter which must be defined.
   * @param response is the response from the endpoint
   * @return Object response to request
   */
  @Override
  public Object handle(Request request, Response response) {
    String filepath = request.queryParams("filepath");
    if (filepath == null) {
      return ResponseBuilder.buildException(400, "Must specify filepath in endpoint.");
    }

    if (!(filepath.startsWith("data/")) || filepath.contains("/..") || filepath.contains("../")) {
      return ResponseBuilder.buildException(
          400, "Illegal file path. File must be in the data folder.");
    }
    if (!filepath.endsWith(".csv")) {
      return ResponseBuilder.buildException(
          400, "Filepath didn't lead to a CSV file (make sure file path ends in '.csv').");
    }

    try {
      FileReader reader = new FileReader(filepath);
      CreatorFromRow<String[]> creator = row -> row.toArray(new String[0]);
      this.parserState.setParser(new CSVParser(reader, creator));
    } catch (FileNotFoundException e) {
      return ResponseBuilder.buildException(404, "File not found.");
    } catch (FactoryFailureException e) {
      return ResponseBuilder.buildException(
          400, "File has inconsistent number of entries in columns.");
    } catch (IOException e) {
      return ResponseBuilder.buildException(400, "Unable to read from file.");
    } catch (IllegalArgumentException e) {
      return ResponseBuilder.buildException(400, "Malformed CSV data.");
    }

    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("type", "success");
    responseMap.put("code", 200);
    responseMap.put("filepath", filepath);
    return ResponseBuilder.mapToJson(responseMap);
  }
}
