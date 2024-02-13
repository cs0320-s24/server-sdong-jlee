package edu.brown.cs.student.main.Server;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import spark.Request;
import spark.Response;
import spark.Route;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Moshi.Builder;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import edu.brown.cs.student.main.Server.SearchHandler.SearchSuccessResponse;
import edu.brown.cs.student.main.Server.SearchHandler.UnableToReadFile;
import java.util.ArrayList;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewHandler implements Route {
  private CSVState csvState;

  public ViewHandler (CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    System.out.println(params);

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    // Check that file is loaded
    if (this.csvState.fileNameIsEmpty()) {
      return new FileNotLoadedResponse().serialize();
    }

    StringCreator stringCreator = new StringCreator();
    String file = this.csvState.getFileName();

    FileReader freader = null;
    try {
      freader = new FileReader("data/" + file);
    } catch (Exception e) {
      return new UnableToReadFile().serialize();
    }
    CSVParser<String> parser = new CSVParser<>(freader, stringCreator, this.csvState.getHasHeader());
    List<String> parsedFile = parser.parseCSV();
    // to add to response map
    List<List<String>> viewResult = new ArrayList<>();

    for (String row : parsedFile) {
      List<String> curList = new ArrayList<>();
      curList.add(row);
      viewResult.add(curList);
    }
    return new ViewSuccessResponse(viewResult).serialize();
  }

  /** Response object to send, when view is successful */
  public record ViewSuccessResponse(String response_type, List<List<String>> data) {
    public ViewSuccessResponse(List<List<String>> data) {
      this("success", data);
    }

    /** @return this response, serialized as Json */
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<ViewHandler.ViewSuccessResponse> adapter = moshi.adapter(
            ViewHandler.ViewSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }
  /** Response object to send, when a file has not been loaded before searching */
  public record FileNotLoadedResponse(String result) {
    public FileNotLoadedResponse() {
      this("error_bad_request: file must be loaded before viewing");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotLoadedResponse.class).toJson(this);
    }
  }

  /** Response object to send, when a file cannot be read */
  public record UnableToReadFile(String result) {
    public UnableToReadFile() {
      this("error_datasource: unable to read file");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ViewHandler.UnableToReadFile.class).toJson(this);
    }
  }
}


