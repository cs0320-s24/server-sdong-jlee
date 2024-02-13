package edu.brown.cs.student.main.CreatorInterface;

import java.util.List;
import java.util.regex.Pattern;

public class ListStringCreator implements CreatorFromRow<List<String>>{

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");


  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    if (row == null || row.isEmpty()) {
      throw new FactoryFailureException("Input data is null or empty", row);
    }
    return row;
  }

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
