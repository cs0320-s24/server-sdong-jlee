package edu.brown.cs.student.main.Server;

import static spark.Spark.connect;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.DatasourceException;
import edu.brown.cs.student.main.Cache.ACSProxy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import spark.Request;
import spark.Response;
import spark.Route;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okio.Buffer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class BroadbandHandler implements Route {

  HashMap<String, String> stateCodesMap;

  public BroadbandHandler() {

  }

  private static HttpURLConnection connect(URL requestURL)
      throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(! (urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }

  private HashMap<String, String> getStateCodes() throws IOException, DatasourceException {
    URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
    HttpURLConnection clientConnection = connect(requestURL);

    Moshi moshi = new Moshi.Builder().build();
    System.out.println("got here 0");
    //JsonAdapter<StateCodeResponse> adapter = moshi.adapter(StateCodeResponse.class).nonNull();
    JsonAdapter<Object> adapter = moshi.adapter(Types.newParameterizedType(List.class, Types.newParameterizedType(List.class, String.class))).nonNull();

    List<List<String>> stateCodeList = (List<List<String>>) adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    //System.out.println(stateCodeList); // records are nice for giving auto toString

    clientConnection.disconnect();
    // Validity checks for response
//    if(body == null || body.properties() == null || body.properties().temperature() == null)
//      throw new DatasourceException("Malformed response from NWS");
//    if(body.properties().temperature().values().isEmpty())
//      throw new DatasourceException("Could not obtain temperature data from NWS");

    HashMap<String, String> stateCodesMap = new HashMap<>();

    assert stateCodeList != null;
    for (List<String> row: stateCodeList) {
      String state = row.get(0);
      String code = row.get(1);

      if (state.equals("NAME")) {
        continue;
      }
      stateCodesMap.put(state, code);
    }
    this.stateCodesMap = stateCodesMap;
    return stateCodesMap;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    System.out.println("sout");
    getStateCodes();
    return null;
  }

  public record StateCodeResponse(List<List<String>> stateCodes) {


  }


}
