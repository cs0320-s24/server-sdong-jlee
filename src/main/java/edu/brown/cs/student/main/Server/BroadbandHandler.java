package edu.brown.cs.student.main.Server;
import static spark.Spark.connect;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import spark.Request;
import spark.Response;
import spark.Route;
import com.squareup.moshi.JsonAdapter;
import okio.Buffer;
import java.net.URLConnection;
import java.util.*;

/**
 * A handler class for the broadband endpoint. Takes in an ACSDatasource to use for getting broadband percentages.
 * Makes a request to the ACS API to populate a stateCodesMap and makes an additional query to find the corresponding
 * county code to a user provided county.
 */
public class BroadbandHandler implements Route {
  private final ACSDatasource datasource;
  HashMap<String, String> stateCodesMap;
  private static String county;
  private static String state;

  public BroadbandHandler(ACSDatasource datasource) throws DatasourceException, IOException {
    this.datasource = datasource;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    county = request.queryParams("county");
    state = request.queryParams("state");
    if (this.stateCodesMap == null) {
      getStateCodes();
    }

    if (county == null || state == null || county.isEmpty() || state.isEmpty()) {
     // return new noHasHeaderInputParam().serialize();
      return new missingInputParam().serialize();
    }

    if (!Character.isUpperCase(county.charAt(0)) || !Character.isUpperCase(state.charAt(0))) {
      return new invalidInputParam().serialize();
    }

    // TODO add more checks for proper formatting of inputs like capital words etc, also how to deal with spaces for county?
    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();
    // gets state and county codes to pass into the datasource get percentage broadband access
    String stateCode = this.stateCodesMap.get(state);
    String countyCode = this.getCountyCode(stateCode, county + ", " + state);
    try {
      ACSData percentage = this.datasource.getPercentageBBAccess(stateCode, countyCode);
      responseMap.put("parameters", List.of(county, state));
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      //TODO check if this is right way to add date and time
      Date date = new Date();
      String dateTime = dateFormat.format(date);
      responseMap.put("date/time", dateTime);
      responseMap.put("Broadband Percentage", percentage);
      return new BroadbandSuccessResponse(responseMap).serialize();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
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

  /**
   * Method for populating stateCodesMap. Calls the ACS API and populates a map from String state : String code
   * @throws IOException
   * @throws DatasourceException
   */
  private void getStateCodes() throws IOException, DatasourceException {
    URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
    HttpURLConnection clientConnection = connect(requestURL);

    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<Object> adapter = moshi.adapter(Types.newParameterizedType(List.class, Types.newParameterizedType(List.class, String.class))).nonNull();
    List<List<String>> stateCodeList = (List<List<String>>) adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    // Validity checks for response
    if(stateCodeList == null)
      throw new DatasourceException("Malformed response from ACS");

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
  }

  //    countyName must be in format "county name, state name" ??????

  /**
   * Method where given a user inputted county name, it queries the ACS API and finds the corresponding county code.
   * @param stateCode
   * @param countyName
   * @return county code - the String representation of a county code
   * @throws IOException
   * @throws DatasourceException
   */
  private String getCountyCode(String stateCode, String countyName) throws IOException, DatasourceException {
    URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
    HttpURLConnection clientConnection = connect(requestURL);

    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<Object> adapter = moshi.adapter(Types.newParameterizedType(List.class, Types.newParameterizedType(List.class, String.class))).nonNull();

    List<List<String>> countyCodesList = (List<List<String>>) adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    String countyCode;

    for (List<String> row: countyCodesList) {
      String county = row.get(0);
      if (county.equals("NAME")) {
        continue;
      }
      countyCode = row.get(2);

      if (county.equals(countyName)) {
        return countyCode;
      }
    }
//    if we never find a matching county in the state
    return null;
  }

  public record BroadbandSuccessResponse(String result, Map<String, Object> resultMap) {
    public BroadbandSuccessResponse(Map<String, Object> resultMap) {
      this("success", resultMap);
    }
    /** @return this response, serialized as Json */
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<BroadbandHandler.BroadbandSuccessResponse> adapter = moshi.adapter(
            BroadbandHandler.BroadbandSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record missingInputParam(String result) {
    public missingInputParam() {
      this("error_bad_request: missing either county param, state param, or both");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandHandler.missingInputParam.class).toJson(this);
    }
  }
  public record invalidInputParam(String result) {
    public invalidInputParam() {
      this("error_bad_request: ensure capitalization's on " + county + " or " + state);
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandHandler.invalidInputParam.class).toJson(this);
    }
  }
}
