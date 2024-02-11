package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.broadband.ACSBroadbandSource;
import edu.brown.cs.student.main.server.broadband.BroadbandHandler;
import edu.brown.cs.student.main.server.csv.LoadCSVHandler;
import edu.brown.cs.student.main.server.csv.SearchCSVHandler;
import edu.brown.cs.student.main.server.csv.ViewCSVHandler;
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
    Spark.get("broadband", new BroadbandHandler(new ACSBroadbandSource()));
    Spark.get("loadcsv", new LoadCSVHandler(parser));
    Spark.get("viewcsv", new ViewCSVHandler(parser));
    Spark.get("searchcsv", new SearchCSVHandler(parser));
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
