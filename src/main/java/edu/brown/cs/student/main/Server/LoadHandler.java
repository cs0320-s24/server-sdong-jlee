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
    String filepath = request.queryParams("filepath");
    String hasHeaderString = request.queryParams("hasHeader");

    System.out.println(params);
    System.out.println(filepath);
    System.out.println(hasHeaderString);
    // need this check at top to ensure we don't get error code 500
    if (hasHeaderString == null) {
      return new NoHasHeaderInput().serialize();
    }

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

    switch (hasHeaderString) {
      case "true":
        this.csvState.setHasHeader(true);
        break;
      case "false":
        this.csvState.setHasHeader(false);
        break;
      case "":
        return new NoHasHeaderInput().serialize();
      default:
        return new InvalidHasHeaderInput().serialize();
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
    /** @return this response, serialized as Json */
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<LoadHandler.LoadSuccessResponse> adapter = moshi.adapter(
            LoadHandler.LoadSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record LoadFileDNEFailureResponse(String error_datasource) {
    public LoadFileDNEFailureResponse() {
      this("File does not exist");
    }

    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileDNEFailureResponse.class).toJson(this);
    }
  }

  public record LoadFileEmptyFailureResponse(String error_bad_request) {
    public LoadFileEmptyFailureResponse() {
      this("Filepath empty, please specify filepath");
    }

    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileEmptyFailureResponse.class).toJson(this);
    }
  }

  public record LoadFileOutsideDirectoryFailureResponse(String error_datasource) {
    public LoadFileOutsideDirectoryFailureResponse() {
      this("File outside data/ directory, please use file within data directory");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileOutsideDirectoryFailureResponse.class).toJson(this);
    }
  }

  public record NoHasHeaderInput(String error_bad_request) {
    public NoHasHeaderInput() {
      this("Exception: No value for hasHeader");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.NoHasHeaderInput.class).toJson(this);
    }
  }

  public record InvalidHasHeaderInput(String error_bad_request) {
    public InvalidHasHeaderInput() {
      this("Exception: Invalid input. Input true or false");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.InvalidHasHeaderInput.class).toJson(this);
    }
  }
}
