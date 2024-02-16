package edu.brown.cs.student.main.ACS;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import okio.Buffer;

/**
 * Real data source for accessing the ACS API.
 */
public class RealACSAPISource implements ACSDatasource {
  static String dateTime;

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

  @Override
  public ACSData getPercentageBBAccess(String stateCode, String countyCode)
      throws IOException, DatasourceException {
    // TODO: figure out if it matters what date we put in
    URL requestURL =
        new URL(
            "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                + countyCode
                + "&in=state:"
                + stateCode);
    System.out.println(requestURL);
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<Object> adapter =
        moshi
            .adapter(
                Types.newParameterizedType(
                    List.class, Types.newParameterizedType(List.class, String.class)))
            .nonNull();

    List<List<String>> broadBandAccessList =
        (List<List<String>>)
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    System.out.println(broadBandAccessList); // records are nice for giving auto toString
    clientConnection.disconnect();
    // Validity checks for response
    if (broadBandAccessList == null) {
      throw new DatasourceException("Malformed response from ACSAPI");
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    dateTime = dateFormat.format(date);

    return new ACSData(broadBandAccessList.get(1).get(1));
  }

  @Override
  public String getDateTime() {
    return dateTime;
  }
}
