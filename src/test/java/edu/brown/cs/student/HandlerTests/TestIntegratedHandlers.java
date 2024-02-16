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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIntegratedHandlers {
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
    public void testTwoFilesLoadedSearch () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection file1Connection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, file1Connection.getResponseCode());

        HttpURLConnection file2Connection = tryRequest("loadcsv?filepath=data/stars/stardata.csv&hasHeader=true");
        assertEquals(200, file2Connection.getResponseCode());

        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=StarID&searchItem=12");
        assertEquals(200, searchConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        SearchHandler.SearchSuccessResponse response =
                moshi
                        .adapter(SearchHandler.SearchSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response != null;
        String result = response.result();
        assertEquals("success", result);
        searchConnection.disconnect();
    }

    @Test
    public void testTwoFilesLoadedView () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection file2Connection = tryRequest("loadcsv?filepath=data/stars/stardata.csv&hasHeader=true");
        assertEquals(200, file2Connection.getResponseCode());

        HttpURLConnection file1Connection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, file1Connection.getResponseCode());

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
        clientConnectionView.disconnect();
    }


    @Test
    public void testViewSearch () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection file1Connection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, file1Connection.getResponseCode());

        HttpURLConnection clientConnectionView = tryRequest("viewcsv");
        assertEquals(200, clientConnectionView.getResponseCode());

        Moshi moshi1 = new Moshi.Builder().build();
        ViewHandler.ViewSuccessResponse response1 =
                moshi1
                        .adapter(ViewHandler.ViewSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(clientConnectionView.getInputStream()));

        assert response1 != null;
        List<List<String>> result1 = response1.data();
        assertEquals(List.of("Rhode Island,\"74,489.00\",\"95,198.00\",\"39,603.00\""), result1.get(0));

        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=City/Town&searchItem=Bristol");
        assertEquals(200, searchConnection.getResponseCode());

        Moshi moshi = new Moshi.Builder().build();
        SearchHandler.SearchSuccessResponse response =
                moshi
                        .adapter(SearchHandler.SearchSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response != null;
        List<List<String>> result = response.data();
        assertEquals(List.of("Bristol,\"80,727.00\",\"115,740.00\",\"42,658.00\"\n"), result.get(0));
        clientConnectionView.disconnect();
    }


    @Test
    public void testSearchView () throws IOException, URISyntaxException, InterruptedException {
        HttpURLConnection file1Connection = tryRequest("loadcsv?filepath=data/RITownIncome/RI.csv&hasHeader=true");
        assertEquals(200, file1Connection.getResponseCode());

        HttpURLConnection searchConnection = tryRequest("searchcsv?columnIdentifier=City/Town&searchItem=Bristol");
        assertEquals(200, searchConnection.getResponseCode());


        Moshi moshi1 = new Moshi.Builder().build();
        SearchHandler.SearchSuccessResponse response1 =
                moshi1
                        .adapter(SearchHandler.SearchSuccessResponse.class)
                        .fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
        assert response1 != null;
        List<List<String>> result1 = response1.data();
        assertEquals(List.of("Bristol,\"80,727.00\",\"115,740.00\",\"42,658.00\"\n"), result1.get(0));

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
        clientConnectionView.disconnect();
    }
}
