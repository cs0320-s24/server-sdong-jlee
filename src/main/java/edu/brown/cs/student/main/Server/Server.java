package edu.brown.cs.student.main.Server;

import static spark.Spark.after;

import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import edu.brown.cs.student.main.ACS.MockedACSAPISource;
import edu.brown.cs.student.main.ACS.RealACSAPISource;
import edu.brown.cs.student.main.Cache.ACSProxy;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import spark.Spark;

/**
 * Class representing a server. Begin the server by running this class and navigating to the desired
 * endpoints.
 */
public class Server {

  // constructor
  // TODO params - fake data class, actual retrieval process class, cache or no cache (specified in
  // main),
  public Server(CSVState csvState, ACSDatasource datasource)
      throws DatasourceException, IOException {
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
    Spark.get("broadband", new BroadbandHandler(datasource));

    Spark.init();
    Spark.awaitInitialization();

    // Notice this link alone leads to a 404... Why is that?
    System.out.println("Server started at http://localhost:" + port);
  }

  public static void main(String[] args) throws DatasourceException, IOException {
    CSVState csvState = new CSVState();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    String dateTime = dateFormat.format(date);

    ACSData acsData = new ACSData("23");

    ACSDatasource mocked = new MockedACSAPISource(acsData);
    ACSDatasource real = new RealACSAPISource();

    Server server = new Server(csvState, new ACSProxy(real, 10, 1));
  }
}
// /loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true
// /searchcsv?columnIdentifier=City/Town&searchItem=Bristol
// http://localhost:3232/loadcsv?filepath=data/RITownIncome/RI.csv/&hasHeader=true
// http://localhost:3232/searchcsv?hasHeader=true&searchItem=Barrington

// date/time, javdoc, testing, readne
