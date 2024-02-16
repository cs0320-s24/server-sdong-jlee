package edu.brown.cs.student.main.CreatorInterface;

import java.util.List;
import java.util.regex.Pattern;

/**
 * StringCreator class to create String objects implementing methods from CreatorFromRow interface
 */
public class StringCreator implements CreatorFromRow<String> {

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /**
   * Create method creates String from a list of strings to create one String to represent a row
   *
   * @param row - list of strings that make up a row
   * @return - single string for whole row
   * @throws FactoryFailureException - if create doesn't work
   */
  @Override
  public String create(List<String> row) throws FactoryFailureException {
    if (row == null || row.isEmpty()) {
      throw new FactoryFailureException("Input data is null or empty", row);
    }
    return String.join(",", row);
  }

  /**
   * method utilized in search when trying to find if a key word is in a given row
   *
   * @param row - row of CSV
   * @param searchItem - key word to search for
   * @return - true if can find word otherwise false
   */
  @Override
  public Boolean iterate(String row, String searchItem) {

    // split result into what regex splits into
    String[] result = regexSplitCSVRow.split(row);
    // loop through individual strings in row to see if search exists
    for (String s : result) {
      if (s.equals(searchItem)) {
        return true;
      }
    }
    return false;
  }
}
