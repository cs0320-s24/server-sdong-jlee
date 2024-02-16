package edu.brown.cs.student.main.Server;

import static spark.Spark.connect;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.HashMap;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A handler class for the broadband endpoint which queries two parameters, county - a county name,
 * state - a state name. The class takes in an ACSDatasource to use for getting broadband
 * percentages.Makes a request to the ACS API to populate a stateCodesMap and makes an additional
 * query to find the corresponding county code to a user provided county.
 */
public class BroadbandHandler implements Route {
  private final ACSDatasource datasource;
  private HashMap<String, String> stateCodesMap;
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
      return new missingInputParam().serialize();
    }

    if (!Character.isUpperCase(county.charAt(0)) || !Character.isUpperCase(state.charAt(0))) {
      return new invalidInputParam().serialize();
    }

    if (!county.contains(" ")) {
      return new invalidCounty().serialize();
    }
    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    // Gets state and county codes to pass into the datasource
    String stateCode = this.stateCodesMap.get(state);
    if (stateCode == null) {
      return new stateNotFound().serialize();
    }

    String countyCode = this.getCountyCode(stateCode, county + ", " + state);
    if (countyCode == null) {
      return new countyNotFound().serialize();
    }

    try {
      ACSData acsData = this.datasource.getPercentageBBAccess(stateCode, countyCode);
      String dateTime = this.datasource.getDateTime();
      responseMap.put("parameters", List.of(county, state));
      responseMap.put("Time of API Call to ACS", dateTime);
      responseMap.put("Broadband Percentage", acsData);
      return new BroadbandSuccessResponse(responseMap).serialize();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200)
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  /**
   * Method for populating stateCodesMap. Calls the ACS API and populates a map from String state :
   * String code
   *
   * @throws IOException
   * @throws DatasourceException
   */
  private void getStateCodes() throws IOException, DatasourceException {
    URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Object> adapter =
        moshi
            .adapter(
                Types.newParameterizedType(
                    List.class, Types.newParameterizedType(List.class, String.class)))
            .nonNull();
    List<List<String>> stateCodeList =
        (List<List<String>>)
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();

    // Validity checks for response
    if (stateCodeList == null) throw new DatasourceException("Malformed response from ACS");

    HashMap<String, String> stateCodesMap = new HashMap<>();

    assert stateCodeList != null;
    for (List<String> row : stateCodeList) {
      String state = row.get(0);
      String code = row.get(1);

      if (state.equals("NAME")) {
        continue;
      }
      stateCodesMap.put(state, code);
    }

    this.stateCodesMap = stateCodesMap;
  }

  /**
   * Method where given a user inputted county name, it queries the ACS API and finds the
   * corresponding county code.
   *
   * @param stateCode
   * @param countyName
   * @return county code - the String representation of a county code
   * @throws IOException
   * @throws DatasourceException
   */
  private String getCountyCode(String stateCode, String countyName)
      throws IOException, DatasourceException {
    URL requestURL =
        new URL(
            "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Object> adapter =
        moshi
            .adapter(
                Types.newParameterizedType(
                    List.class, Types.newParameterizedType(List.class, String.class)))
            .nonNull();

    List<List<String>> countyCodesList =
        (List<List<String>>)
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    String countyCode;

    for (List<String> row : countyCodesList) {
      String county = row.get(0);
      if (county.equals("NAME")) {
        continue;
      }
      countyCode = row.get(2);

      if (county.equals(countyName)) {
        return countyCode;
      }
    }

    return null;
  }

  /**
   * Response object to send, when broadband percentage information is successfully retrieved from
   * ACS API
   */
  public record BroadbandSuccessResponse(String result, Map<String, Object> resultMap) {
    public BroadbandSuccessResponse(Map<String, Object> resultMap) {
      this("success", resultMap);
    }
    /** @return this response, serialized as Json */
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<BroadbandHandler.BroadbandSuccessResponse> adapter =
            moshi.adapter(BroadbandHandler.BroadbandSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send, when county or state parameter is empty */
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

  /** Response object to send, when a county or state parameter is invalid */
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

  /** Response object to send, when a county or state parameter is invalid */
  public record invalidCounty(String result) {
    public invalidCounty() {
      this("error_bad_request: ensure space in county name");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandHandler.invalidCounty.class).toJson(this);
    }
  }

  /** Response object to send, when a county or state parameter is invalid */
  public record stateNotFound(String result) {
    public stateNotFound() {
      this(
          "error_bad_request: state: "
              + state
              + " not found, check formatting or state is valid state");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandHandler.stateNotFound.class).toJson(this);
    }
  }
  /** Response object to send, when a county or state parameter is invalid */
  public record countyNotFound(String result) {
    public countyNotFound() {
      this(
          "error_bad_request: county: "
              + county
              + " not found, check formatting or county is valid county");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandHandler.countyNotFound.class).toJson(this);
    }
  }
}
