package edu.brown.cs.student.main.ACS;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import okio.Buffer;

public class RealACSAPISource implements ACSDatasource{

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

  @Override
  public ACSData getPercentageBBAccess(String stateCode, String countyCode)
      throws IOException, DatasourceException {
    //TODO: figure out if it matters what date we put in
    URL requestURL = new URL("https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"+countyCode+"&in=state:"+stateCode);
    System.out.println(requestURL);
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<Object> adapter = moshi.adapter(
        Types.newParameterizedType(List.class, Types.newParameterizedType(List.class, String.class))).nonNull();

    List<List<String>> broadBandAccessList = (List<List<String>>) adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    System.out.println(broadBandAccessList); // records are nice for giving auto toString

    clientConnection.disconnect();
    // Validity checks for response
//    if(body == null || body.properties() == null || body.properties().temperature() == null)
//      throw new DatasourceException("Malformed response from NWS");
//    if(body.properties().temperature().values().isEmpty())
//      throw new DatasourceException("Could not obtain temperature data from NWS");

    assert broadBandAccessList != null;
    return new ACSData(broadBandAccessList.get(1).get(1));
  }
}
