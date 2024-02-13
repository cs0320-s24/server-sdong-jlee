package edu.brown.cs.student.main.CreatorInterface;

import java.util.List;

public class ListStringCreator implements CreatorFromRow<List<String>>{

  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    if (row == null || row.isEmpty()) {
      throw new FactoryFailureException("Input data is null or empty", row);
    }
    return row;
  }

  @Override
  public Boolean iterate(String row, String searchItem) {
    return null;
  }
}
