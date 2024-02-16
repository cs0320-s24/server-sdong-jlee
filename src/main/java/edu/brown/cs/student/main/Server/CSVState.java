package edu.brown.cs.student.main.Server;

/**
 * A shared CSV state for LoadHandler, SearchHandler, and ViewHandler to ensure operation on the
 * same CSV file. Allows for updates to the CSVState to be seen by all three handlers.
 */
public class CSVState {
  private String fileName;
  private boolean hasHeader;

  public CSVState() {}

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setHasHeader(boolean hasHeader) {
    this.hasHeader = hasHeader;
  }

  public boolean fileNameIsEmpty() {
    return fileName == null ? true : false;
  }

  public String getFileName() {
    return this.fileName;
  }

  public boolean getHasHeader() {
    return hasHeader;
  }
}
