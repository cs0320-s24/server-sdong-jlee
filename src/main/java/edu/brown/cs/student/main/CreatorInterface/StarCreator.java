package edu.brown.cs.student.main.CreatorInterface;

import java.util.List;
import java.util.regex.Pattern;

/** StarCreator class implements CreatorFromRow to create Star objects */
public class StarCreator implements CreatorFromRow<Star> {
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /**
   * create function implemented from CreatorFromRow and creates star object from a given row from
   * the stardata.csv file
   *
   * @param row - row from CSV
   * @return - Star
   * @throws FactoryFailureException - if create doesn't work
   */
  @Override
  public Star create(List<String> row) throws FactoryFailureException {
    if (row == null || row.isEmpty()) {
      throw new FactoryFailureException("Input data is null or empty", row);
    }

    int id = Integer.parseInt(row.get(0));
    String ProperName = row.get(1);
    double X = Double.parseDouble(row.get(2));
    double Y = Double.parseDouble(row.get(3));
    double Z = Double.parseDouble(row.get(4));

    return new Star(id, ProperName, X, Y, Z);
  }

  /**
   * iterate from CreatorFromRow interface
   *
   * @param row
   * @param searchItem
   * @return
   */
  @Override
  public Boolean iterate(String row, String searchItem) {
    return null;
  }
}
