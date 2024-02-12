package edu.brown.cs.student.main.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.Server.SearchHandler.SearchNoMatchFailureResponse;
import edu.brown.cs.student.main.Server.SearchHandler.SearchSuccessResponse;
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
      System.out.println("filepath empty");
      return new LoadFileEmptyFailureResponse().serialize();
    }

    File f = new File(filepath);
    if (!f.exists()) {
      System.out.println("file DNE");
      return new LoadFileDNEFailureResponse().serialize();
    }

    if (!filepath.startsWith("data/")) {
      System.out.println("outside direct");
      return new LoadFileOutsideDirectoryFailureResponse().serialize();
    }

    filepath = filepath.substring(5);
    this.csvState.setFileName(filepath);
    responseMap.put("result", "load success");
    System.out.println("filepath: " + filepath);
    return new LoadSuccessResponse(responseMap).serialize();
  }


  public record LoadSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public LoadSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<LoadHandler.LoadSuccessResponse> adapter = moshi.adapter(
            LoadHandler.LoadSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // For debugging purposes, show in the console _why_ this fails
        // Otherwise we'll just get an error 500 from the API in integration
        // testing.
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send if someone requested file that doesn't exist */
  public record LoadFileDNEFailureResponse(String error) {
    public LoadFileDNEFailureResponse() {
      this("File does not exist");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileDNEFailureResponse.class).toJson(this);
    }
  }

  public record LoadFileEmptyFailureResponse(String error) {
    public LoadFileEmptyFailureResponse() {
      this("Filepath empty, please specify filepath");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileEmptyFailureResponse.class).toJson(this);
    }
  }

  public record LoadFileOutsideDirectoryFailureResponse(String error) {
    public LoadFileOutsideDirectoryFailureResponse() {
      this("File outside data/ directory, please use file within data directory");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileOutsideDirectoryFailureResponse.class).toJson(this);
    }
  }
}
