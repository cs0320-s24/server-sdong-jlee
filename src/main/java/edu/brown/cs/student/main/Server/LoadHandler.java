package edu.brown.cs.student.main.Server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadHandler implements Route {

  private CSVState csvState;
  public LoadHandler(CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    System.out.println(params);
    String filepath = request.queryParams("filepath");
    System.out.println(filepath);

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    if (filepath.isEmpty()) {
      responseMap.put("result", "Exception: Filepath empty");
      return responseMap;
    }

    File f = new File(filepath);
    if (!f.exists()) {
      responseMap.put("result", "Exception: File does not exist");
      return responseMap;
    }

    if (!filepath.startsWith("data/")) {
      responseMap.put("result", "Exception: File not in data directory");
      return responseMap;
    }

    filepath = filepath.substring(5);
    this.csvState.setFileName(filepath);
    responseMap.put("result", "load success");
    System.out.println("filepath: " + filepath);
    return responseMap;
  }
}
