package edu.brown.cs.student.main.Server;

import com.squareup.moshi.Moshi;
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
      return new SearchHandler.UnableToReadFile().serialize();
    }

    CSVParser<String> parser = new CSVParser<>(freader, stringCreator, this.hasHeader);
    Search search = new Search(stringCreator, parser, file);
    List<String> searchResult;


    // viewcsv, which sends back the entire CSV file's contents as a Json 2-dimensional array.
    // parse into list of list<String>






    return null;
  }







  /** Response object to send, when a file has not been loaded before searching */
  public record FileNotLoadedResponse(String error) {
    public FileNotLoadedResponse() {
      this("Exception: must load a csv file to search");
    }
    /** @return this response, serialized as Json */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchHandler.FileNotLoadedResponse.class).toJson(this);
    }
  }

}
