package edu.brown.cs.student.main.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchHandler implements Route {
  private CSVState csvState;
  private boolean hasHeader;


  public SearchHandler(CSVState csvState) {
    this.csvState = csvState;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    System.out.println(params);

    String hasHeaderString = request.queryParams("hasHeader");
    System.out.println(hasHeaderString);

    String columnIdentifier = request.queryParams("columnIdentifier");
    System.out.println(columnIdentifier);

    String searchItem = request.queryParams("searchItem");
    System.out.println(searchItem);

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    // Check that file is loaded
    if (this.csvState.fileNameIsEmpty()) {
      responseMap.put("result", "Exception: Must load a csv file to search with loadcsv endpoint and filepath");
      return responseMap;
    }

    switch(hasHeaderString) {
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

    FileReader freader = new FileReader("data/" + file);
    System.out.println("test past filerader");

    CSVParser<String> parser = new CSVParser<>(freader, stringCreator, this.hasHeader);
    Search search = new Search(stringCreator, parser, file);

    List<String> matchingRows;
    if (columnIdentifier == null) {
      matchingRows = search.searchFile(searchItem);
    } else {
      matchingRows = search.searchFile(searchItem, columnIdentifier, this.hasHeader);
    }
    System.out.println(matchingRows);

    if (matchingRows.size() == 0) {
      responseMap.put("result", "Exception: Search could not find: " + searchItem);
      return responseMap;

    } else {

      // as in fromJson, we need to work with ingredients.
      // The polymorphic factory will automatically _insert_ the "type" field
      Moshi moshi = new Moshi.Builder().build();
      // Uses a similar pattern but turns it toJson and returns it as a string
      JsonAdapter<List<String>> adapter = moshi.adapter(Types.newParameterizedType(List.class, String.class));
      System.out.println("Test A");
      responseMap.put("result", adapter.toJson(matchingRows));
    }

    System.out.println("Test B");

//    responseMap.put("result", "search success");
    return responseMap;
  }

}

// /loadcsv?filepath=data/RITownIncome/RI.csv
// /searchcsv?hasHeader=true&columnIdentifier=City/Town&searchItem=Exeter
