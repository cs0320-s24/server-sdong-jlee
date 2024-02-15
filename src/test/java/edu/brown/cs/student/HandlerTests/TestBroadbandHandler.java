package edu.brown.cs.student.HandlerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beust.ah.A;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.DatasourceException;
import edu.brown.cs.student.main.ACS.MockedACSAPISource;
import edu.brown.cs.student.main.Server.BroadbandHandler;
import edu.brown.cs.student.main.Server.CSVState;
import edu.brown.cs.student.main.Server.LoadHandler;
import edu.brown.cs.student.main.Server.TestServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestBroadbandHandler {
  @BeforeAll
  public static void setup_before_everything() {
    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    // Setting port 0 will cause Spark to use an arbitrary available port.
    TestServer testServer = new TestServer();
    // Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setup() throws DatasourceException, IOException {
    Spark.get("broadband", new BroadbandHandler(new MockedACSAPISource(new ACSData("20"))));
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints after each test
    Spark.unmap("broadband");
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
    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void workingBroadband() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("broadband?county=Kings%20County&state=California");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.BroadbandSuccessResponse response =
        moshi
            .adapter(BroadbandHandler.BroadbandSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assert response != null;
    String result = response.result();
    Object broadbandPercentageObject = response.resultMap().get("Broadband Percentage");
    assertEquals("{percentage=20}", broadbandPercentageObject.toString());
    clientConnection.disconnect();
  }

  @Test
  public void paramEmpty() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("broadband?county=Kings%20County&state=");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.missingInputParam response =
        moshi
            .adapter(BroadbandHandler.missingInputParam.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assert response != null;
    String result = response.result();
    System.out.println(result);
    //assertEquals("error_bad_request: missing either county param, state param, or both", result);
    clientConnection.disconnect();

  }

  @Test
  public void paramNoCap() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("broadband?county=kings%20County&state=California");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.invalidInputParam response =
        moshi
            .adapter(BroadbandHandler.invalidInputParam.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assert response != null;
    String result = response.result();
    System.out.println(result);
    assertEquals("error_bad_request: ensure capitalization's on kings County or California", result);
    clientConnection.disconnect();
  }

}
