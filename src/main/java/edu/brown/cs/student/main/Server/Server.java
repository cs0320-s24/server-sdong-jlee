package edu.brown.cs.student.main.Server;

import static spark.Spark.after;

import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.MockedACSAPISource;
import spark.Spark;

import java.util.Optional;

public class Server {

  // constructor
    // TODO params - fake data class, actual retrieval process class, cache or no cache (specified in main),
  public Server(CSVState csvState, ACSDatasource datasource) {
    int port = 3232;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Spark.get("loadcsv", new LoadHandler(csvState));
    Spark.get("viewcsv", new ViewHandler(csvState));
    Spark.get("searchcsv", new SearchHandler(csvState));
    Spark.get("broadband", new BroadbandHandler());

    Spark.init();
    Spark.awaitInitialization();

    // Notice this link alone leads to a 404... Why is that?
    System.out.println("Server started at http://localhost:" + port);
  }

  public static void main(String[] args) {
    CSVState csvState = new CSVState();
    Server server = new Server(csvState, new MockedACSAPISource(new ACSData("test")));
  }
}
// /loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true
// /searchcsv?columnIdentifier=City/Town&searchItem=Bristol
// http://localhost:3232/loadcsv?filepath=data/RITownIncome/RI.csv/&hasHeader=true
// http://localhost:3232/searchcsv?hasHeader=true&searchItem=Barrington
