package edu.brown.cs.student.main.ACS;

public class MockedACSAPISource implements ACSDatasource{

  private final ACSData constantData;

  public MockedACSAPISource(ACSData constantData) {
    this.constantData = constantData;
  }

  @Override
  public ACSData getPercentageBBAccess() {
    return constantData;
  }
}
