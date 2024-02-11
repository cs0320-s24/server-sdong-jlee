package edu.brown.cs.student.main.Server;

import static spark.Spark.after;

import spark.Spark;

public class TestServer {
  public static void main(String[] args) {
    int port = 3232;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    CSVState csvState = new CSVState();
    Spark.get("loadcsv", new LoadHandler(csvState));
    //        Spark.get("activity", new ActivityHandler());

    Spark.init();
    Spark.awaitInitialization();

    // Notice this link alone leads to a 404... Why is that?
    System.out.println("Server started at http://localhost:" + port);
  }
}