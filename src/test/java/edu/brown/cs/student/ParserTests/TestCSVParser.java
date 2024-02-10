package edu.brown.cs.student.ParserTests;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.CreatorInterface.Star;
import edu.brown.cs.student.main.CreatorInterface.StarCreator;
import edu.brown.cs.student.main.CreatorInterface.StringCreator;
import edu.brown.cs.student.main.FactoryFailureException;
import edu.brown.cs.student.main.Parse.CSVParser;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Tests the functionality of the CSVParser class and associated methods */
public class TestCSVParser {
  String line =
      "State,Data Type,Average Weekly Earnings,Number of Workers,Earnings Disparity,Employed Percent";
  String line2 =
      "2,Black,2020,2020,72443,54768,\"Bristol County, RI\",05000US44001,bristol-county-ri";

  String starFile = "/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/stars/stardata.csv";
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  List<String> empty_string_list = new ArrayList<>();

  @Test
  public void RITownParse() throws IOException, FactoryFailureException {
    FileReader reader = new FileReader("/Users/masonlee/Desktop/CS/CS32/projects/server-sdong-jlee/data/RITownIncome/RI.csv");

    StringCreator readerObject = new StringCreator();
    CSVParser<String> parser = new CSVParser<>(reader, readerObject, true);
    List<String> result = parser.parseCSV();
    assertEquals(40, result.size());
    //assertEquals(line, result.get(0));
  }

  @Test
  public void standardLine() throws IOException, FactoryFailureException {
    StringReader reader = new StringReader(line);

    StringCreator readerObject = new StringCreator();

    CSVParser<String> parser = new CSVParser<>(reader, readerObject, false);
    List<String> result = parser.parseCSV();
    assertEquals(1, result.size());
    assertEquals(line, result.get(0));
  }

  @Test
  public void moreComplexLine() throws IOException, FactoryFailureException {
    StringReader reader = new StringReader(line2);
    StringCreator readerObject = new StringCreator();
    CSVParser<String> parser = new CSVParser<>(reader, readerObject, false);
    List<String> result = parser.parseCSV();
    assertEquals(1, result.size());
    assertEquals(line2, result.get(0));
  }

  @Test
  public void invalidCreateThrowsFactoryFailure() throws IOException, FactoryFailureException {

    StringCreator readerObject = new StringCreator();
    assertThrows(FactoryFailureException.class, () -> readerObject.create(empty_string_list));
  }

  @Test
  public void iterateSimple() throws IOException, FactoryFailureException {
    StringCreator readerObject = new StringCreator();
    assertEquals(line.contains("State"), readerObject.iterate(line, "State"));
  }

  @Test
  public void iterateNumber() throws IOException, FactoryFailureException {
    StringCreator readerObject = new StringCreator();
    assertEquals(line2.contains("72443"), readerObject.iterate(line2, "72443"));
  }

  @Test
  public void parseWithFile() throws IOException, FactoryFailureException {
    FileReader fileReader = new FileReader(starFile);

    StringCreator readerObject = new StringCreator();
    CSVParser<String> parser = new CSVParser<>(fileReader, readerObject, true);

    assertEquals("3,Mortimer,277.11358,0.02422,223.27753", parser.parseCSV().get(3));
  }

  @Test
  public void regexProb() {
    String input = "Test,testing,\"hi, \"\"hello\"\", hola\",1010";
    String[] expected = {"Test", "testing", "hi, \"hello\", hola", "1010"};

    String[] split = regexSplitCSVRow.split(input);

    assertNotEquals(List.of(expected), List.of(split));
  }

  @Test
  public void parseStarObject() throws IOException, FactoryFailureException {
    FileReader fileReader = new FileReader(starFile);

    StarCreator readerObject = new StarCreator();
    CSVParser<Star> parser = new CSVParser<>(fileReader, readerObject, true);
    Star expectedStar = new Star(0, "Sol", 0, 0, 0);
    Star actualStar = parser.parseCSV().get(0);
    //    have to assert fields are equal and not objects in this case
    assertEquals(expectedStar.id, actualStar.id);
    assertEquals(expectedStar.ProperName, actualStar.ProperName);
    assertEquals(expectedStar.X, actualStar.X);
    assertEquals(expectedStar.Y, actualStar.Y);
    assertEquals(expectedStar.Z, actualStar.Z);
  }
}
