package edu.brown.cs.student.main.ACS;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Interface for data sources. Contains a getPercentageBBAccess method for retrieving the percentage of broadband
 * access. Also contains a getDateTime method for getting the date/time when the broadband data is retrieved.
 */
public interface ACSDatasource {

  ACSData getPercentageBBAccess(String stateCode, String countyCode)
      throws IOException, DatasourceException, ExecutionException;

  String getDateTime();
}
