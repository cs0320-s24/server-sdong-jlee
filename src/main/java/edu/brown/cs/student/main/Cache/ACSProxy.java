package edu.brown.cs.student.main.Cache;

import edu.brown.cs.student.main.Server.BroadbandHandler;
import spark.Request;
import spark.Response;
import spark.Route;

public class ACSProxy implements Route {

  private BroadbandHandler broadbandHandler;
  //TODO add user parameters to constructor
  public ACSProxy(BroadbandHandler broadbandHandler) {
    this.broadbandHandler = broadbandHandler;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    return null;
  }
}
