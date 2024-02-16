package edu.brown.cs.student.HandlerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.Server.CSVState;
import edu.brown.cs.student.main.Server.LoadHandler;
import edu.brown.cs.student.main.Server.MockServer;
import edu.brown.cs.student.main.Server.ViewHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestViewHandler {
  @BeforeAll
  public static void setup_before_everything() {
    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    MockServer testServer = new MockServer();
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setup() {
    CSVState csvState = new CSVState();
    Spark.get("loadcsv", new LoadHandler(csvState));
    Spark.get("viewcsv", new ViewHandler(csvState));
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("loadcsv");
    Spark.unmap("viewcsv");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void workingView() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnectionView = tryRequest("viewcsv");
    assertEquals(200, clientConnectionView.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    ViewHandler.ViewSuccessResponse response =
        moshi
            .adapter(ViewHandler.ViewSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionView.getInputStream()));

    assert response != null;
    List<List<String>> result = response.data();
    assertEquals(List.of("Rhode Island,\"74,489.00\",\"95,198.00\",\"39,603.00\""), result.get(0));
    clientConnection.disconnect();
  }

  @Test
  public void noLoadBeforeView() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    ViewHandler.FileNotLoadedResponse response =
        moshi
            .adapter(ViewHandler.FileNotLoadedResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    String result = response.result();
    assertEquals("error_bad_request: file must be loaded before viewing", result);
    clientConnection.disconnect();
  }

  @Test
  public void newLoadBeforeView() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnectionLoad =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv");
    assertEquals(200, clientConnectionLoad.getResponseCode());
    HttpURLConnection clientConnectionView = tryRequest("viewcsv");
    assertEquals(200, clientConnectionView.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    ViewHandler.ViewSuccessResponse response =
        moshi
            .adapter(ViewHandler.ViewSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionView.getInputStream()));

    assert response != null;
    List<List<String>> result = response.data();
    assertEquals(List.of("Rhode Island,\"74,489.00\",\"95,198.00\",\"39,603.00\""), result.get(0));
    clientConnection.disconnect();
  }
}
