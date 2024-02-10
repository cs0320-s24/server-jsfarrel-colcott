package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.csv.CSVParser;
import edu.brown.cs.student.main.csv.CSVSearcher;
import edu.brown.cs.student.main.csv.CSVSearcher.ColumnSpecified;
import edu.brown.cs.student.main.csv.CreatorFromRow;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.IOException;
import java.util.List;
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
    try {
      String toSearch = request.queryParams("toSearch");
      String columnSpecifierString = request.queryParams("columnSpecifier");
      ColumnSpecified columnSpecifier;
      //TODO: should index be capitalized?
      if(columnSpecifierString.equals("index")){
        columnSpecifier = ColumnSpecified.INDEX;
      }else if (columnSpecifierString.equals("name")){
        columnSpecifier = ColumnSpecified.NAME;
      }else{
        columnSpecifier = ColumnSpecified.UNSPECIFIED;
      }
      String columnIdentifier = request.queryParams("columnIdentifier");
      String headerParam = request.queryParams("hasHeaders");
      boolean hasHeaders = headerParam.equals("true");
      CSVSearcher searcher = new CSVSearcher(this.parserState.getParser(), hasHeaders);
      return searcher.search(toSearch, columnIdentifier, columnSpecifier);
    }catch (FactoryFailureException e){
      //TODO: put a useful message
      return ResponseBuilder.buildException(404, "File has malformed csv data.");
    }
  }
}
