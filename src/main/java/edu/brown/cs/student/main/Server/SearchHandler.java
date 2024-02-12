package edu.brown.cs.student.main.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchHandler implements Route {
  private CSVState csvState;
  private boolean hasHeader;

  public SearchHandler(CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    String hasHeaderString = request.queryParams("hasHeader");
    String columnIdentifier = request.queryParams("columnIdentifier");
    String searchItem = request.queryParams("searchItem");

    System.out.println(params);
    System.out.println(hasHeaderString);
    System.out.println(columnIdentifier);
    System.out.println(searchItem);

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    // Check that file is loaded
    if (this.csvState.fileNameIsEmpty()) {
      return new FileNotLoadedResponse().serialize();
    }

    switch (hasHeaderString) {
      case "true":
        this.hasHeader = true;
        break;
      case "false":
        this.hasHeader = false;
        break;
      case "":
        responseMap.put("result", "Exception: No value for hasHeader");
        return responseMap;
      default:
        responseMap.put("result", "Exception: Invalid input. Input true or false");
        return responseMap;
    }

    StringCreator stringCreator = new StringCreator();
    String file = this.csvState.getFileName();

    FileReader freader = null;
    try {
      freader = new FileReader("data/" + file);
    } catch (Exception e) {
      return new UnableToReadFile().serialize();
    }

    CSVParser<String> parser = new CSVParser<>(freader, stringCreator, this.hasHeader);
    Search search = new Search(stringCreator, parser, file);
    List<String> searchResult;

    if (columnIdentifier == null) {
      searchResult = search.searchFile(searchItem);
      System.out.println(searchResult);
    } else {
      searchResult = search.searchFile(searchItem, columnIdentifier, this.hasHeader);
      System.out.println(searchResult);
    }

    // No matches found
    if (searchResult.contains("Unable to find: '" + searchItem + "' in file")) {
      return new SearchNoMatchFailureResponse().serialize();
    }

    // Successful search
    responseMap.put("success", searchResult);
    System.out.println(searchResult);
    return new SearchSuccessResponse(responseMap).serialize();
  }

  /** Response object to send, when search is successful */
  public record SearchSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public SearchSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
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
  public record SearchNoMatchFailureResponse(String error) {
    public SearchNoMatchFailureResponse() {
      this("no match found in file");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchNoMatchFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send, when a file has not been loaded before searching */
  public record FileNotLoadedResponse(String error) {
    public FileNotLoadedResponse() {
      this("Exception: must load a csv file to search");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotLoadedResponse.class).toJson(this);
    }
  }

  /** Response object to send, when a file cannot be read */
  public record UnableToReadFile(String error) {
    public UnableToReadFile() {
      this("Error: Unable to read file");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(UnableToReadFile.class).toJson(this);
    }
  }

}