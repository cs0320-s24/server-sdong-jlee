package edu.brown.cs.student.CacheTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.beust.ah.A;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import edu.brown.cs.student.main.ACS.MockedACSAPISource;
import edu.brown.cs.student.main.ACS.RealACSAPISource;
import edu.brown.cs.student.main.Cache.ACSProxy;
import edu.brown.cs.student.main.Server.BroadbandHandler;
import edu.brown.cs.student.main.Server.MockServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestACSProxy {
  @BeforeAll
  public static void setup_before_everything() {
    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    // Setting port 0 will cause Spark to use an arbitrary available port.
    MockServer testServer = new MockServer();
    // Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setup() throws DatasourceException, IOException {
    ACSData acsData = new ACSData("23");
    ACSDatasource mocked = new MockedACSAPISource(acsData);
    ACSDatasource real = new RealACSAPISource();
    ACSProxy proxy = new ACSProxy(mocked, 10, 1);
    Spark.get("broadband", new BroadbandHandler(proxy));
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
  public void testCacheBasic()
      throws IOException, URISyntaxException, InterruptedException, DatasourceException {
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
    assertEquals("{percentage=23}", broadbandPercentageObject.toString());

    ACSDatasource real = new RealACSAPISource();
    Spark.get("broadband", new BroadbandHandler(real));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnectionCached = tryRequest("broadband?county=Kings%20County&state=California");
    assertEquals(200, clientConnectionCached.getResponseCode());
    BroadbandHandler.BroadbandSuccessResponse responseCached =
        moshi
            .adapter(BroadbandHandler.BroadbandSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionCached.getInputStream()));
    assert responseCached != null;
    Object broadbandPercentageObjectCached = responseCached.resultMap().get("Broadband Percentage");
    assertEquals("{percentage=23}", broadbandPercentageObjectCached.toString());

    clientConnection.disconnect();
    clientConnectionCached.disconnect();
  }

  @Test
  public void testCacheEviction()
      throws IOException, URISyntaxException, InterruptedException, DatasourceException, ExecutionException {
    ACSData acsData = new ACSData("23");
    ACSDatasource mocked = new MockedACSAPISource(acsData);
    ACSDatasource real = new RealACSAPISource();
    ACSProxy proxy = new ACSProxy(mocked, 5, 1);
    Spark.get("broadband", new BroadbandHandler(proxy));

    for (int i = 0; i < 10; i++) {
      proxy.getPercentageBBAccess("0" + i, "0" + i);
    }
    String state = "0";
    String county = "0";

    assertEquals(5, proxy.cache.size());
    assertEquals(5, proxy.cache.stats().evictionCount());
    System.out.println(proxy.cache.stats());
  }

  @Test
  public void testCacheHits()
      throws IOException, URISyntaxException, InterruptedException, DatasourceException, ExecutionException {
    ACSData acsData = new ACSData("23");
    ACSDatasource mocked = new MockedACSAPISource(acsData);
    ACSDatasource real = new RealACSAPISource();
    ACSProxy proxy = new ACSProxy(mocked, 5, 1);
    Spark.get("broadband", new BroadbandHandler(proxy));

    for (int i = 0; i < 5; i++) {
      proxy.getPercentageBBAccess("0" + i, "0" + i);
    }
    for (int i = 0; i < 5; i++) {
      proxy.getPercentageBBAccess("0" + i, "0" + i);
    }
    String state = "0";
    String county = "0";
    System.out.println(proxy.cache.stats());
    assertEquals(5, proxy.cache.stats().hitCount());
    assertEquals(5, proxy.cache.stats().missCount());
  }
}
