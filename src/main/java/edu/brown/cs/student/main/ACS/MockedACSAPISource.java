package edu.brown.cs.student.main.ACS;

/**
 * A mock data source to use in place of the RealACSAPISource. This class allows for testing handler functionality
 * without actually calling the ACS API.
 */
public class MockedACSAPISource implements ACSDatasource {
  private final ACSData constantData;

  public MockedACSAPISource(ACSData constantData) {
    this.constantData = constantData;
  }

  @Override
  public ACSData getPercentageBBAccess(String stateCode, String countyCode) {
    return constantData;
  }

  @Override
  public String getDateTime() {
    return "today and now";
  }
}
