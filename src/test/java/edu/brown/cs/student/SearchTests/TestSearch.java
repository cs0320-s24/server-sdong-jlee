package edu.brown.cs.student.SearchTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.brown.cs.student.main.CreatorInterface.FactoryFailureException;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.Parse.CSVParser;
import edu.brown.cs.student.main.Searcher.Search;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests functionality of Search class and associated methods */
public class TestSearch {

  String testfile =
      "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/census/dol_ri_earnings_disparity.csv";
  String testline = "RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%";
  FileReader reader = new FileReader(testfile);
  StringCreator stringCreator = new StringCreator();
  CSVParser<String> csvParser = new CSVParser<>(reader, stringCreator, true);

  public TestSearch() throws FileNotFoundException {}

  @Test
  public void RITownSearch() throws IOException, FactoryFailureException {
    String RIfile =
        "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/RITownIncome/RI.csv";
    FileReader riReader = new FileReader((RIfile));
    CSVParser<String> riParser = new CSVParser<>(riReader, stringCreator, true);
    Search search = new Search(stringCreator, riParser, RIfile);
    assertEquals(
        List.of("Barrington,\"130,455.00\",\"154,441.00\",\"69,917.00\"\n"),
        search.searchFile("Barrington"));
  }

  @Test
  public void basicSearchNoHeader() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(List.of(testline + "\n"), search.searchFile("White"));
  }

  @Test
  public void basicSearchFail() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of("Unable to find: 'thisisnotinfile' in file"), search.searchFile("thisisnotinfile"));
  }

  @Test
  public void basicSearchWithColIndex() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(List.of(testline + "\n"), search.searchFile("White", "1", false));
  }

  @Test
  public void basicSearchWithColIndexWrong() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of("Unable to find: 'White' in file"), search.searchFile("White", "5", false));
  }

  @Test
  public void basicSearchWithColHeader() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(List.of(testline + "\n"), search.searchFile("White", "Data Type", true));
  }

  @Test
  public void basicSearchWithColHeaderWrong() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of("Unable to find: 'White' in file"), search.searchFile("White", "State", true));
  }

  @Test
  public void fileOutsideDirectory() throws IOException, FactoryFailureException {
    FileReader reader =
        new FileReader(
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/src/main/java/edu/brown/cs/student/main/CreatorInterface/CreatorFromRow.java");
    CSVParser<String> csvParser = new CSVParser<>(reader, stringCreator, true);
    Search search =
        new Search(
            stringCreator,
            csvParser,
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/config/intellij-java-google-style.xml");
    assertNull(search.searchFile("White", "State", true));
  }

  @Test
  public void testMalformedColsInvalID() throws IOException, FactoryFailureException {
    FileReader reader =
        new FileReader(
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/malformed/malformed_signs.csv");
    CSVParser<String> csvParser = new CSVParser<>(reader, stringCreator, true);
    Search search =
        new Search(
            stringCreator,
            csvParser,
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/malformed/malformed_signs.csv");
    assertEquals(List.of("Unable to find: 'Nick' in file"), search.searchFile("Nick", "3", false));
  }

  @Test
  public void testMalformedCols() throws IOException, FactoryFailureException {
    FileReader reader =
        new FileReader(
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/malformed/malformed_signs.csv");
    CSVParser<String> csvParser = new CSVParser<>(reader, stringCreator, true);
    Search search =
        new Search(
            stringCreator,
            csvParser,
            "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/malformed/malformed_signs.csv");
    assertEquals(List.of("Gemini,Roberto,Nick\n"), search.searchFile("Nick"));
  }

  @Test
  public void testMultipleRows() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);

    assertEquals(
        List.of(
            "RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%\n",
            "RI,Black, $770.26 ,30424.80376, $0.73 ,6%\n",
            "RI,Native American/American Indian, $471.07 ,2315.505646, $0.45 ,0%\n",
            "RI,Asian-Pacific Islander,\" $1,080.09 \",18956.71657, $1.02 ,4%\n",
            "RI,Hispanic/Latino, $673.14 ,74596.18851, $0.64 ,14%\n",
            "RI,Multiracial, $971.89 ,8883.049171, $0.92 ,2%\n"),
        search.searchFile("RI"));
  }

  @Test
  public void testMultipleRowsWithColumnNumber() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of(
            "RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%\n",
            "RI,Black, $770.26 ,30424.80376, $0.73 ,6%\n",
            "RI,Native American/American Indian, $471.07 ,2315.505646, $0.45 ,0%\n",
            "RI,Asian-Pacific Islander,\" $1,080.09 \",18956.71657, $1.02 ,4%\n",
            "RI,Hispanic/Latino, $673.14 ,74596.18851, $0.64 ,14%\n",
            "RI,Multiracial, $971.89 ,8883.049171, $0.92 ,2%\n"),
        search.searchFile("RI", "0", false));
  }

  @Test
  public void testMultipleRowsWithColumnHeader() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of(
            "RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%\n",
            "RI,Black, $770.26 ,30424.80376, $0.73 ,6%\n",
            "RI,Native American/American Indian, $471.07 ,2315.505646, $0.45 ,0%\n",
            "RI,Asian-Pacific Islander,\" $1,080.09 \",18956.71657, $1.02 ,4%\n",
            "RI,Hispanic/Latino, $673.14 ,74596.18851, $0.64 ,14%\n",
            "RI,Multiracial, $971.89 ,8883.049171, $0.92 ,2%\n"),
        search.searchFile("RI", "State", true));
  }

  @Test
  public void testWithColumnHeader() throws IOException, FactoryFailureException {
    Search search = new Search(stringCreator, csvParser, testfile);
    assertEquals(
        List.of("RI,White,\" $1,058.47 \",395773.6521, $1.00 ,75%\n"),
        search.searchFile("White", "Data Type", true));
  }
}
