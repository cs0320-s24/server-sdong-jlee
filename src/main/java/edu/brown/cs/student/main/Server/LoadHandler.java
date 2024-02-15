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

/**
 * A handler class for the loadcsv endpoint. Takes in two parameters, filepath - a filepath starting with "data/", and
 * hasHeader - an argument to searchFile in the Search class.Attempts to update the CSVState with the user-provided CSV
 * filename. Then, in preparation for viewcsv and searchcsv endpoints, the handler also sets the CSVState hasHeader field.
 */
public class LoadHandler implements Route {
  private CSVState csvState;
  private static String hasHeaderString;
  private static String filepath;

  public LoadHandler(CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    Set<String> params = request.queryParams();
    filepath = request.queryParams("filepath");
    hasHeaderString = request.queryParams("hasHeader");

    // need this check at top to ensure we don't get error code 500
    if (hasHeaderString == null) {
      return new NoHasHeaderInput().serialize();
    }

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    if (filepath.isEmpty()) {
      return new LoadFileEmptyFailureResponse().serialize();
    }

    File f = new File(filepath);
    if (!f.exists()) {
      return new LoadFileDNEFailureResponse().serialize();
    }

    if (!filepath.startsWith("data/")) {
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

    // Loads the filepath without "data/" included
    filepath = filepath.substring(5);
    this.csvState.setFileName(filepath);
    responseMap.put("result", "load success");
    return new LoadSuccessResponse(filepath).serialize();
  }

  public record LoadSuccessResponse(String result, String filepath) {
    public LoadSuccessResponse(String filepath) {
      this("success", filepath);
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

  /** Response object to send, when filepath does not exist */
  public record LoadFileDNEFailureResponse(String result) {
    public LoadFileDNEFailureResponse() {
      this("error_datasource: " + filepath + " does not exist");
    }

    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileDNEFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send, when no filepath is provided */
  public record LoadFileEmptyFailureResponse(String result ) {
    public LoadFileEmptyFailureResponse() {
      this("error_bad_request: file path parameter empty");
    }

    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileEmptyFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send, when file to load is outside the /data directory */
  public record LoadFileOutsideDirectoryFailureResponse(String result) {
    public LoadFileOutsideDirectoryFailureResponse() {
      this("error_datasource");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.LoadFileOutsideDirectoryFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send, when hasHeader parameter is not true or false */
  public record InvalidHasHeaderInput(String result) {
    public InvalidHasHeaderInput() {
      this("error_bad_request: " + "'"+hasHeaderString+"'" + " not equal to true or false");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.InvalidHasHeaderInput.class).toJson(this);
    }
  }

  /** Response object to send, when a no hasHeader parameter is entered */
  public record NoHasHeaderInput(String result) {
    public NoHasHeaderInput() {
      this("error_bad_request: loadcsv endpoint requires a hasHeader parameter");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadHandler.NoHasHeaderInput.class).toJson(this);
    }
  }
}
