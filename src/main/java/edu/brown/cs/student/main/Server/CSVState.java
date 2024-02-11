package edu.brown.cs.student.main.Server;

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
}
