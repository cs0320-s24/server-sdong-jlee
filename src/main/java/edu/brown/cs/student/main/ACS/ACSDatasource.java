package edu.brown.cs.student.main.ACS;

import java.io.IOException;
import java.net.MalformedURLException;

public interface ACSDatasource {


   ACSData getPercentageBBAccess(String stateCode, String countyCode)
       throws IOException, DatasourceException;

}
