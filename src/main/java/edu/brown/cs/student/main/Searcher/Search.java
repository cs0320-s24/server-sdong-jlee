package edu.brown.cs.student.main.Searcher;

import edu.brown.cs.student.main.CreatorInterface.CreatorFromRow;
import edu.brown.cs.student.main.CreatorInterface.FactoryFailureException;
import edu.brown.cs.student.main.Parse.CSVParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Search class uses the User's input from the main function to return a list of rows that contains
 * a keyword to search for.
 */
public class Search {
  /** Regex */
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
  /** filename to search in */
  private final String fileName;
  /** CreatorFromRow object to parse CSV into strings */
  CreatorFromRow<String> stringCreator;
  /** csvParser to handle CSV and convert to rows of strings */
  CSVParser<String> csvParser;
  /** Map to store header to index for searching within specific header column */
  public Map<String, Integer> headerIndexMap;

  /**
   * Search constructor that takes in params and sets up global variables
   *
   * @param stringCreator see above
   * @param csvParser see above
   * @param fileName see above
   */
  public Search(
      CreatorFromRow<String> stringCreator, CSVParser<String> csvParser, String fileName) {
    this.stringCreator = stringCreator;
    this.csvParser = csvParser;
    this.headerIndexMap = new HashMap<>();
    this.fileName = fileName;
  }

  // utilize method overloading to create methods under same name with diff number of parameters

  /**
   * Search function when given only key word to search for. Uses CSVparser object to parse then
   * iterates using the iterate function from the stringCreator object to determine which row
   * contains the key word
   *
   * @param searchItem - key word to search for
   * @return A list of rows with the key word or null if cannot find word
   * @throws IOException - if inputs are incorrect
   * @throws FactoryFailureException - if create from stringCreator fails when parsing
   */
  public List<String> searchFile(String searchItem) throws IOException, FactoryFailureException {
    List<String> returnList = new ArrayList<>();
    Boolean containsVal = false;
//
//    if (!this.fileName.contains(
//        "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/")) {
//      System.err.println("Error: unable to read file: " + this.fileName);
//      return null;
//    }

    List<String> parsedFile = this.csvParser.parseCSV();

    for (String row : parsedFile) {
      if (this.stringCreator.iterate(row, searchItem)) {
        returnList.add(row + '\n');
        containsVal = true;
      }
    }
    if (containsVal) {
      return returnList;
    }
    // in the case that we loop through whole parsed file and don't find the word:
    return List.of("Unable to find: '" + searchItem + "' in file");
  }

  // takes in filename, String to search for, column identifier can be column index or column
  // header,

  /**
   * Search function when given a column identifier to narrow search
   *
   * @param searchItem - key word to search for
   * @param columnIdentifier - certain column to search, either header or index
   * @param containsHeader - boolean if CSV has header
   * @return A list of rows with the key word or null if cannot find word
   * @throws IOException - if inputs are incorrect
   * @throws FactoryFailureException - if create from stringCreator fails when parsing
   */
  public List<String> searchFile(String searchItem, String columnIdentifier, Boolean containsHeader)
      throws IOException, FactoryFailureException {

    List<String> returnList = new ArrayList<>();
    boolean containsVal = false;
//    if (!this.fileName.contains(
//        "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/")) {
//      System.err.println("Error: unable to read file: " + this.fileName);
//      return null;
//    }
    int columnIndex = -1;
    List<String> parsedFile = this.csvParser.parseCSV();
    List<String> headers = null;
    String headers_line = null;

    // create and populates map of header to column index
    if (containsHeader) {
      headers_line = this.csvParser.header;
      if (headers_line != null) {
        headers = List.of(regexSplitCSVRow.split(headers_line));
        for (int i = 0; i < headers.size(); i++) {
          this.headerIndexMap.put(headers.get(i), i);
        }
      }
    } else { // if using index still want to determine number of cols
      headers = List.of(regexSplitCSVRow.split(parsedFile.get(0)));
      for (int i = 0; i < headers.size(); i++) {
        this.headerIndexMap.put(headers.get(i), i);
      }
    }

    //    sets columnIndex to search in depending on header or index
    if (containsHeader) {
      columnIndex = this.headerIndexMap.getOrDefault(columnIdentifier, -1);
    } else {
      columnIndex = Integer.parseInt(columnIdentifier);
    }

    if (columnIndex < 0 || columnIndex > headers.size()) { // if index never gets set
      System.err.println("Invalid column identifier");
      return List.of("Unable to find: '" + searchItem + "' in file");
    }

    for (String string : parsedFile) {
      List<String> row = List.of(regexSplitCSVRow.split((string)));
      if (row.get(columnIndex).equals(searchItem)) {
        returnList.add(string + '\n');
        containsVal = true;
      }
    }

    if (containsVal) {
      return returnList;
    }
    // in the case that we loop through whole parsed file and don't find the word:
    return List.of("Unable to find: '" + searchItem + "' in file");
  }
}
