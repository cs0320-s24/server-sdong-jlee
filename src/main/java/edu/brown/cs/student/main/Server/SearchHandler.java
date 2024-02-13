package edu.brown.cs.student.main.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchHandler implements Route {
  private CSVState csvState;

  public SearchHandler(CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    String columnIdentifier = request.queryParams("columnIdentifier");
    String searchItem = request.queryParams("searchItem");

    Set<String> returnParams = new HashSet<>();
    returnParams.add(searchItem);

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
    Search search = new Search(stringCreator, parser, file);
    List<String> searchResult;

    if (columnIdentifier == null) {
      searchResult = search.searchFile(searchItem);
    } else {
      searchResult = search.searchFile(searchItem, columnIdentifier, this.csvState.getHasHeader());
      returnParams.add(columnIdentifier);
    }

    // No matches found
    if (searchResult.contains("Unable to find: '" + searchItem + "' in file")) {
      return new SearchNoMatchFailureResponse().serialize();
    }

    // Successful search
    responseMap.put("Matching Rows", searchResult);
    return new SearchSuccessResponse(returnParams, responseMap).serialize();
  }

  /** Response object to send, when search is successful */
  public record SearchSuccessResponse(String result, Set<String> parameters, Map<String, Object> responseMap) {
    public SearchSuccessResponse(Set<String> params, Map<String, Object> responseMap) {
      this("success", params, responseMap);
    }
    /** @return this response, serialized as Json */
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<SearchSuccessResponse> adapter = moshi.adapter(SearchSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send, when search fails to find a match */
  public record SearchNoMatchFailureResponse(String result) {
    public SearchNoMatchFailureResponse() {
      this("error_no_match");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchNoMatchFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send, when a file has not been loaded before searching */
  public record FileNotLoadedResponse(String result) {
    public FileNotLoadedResponse() {
      this("error_bad_request");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotLoadedResponse.class).toJson(this);
    }
  }
  //TODO: is this necessary here?
  /** Response object to send, when a file cannot be read */
  public record UnableToReadFile(String result) {
    public UnableToReadFile() {
      this(" error_datasource");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(UnableToReadFile.class).toJson(this);
    }
  }

}
