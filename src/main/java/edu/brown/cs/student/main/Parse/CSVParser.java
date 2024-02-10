package edu.brown.cs.student.main.Parse;

import edu.brown.cs.student.main.CreatorInterface.CreatorFromRow;
import edu.brown.cs.student.main.CreatorInterface.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CSV Parser class takes in a generic T to ultimately parse CSV rows into objects of type T.
 * Utilizes regex to split rows.
 *
 * @param <T>
 */
public class CSVParser<T> {

  /** Regex field */
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
  /** If CSV contains header, variable to access header if needed */
  public String header;
  //  constructor takes in 2 things: object that extends reader, then creatorFromRow
  /** Flexible Reader object to be read in different types of data, e.g. FileReader, StringReader */
  Reader reader;
  /**
   * CreatorFromRow object used as parameter for constructor to determine type of object to parse
   * CSV to. User can specify/create custom functionality.
   */
  CreatorFromRow<T> rowObject;
  /** boolean to determine if CSV contains a header */
  Boolean hasHeader;

  /**
   * CSVParser constructor, sets header to "" to determine later
   *
   * @param reader see above
   * @param rowObject see above
   * @param hasHeader see above
   */
  public CSVParser(Reader reader, CreatorFromRow<T> rowObject, Boolean hasHeader) {
    this.reader = reader;
    this.rowObject = rowObject;
    this.hasHeader = hasHeader;
    this.header = "";
  }

  /**
   * parseCSV function utilizes the regex and rowObject's create function as well as a
   * BufferedReader to iterate through the rows of the CSV, skipping the header if the CSV has a
   * header row, and converts the row into a list of type T (determined by the user).
   *
   * @return List of type T where T is any object (e.g. String, Star)
   * @throws IOException
   * @throws FactoryFailureException any time the create function from the row Object doesn't work
   */
  public List<T> parseCSV() throws IOException, FactoryFailureException {

    BufferedReader csvReader = new BufferedReader(reader);

    // reads each line of CSV into rows array
    List<T> lines = new ArrayList<>();
    String line = null;
    if (hasHeader) {
      this.header = csvReader.readLine(); // skip header
    }

    while ((line = csvReader.readLine()) != null) {
      // regex to get list of strings for each line
      String[] result = regexSplitCSVRow.split(line);
      // create T object from list of strings (one line) to add to return result and if not possible
      // throw error
      try {
        lines.add(this.rowObject.create(List.of(result)));
      } catch (Exception e) {
        throw new FactoryFailureException("Object unable to be created: ", List.of(result));
      }
    }

    return lines;
  }
}
