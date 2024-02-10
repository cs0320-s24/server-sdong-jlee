package edu.brown.cs.student.main;

import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/** The Main class of our project. This is where execution begins. */
public final class Main {
  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) throws IOException, FactoryFailureException {
    new Main(args).run();
  }

  private Main(String[] args) {}

  private void run() throws IOException, FactoryFailureException {
    Scanner scanner = new Scanner(System.in);

    System.out.println(
        "Enter your input in this format: relative filepath from data folder, value to search for, boolean for if header, "
            + "column id (optional)"
            + " as either index or header)");
    String[] userInput = scanner.nextLine().split(", ");

    StringCreator stringCreator = new StringCreator();
    FileReader reader = null;
    try {
      //      restrict to only data folder
      reader =
          new FileReader(
              "/Users/masonlee/Desktop/CS/CS32/projects/csv-jhmlee/data/" + userInput[0]);
    } catch (Exception e) {
      System.err.println("Error: Unable to read file: " + userInput[0]);
    }

    if (userInput.length <= 2) {
      System.err.println("Error: Incorrect inputs. Make sure inputs are formatted correctly");
    }

    Boolean hasHeader = Boolean.parseBoolean(userInput[2]);

    if (userInput.length == 4) {
      Search search =
          new Search(
              stringCreator,
              new CSVParser<>(reader, stringCreator, hasHeader),
              "/Users/masonlee/Desktop/CS/CS32/projects/csv-jhmlee/data/" + userInput[0]);
      System.out.println(search.searchFile(userInput[1], userInput[3], true));
    } else if (userInput.length == 3) {
      Search search =
          new Search(
              stringCreator,
              new CSVParser<>(reader, stringCreator, hasHeader),
              "/Users/masonlee/Desktop/CS/CS32/projects/csv-jhmlee/data/" + userInput[0]);
      System.out.println(search.searchFile(userInput[1]));
    } else {
      System.err.println("Error: Incorrect inputs. Make sure inputs are formatted correctly");
    }

    scanner.close();
  }
}
