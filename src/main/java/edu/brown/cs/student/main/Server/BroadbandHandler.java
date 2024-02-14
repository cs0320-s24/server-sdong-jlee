package edu.brown.cs.student.main.Server;

import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.Cache.ACSProxy;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  public BroadbandHandler(ACSDatasource acsDatasource) {

  }

  public BroadbandHandler(ACSDatasource acsDatasource, ACSProxy acsProxy) {

  }


  @Override
  public Object handle(Request request, Response response) throws Exception {
    return null;
  }
}
