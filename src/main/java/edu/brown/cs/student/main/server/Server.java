package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.csv.ParserState;
import spark.Spark;

public class Server {
  public static void main(String[] args) {
    int port = 3232;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    ParserState parser = new ParserState();

    // Setting up the handler for the GET /loadcsv, /viewcsv, /searchcsv
    Spark.get("loadcsv", new LoadCSVHandler(parser));
    Spark.get("viewcsv", new ViewCSVHandler(parser));
    Spark.get("searchcsv", new SearchCSVHandler(parser));
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
