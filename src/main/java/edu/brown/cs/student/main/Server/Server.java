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

    System.out.println("Server started at http://localhost:" + port);
  }

  /**
   * Main class where the server is created. Developers may change the server configuration to include a real or mocked
   * data source. There's also an option to include a proxy class for caching response results.
   * @param args
   * @throws DatasourceException
   * @throws IOException
   */
  public static void main(String[] args) throws DatasourceException, IOException {
    CSVState csvState = new CSVState();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    String dateTime = dateFormat.format(date);

    ACSData acsData = new ACSData("23");

    ACSDatasource mocked = new MockedACSAPISource(acsData);
    ACSDatasource real = new RealACSAPISource();

    Server server = new Server(csvState, new ACSProxy(mocked,10, 1));
  }
}
