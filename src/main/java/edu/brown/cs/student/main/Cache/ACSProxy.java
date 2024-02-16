package edu.brown.cs.student.main.Cache;

import edu.brown.cs.student.main.ACS.ACSData;
import edu.brown.cs.student.main.ACS.ACSDatasource;
import edu.brown.cs.student.main.ACS.DatasourceException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ACSProxy implements ACSDatasource {

  private ACSDatasource acsDatasource;
  public final LoadingCache<List<String>, ACSData> cache;
  //TODO add user parameters to constructor
  public ACSProxy(ACSDatasource acsDatasource, Integer maxSize, Integer expirationTime) {

    this.acsDatasource = acsDatasource;

    this.cache = CacheBuilder.newBuilder()
        .maximumSize(maxSize)
        // How long should entries remain in the cache?
        .expireAfterWrite(expirationTime, TimeUnit.MINUTES)

        // Keep statistical info around for profiling purposes
        .recordStats()
        .build(
            new CacheLoader<>() {
              @NotNull
              public ACSData load(@NotNull List<String> stateAndCountyCode)
                throws DatasourceException, IOException, ExecutionException {
                String stateCode = stateAndCountyCode.get(0);
                String countyCode = stateAndCountyCode.get(1);
                return acsDatasource.getPercentageBBAccess(stateCode, countyCode);
              }
            });
  }

  @Override
  public ACSData getPercentageBBAccess(String stateCode, String countyCode)
      throws IOException, DatasourceException, ExecutionException {
    List<String> stateAndCountyCode = new ArrayList<>(Arrays.asList(stateCode, countyCode));
    return cache.get(stateAndCountyCode);
  }

  @Override
  public String getDateTime() {
    return this.acsDatasource.getDateTime();
  }
}
