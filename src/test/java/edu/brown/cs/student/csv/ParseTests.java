package edu.brown.cs.student.csv;

import edu.brown.cs.student.main.csv.CSVParser;
import edu.brown.cs.student.main.csv.CreatorFromRow;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class ParseTests {

  /** Creator from row (List<String>) to String[] */
  private final CreatorFromRow<String[]> creator =
      row -> {
        // src: https://stackoverflow.com/questions/2552420/converting-liststring-to-string-in-java
        return row.toArray(new String[0]);
      };

  /** Creator from row (List<String>) to String (concat row) */
  private final CreatorFromRow<String> creator2 =
      row -> {
        StringBuilder output = new StringBuilder();
        for (String s : row) {
          output.append(s);
        }
        return output.toString();
      };

  /** Creator from row (List<String>) to Integer (summing row) */
  private final CreatorFromRow<Integer> creator3 =
      row -> {
        int output = 0;
        try {
          for (String s : row) {
            output += Integer.parseInt(s);
          }
        } catch (NumberFormatException e) {
          throw new FactoryFailureException(
              "All inputs must be Strings representing Integers.", row);
        }
        return output;
      };

  /**
   * Assertions to check output of parse equals expected, comparing to lists of String arrays.
   *
   * @param actual - the actual output of parse, a List<String[]>
   * @param expected - the expected output of parse, a List<String[]>
   */
  private <T> void assertEqualsCSV(List<T> actual, List<T> expected) {
    Assert.assertEquals(expected.size(), actual.size());
    for (int i = 0; i < actual.size(); i++) {
      Assert.assertEquals(expected.get(i), actual.get(i));
    }
  }

  @Test
  public void testParseBasic() throws IOException, FactoryFailureException {
    String input = "hi,1,2,3\nhi,3,2,1";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"hi", "1", "2", "3"};
    String[] line2 = {"hi", "3", "2", "1"};
    expectedOutput.add(line1);
    expectedOutput.add(line2);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseOneLineMultiColumn() throws IOException, FactoryFailureException {
    String input = "a,B,Hello,1,4";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"a", "B", "Hello", "1", "4"};
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseOneLineOneColumn() throws IOException, FactoryFailureException {
    String input = "a";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"a"};
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseMultiLineOneColumn() throws IOException, FactoryFailureException {
    String input = "a\nB\nHello\n1\n-99";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"a"};
    String[] line2 = {"B"};
    String[] line3 = {"Hello"};
    String[] line4 = {"1"};
    String[] line5 = {"-99"};
    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    expectedOutput.add(line5);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseEmpty() throws IOException, FactoryFailureException {
    String input = "";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseInvalidBasic() {
    String input = "a,b\na,b,c";
    StringReader reader = new StringReader(input);

    Assert.expectThrows(
        IllegalArgumentException.class, () -> new CSVParser<>(reader, this.creator));
  }

  @Test
  public void testParseInvalidOneColumn() {
    String input = "a\na\nb\na,c\ne\nf";
    StringReader reader = new StringReader(input);

    Assert.expectThrows(
        IllegalArgumentException.class, () -> new CSVParser<>(reader, this.creator));
  }

  @Test
  public void testParseInvalidMultiColumn() {
    String input = "a\na,b,d,e,f\nb\na,c\ne\nf";
    StringReader reader = new StringReader(input);

    Assert.expectThrows(
        IllegalArgumentException.class, () -> new CSVParser<>(reader, this.creator));
  }

  @Test
  public void testParseInvalidComma() {
    String input = "a\na\nb\na,,\ne\nf";
    StringReader reader = new StringReader(input);

    Assert.expectThrows(
        IllegalArgumentException.class, () -> new CSVParser<>(reader, this.creator));
  }

  @Test
  public void testParseManyEmptyCommas() throws IOException, FactoryFailureException {
    String input = "hi,,,,hi";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"hi", "", "", "", "hi"};

    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseAllEmptyCommas() throws IOException, FactoryFailureException {
    String input = ",,,,";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"", "", "", "", ""};

    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseQuote() throws IOException, FactoryFailureException {
    String input = "Caesar, Julius, \"veni, vidi, vici\"";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"Caesar", " Julius", " \"veni, vidi, vici\""};
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseEmptyCommas() throws IOException, FactoryFailureException {
    String input = "Caesar, Julius,,";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"Caesar", " Julius", "", ""};
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseEmptyCommas2() throws IOException, FactoryFailureException {
    String input =
        "Caesar, Julius,,\nCaesar, Julius,,\nCaesar, Julius,,\nCaesar, Julius,,\nCaesar, Julius,,";
    StringReader reader = new StringReader(input);
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"Caesar", " Julius", "", ""};
    expectedOutput.add(line);
    expectedOutput.add(line);
    expectedOutput.add(line);
    expectedOutput.add(line);
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseMalformedCSV() throws IOException {
    try (FileReader reader = new FileReader("data/malformed/malformed_signs.csv")) {
      Assert.expectThrows(
          IllegalArgumentException.class, () -> new CSVParser<>(reader, this.creator));
    }
  }

  @Test
  public void testParseCSV() throws IOException, FactoryFailureException {
    FileReader reader = new FileReader("data/stars/ten-star.csv");
    CSVParser<String[]> parser = new CSVParser<>(reader, this.creator);

    List<String[]> actualOutput = parser.getParsed();

    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"StarID", "ProperName", "X", "Y", "Z"};
    String[] line2 = {"0", "Sol", "0", "0", "0"};
    String[] line3 = {"1", "", "282.43485", "0.00449", "5.36884"};
    String[] line4 = {"2", "", "43.04329", "0.00285", "-15.24144"};
    String[] line5 = {"3", "", "277.11358", "0.02422", "223.27753"};
    String[] line6 = {"3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"};
    String[] line7 = {"70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"};
    String[] line8 = {"71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"};
    String[] line9 = {"71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"};
    String[] line10 = {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"};
    String[] line11 = {"118721", "", "-2.28262", "0.64697", "0.29354"};

    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    expectedOutput.add(line5);
    expectedOutput.add(line6);
    expectedOutput.add(line7);
    expectedOutput.add(line8);
    expectedOutput.add(line9);
    expectedOutput.add(line10);
    expectedOutput.add(line11);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseCreateToString() throws IOException, FactoryFailureException {
    String input = "Hello, World,!!!";
    StringReader reader = new StringReader(input);
    CSVParser<String> parser = new CSVParser<>(reader, this.creator2);

    List<String> actualOutput = parser.getParsed();

    List<String> expectedOutput = new ArrayList<>();
    String line = "Hello World!!!";
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseCreateToStringMultiLine() throws IOException, FactoryFailureException {
    String input = "Hello, World,!!!\nGood,bye, :)";
    StringReader reader = new StringReader(input);
    CSVParser<String> parser = new CSVParser<>(reader, this.creator2);

    List<String> actualOutput = parser.getParsed();

    List<String> expectedOutput = new ArrayList<>();
    String line1 = "Hello World!!!";
    String line2 = "Goodbye :)";
    expectedOutput.add(line1);
    expectedOutput.add(line2);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseCreateInt() throws IOException, FactoryFailureException {
    String input = "1,2,3,4\n1,0,0,-1";
    StringReader reader = new StringReader(input);
    CSVParser<Integer> parser = new CSVParser<>(reader, this.creator3);

    List<Integer> actualOutput = parser.getParsed();

    List<Integer> expectedOutput = new ArrayList<>();
    int line1 = 10;
    int line2 = 0;
    expectedOutput.add(line1);
    expectedOutput.add(line2);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testParseCreateFactoryFail() {
    String input = "1.0";
    StringReader reader = new StringReader(input);

    Assert.expectThrows(
        FactoryFailureException.class, () -> new CSVParser<>(reader, this.creator3));
  }
}
