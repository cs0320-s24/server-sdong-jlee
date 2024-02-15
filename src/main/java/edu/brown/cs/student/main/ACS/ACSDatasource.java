package edu.brown.cs.student.main.ACS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public interface ACSDatasource {

   ACSData getPercentageBBAccess(String stateCode, String countyCode)
           throws IOException, DatasourceException, ExecutionException;

}
