package edu.brown.cs.student.main.ACS;

import java.util.Date;

public class MockedACSAPISource implements ACSDatasource{
  private final ACSData constantData;

  public MockedACSAPISource(ACSData constantData) {
    this.constantData = constantData;
  }

  @Override
  public ACSData getPercentageBBAccess(String stateCode, String countyCode) {
    return constantData;
  }

}
