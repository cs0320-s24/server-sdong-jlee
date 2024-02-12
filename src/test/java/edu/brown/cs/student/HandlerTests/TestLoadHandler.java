package edu.brown.cs.student.HandlerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.Server.CSVState;
import edu.brown.cs.student.main.Server.LoadHandler;
import edu.brown.cs.student.main.Server.TestServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestLoadHandler {
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
  public void setup() {
    Spark.get("loadcsv", new LoadHandler(new CSVState()));
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints after each test
    Spark.unmap("loadcsv");
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
  public void workingFile() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    LoadHandler.LoadSuccessResponse response =
        moshi
            .adapter(LoadHandler.LoadSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    String result = (String) response.responseMap().get("result");
    assertEquals("load success", result);

    clientConnection.disconnect();
  }
  @Test
  public void fileOutsideDirectory() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=src/main/java/edu/brown/cs/student/main/Server/BroadbandHandler.java");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    LoadHandler.LoadFileOutsideDirectoryFailureResponse response =
        moshi
            .adapter(LoadHandler.LoadFileOutsideDirectoryFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    String result = response.error();
    assertEquals("File outside data/ directory, please use file within data directory", result);
    clientConnection.disconnect();
  }
  @Test
  public void fileDNE() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/census/aslkdjfaklsdf.csv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    LoadHandler.LoadFileDNEFailureResponse response =
        moshi
            .adapter(LoadHandler.LoadFileDNEFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    String result = response.error();
    assertEquals("File does not exist", result);
    clientConnection.disconnect();

  }
  @Test
  public void filepathEmpty() throws IOException, URISyntaxException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    LoadHandler.LoadFileEmptyFailureResponse response =
        moshi
            .adapter(LoadHandler.LoadFileEmptyFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    String result = response.error();
    assertEquals("Filepath empty, please specify filepath", result);
    clientConnection.disconnect();
  }
}
