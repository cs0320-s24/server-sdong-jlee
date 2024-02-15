package edu.brown.cs.student.main.Cache;

import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import edu.brown.cs.student.main.Server.BroadbandHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingDatasurce implements Route, ACSDatasource {

  // proxy pattern for caching
  private BroadbandHandler broadbandHandler;
  //TODO add user parameters to constructor



  public CachingDatasurce(BroadbandHandler broadbandHandler) {
    // wraps broadbandHandler
    this.broadbandHandler = broadbandHandler;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    LoadingCache<String, List<String>> graphs = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, List<String>>>() {
//                      public Graph load(Key key) throws AnyException {
//                        return createExpensiveGraph(key);
                      }
                    }

    @Override
    public ACSData getPercentageBBAccess(String stateCode, String countyCode) throws IOException, DatasourceException {
        return null;
    });



    return null;
  }

}
