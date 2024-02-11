package edu.brown.cs.student.main.Server;

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
      responseMap.put("result", "Exception: must load a csv file to search");
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
        responseMap.put("result", "Exception: no value for hasHeader");
        return responseMap;
      default:
        responseMap.put("result", "Exception: not a valid input. Input true or false");
        return responseMap;
    }

    StringCreator stringCreator = new StringCreator();
    String file = this.csvState.getFileName();
    FileReader freader = new FileReader(file);
    CSVParser<String> parser = new CSVParser<>(freader, stringCreator, this.hasHeader);
    Search search = new Search(stringCreator, parser, file);

    if (columnIdentifier == null) {
      List<String> matchingRows = search.searchFile(searchItem);

    } else {
      List<String> matchingRows = search.searchFile(searchItem, columnIdentifier, this.hasHeader);

    }



//    responseMap.put("result", "search success");
    return responseMap;
  }

}
