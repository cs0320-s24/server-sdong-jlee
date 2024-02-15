package edu.brown.cs.student.HandlerTests;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.Server.*;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSearchHandler {
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
    public void setup() {
        CSVState csvState = new CSVState();
        Spark.get("loadcsv", new LoadHandler(csvState));
        Spark.get("viewcsv", new ViewHandler(csvState));
        Spark.get("searchcsv", new SearchHandler(csvState));
        Spark.init();
        Spark.awaitInitialization(); // don't continue until the server is listening
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on both endpoints after each test
        Spark.unmap("loadcsv");
        Spark.unmap("viewcsv");
        Spark.unmap("searchcsv");
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
    public void testWorkingSearch () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, clientConnection.getResponseCode());
        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=City/Town&searchItem=Bristol");
        assertEquals(200, searchConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        LoadHandler.LoadSuccessResponse response =
                moshi
                        .adapter(LoadHandler.LoadSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response != null;
        String result = response.result();
        assertEquals("success", result);

        searchConnection.disconnect();
    }

    @Test
    public void testNoSearchMatch () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, clientConnection.getResponseCode());
        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=City/Town&searchItem=Madeuptown");
        assertEquals(200, searchConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        LoadHandler.LoadSuccessResponse response =
                moshi
                        .adapter(LoadHandler.LoadSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response != null;
        String result = response.result();
        assertEquals("error_no_match", result);

        searchConnection.disconnect();
    }

    @Test
    public void testFileNotLoaded () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=City/Town&searchItem=Madeuptown");
        assertEquals(200, searchConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        LoadHandler.LoadSuccessResponse response =
                moshi
                        .adapter(LoadHandler.LoadSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response != null;
        String result = response.result();
        assertEquals("error_bad_request", result);

        searchConnection.disconnect();
    }


}
