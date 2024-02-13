package edu.brown.cs.student.main.Server;

import static spark.Spark.after;

import spark.Spark;

public class Server {

  // constructor
    // params - fake/mocked class, actual retrieval process class, cache or no cache (specified in main),
  public Server() {
    int port = 3232;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    CSVState csvState = new CSVState();
    Spark.get("loadcsv", new LoadHandler(csvState));
    Spark.get("viewcsv", new ViewHandler(csvState));
    Spark.get("searchcsv", new SearchHandler(csvState));
    //when we make a broadband handler we pass in optional cache or no cache


    Spark.init();
    Spark.awaitInitialization();

    // Notice this link alone leads to a 404... Why is that?
    System.out.println("Server started at http://localhost:" + port);
  }



  public static void main(String[] args) {

  }
}
// /loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true
// /searchcsv?columnIdentifier=City/Town&searchItem=Bristol
// http://localhost:3232/loadcsv?filepath=data/RITownIncome/RI.csv/&hasHeader=true
// http://localhost:3232/searchcsv?hasHeader=true&searchItem=Barrington
