package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.csv.CSVParser;
import edu.brown.cs.student.main.csv.CreatorFromRow;
import edu.brown.cs.student.main.csv.ParserState;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler implements Route {
  private final ParserState parserState;

  public LoadCSVHandler(ParserState parserState) {
    this.parserState = parserState;
  }

  @Override
  public Object handle(Request request, Response response) {
    String filepath = request.queryParams("filepath");
    if (!(filepath.startsWith("data/")) || filepath.contains("/..")) {
      return ResponseBuilder.buildException(404, "Illegal file path. File must be in the data folder.");
    }
    try {
      FileReader reader = new FileReader(filepath);
      CreatorFromRow<String[]> creator = row -> row.toArray(new String[0]);
      this.parserState.setParser(new CSVParser(reader, creator));
    } catch (FileNotFoundException e) {
      return ResponseBuilder.buildException(404, "File not found.");
    }

    // create response
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("type", "success");
    responseMap.put("code", 200);
    responseMap.put("filepath", filepath);
    return ResponseBuilder.mapToJson(responseMap);
  }
}
