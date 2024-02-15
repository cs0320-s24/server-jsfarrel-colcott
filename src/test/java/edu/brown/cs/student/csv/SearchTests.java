package edu.brown.cs.student.csv;

import edu.brown.cs.student.main.csv.CSVParser;
import edu.brown.cs.student.main.csv.CSVSearcher;
import edu.brown.cs.student.main.csv.CSVSearcher.ColumnSpecified;
import edu.brown.cs.student.main.csv.CreatorFromRow;
import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class SearchTests {

  /** Creator from row (List<String>) to String[] */
  private final CreatorFromRow<String[]> creator =
      row -> {
        // src: https://stackoverflow.com/questions/2552420/converting-liststring-to-string-in-java
        return row.toArray(new String[0]);
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
  public void testSearchUnspecifiedColumnOneResult() throws IOException, FactoryFailureException {
    String filename = "stars/stardata.csv";
    String value = "Andreas";
    String column = "";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"1", "Andreas", "282.43485", "0.00449", "5.36884"};
    expectedOutput.add(line);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchIndexColumnOneResult() throws IOException, FactoryFailureException {
    String filename = "stars/stardata.csv";
    String value = "Andreas";
    String column = "1";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.INDEX);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"1", "Andreas", "282.43485", "0.00449", "5.36884"};
    expectedOutput.add(line);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchMultipleTimes() throws IOException, FactoryFailureException {
    String filename = "stars/stardata.csv";
    String value = "Andreas";
    String column = "1";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.INDEX);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"1", "Andreas", "282.43485", "0.00449", "5.36884"};
    expectedOutput.add(line);
    this.assertEqualsCSV(actualOutput, expectedOutput);

    actualOutput = searcher.search("Rory", column, ColumnSpecified.UNSPECIFIED);
    expectedOutput = new ArrayList<>();
    String[] line2 = {"2", "Rory", "43.04329", "0.00285", "-15.24144"};
    expectedOutput.add(line2);
    this.assertEqualsCSV(actualOutput, expectedOutput);

    actualOutput = searcher.search("0", "StarID", ColumnSpecified.NAME);
    expectedOutput = new ArrayList<>();
    String[] line0 = {"0", "Sol", "0", "0", "0"};
    expectedOutput.add(line0);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchNameColumnOneResult() throws IOException, FactoryFailureException {
    String filename = "stars/stardata.csv";
    String value = "Andreas";
    String column = "ProperName";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.NAME);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"1", "Andreas", "282.43485", "0.00449", "5.36884"};
    expectedOutput.add(line);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchUnspecifiedColumnMultiResult() throws IOException, FactoryFailureException {
    String filename = "census/dol_ri_earnings_disparity.csv";
    String value = "RI";
    String column = "";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"};
    String[] line2 = {"RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"};
    String[] line3 = {
      "RI", "Native American/American Indian", " $471.07 ", "2315.505646", " $0.45 ", "0%"
    };
    String[] line4 = {
      "RI", "Asian-Pacific Islander", "\" $1,080.09 \"", "18956.71657", " $1.02 ", "4%"
    };
    String[] line5 = {"RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"};
    String[] line6 = {"RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"};

    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    expectedOutput.add(line5);
    expectedOutput.add(line6);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchNameColumnMultiResult() throws IOException, FactoryFailureException {
    String filename = "census/dol_ri_earnings_disparity.csv";
    String value = "RI";
    String column = "State";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.NAME);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"};
    String[] line2 = {"RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"};
    String[] line3 = {
      "RI", "Native American/American Indian", " $471.07 ", "2315.505646", " $0.45 ", "0%"
    };
    String[] line4 = {
      "RI", "Asian-Pacific Islander", "\" $1,080.09 \"", "18956.71657", " $1.02 ", "4%"
    };
    String[] line5 = {"RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"};
    String[] line6 = {"RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"};

    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    expectedOutput.add(line5);
    expectedOutput.add(line6);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchIndexColumnMultiResult() throws IOException, FactoryFailureException {
    String filename = "census/dol_ri_earnings_disparity.csv";
    String value = "RI";
    String column = "0";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.INDEX);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"};
    String[] line2 = {"RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"};
    String[] line3 = {
      "RI", "Native American/American Indian", " $471.07 ", "2315.505646", " $0.45 ", "0%"
    };
    String[] line4 = {
      "RI", "Asian-Pacific Islander", "\" $1,080.09 \"", "18956.71657", " $1.02 ", "4%"
    };
    String[] line5 = {"RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"};
    String[] line6 = {"RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"};

    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    expectedOutput.add(line5);
    expectedOutput.add(line6);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchEmptyValues() throws IOException, FactoryFailureException {
    String filename = "stars/ten-star.csv";
    String value = "";
    String column = "";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"1", "", "282.43485", "0.00449", "5.36884"};
    String[] line2 = {"2", "", "43.04329", "0.00285", "-15.24144"};
    String[] line3 = {"3", "", "277.11358", "0.02422", "223.27753"};
    String[] line4 = {"118721", "", "-2.28262", "0.64697", "0.29354"};

    expectedOutput.add(line1);
    expectedOutput.add(line2);
    expectedOutput.add(line3);
    expectedOutput.add(line4);
    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchNoResults() throws IOException, FactoryFailureException {
    String filename = "stars/ten-star.csv";
    String value = "hi";
    String column = "";
    FileReader fileReader = new FileReader("data/" + filename);

    CSVParser<String[]> parser = new CSVParser<>(fileReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchStringReaderBasic() throws IOException, FactoryFailureException {
    String data = "test,data,123\nno,test,-1";
    String value = "test";
    String column = "";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, false);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"test", "data", "123"};
    String[] line2 = {"no", "test", "-1"};
    expectedOutput.add(line1);
    expectedOutput.add(line2);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchMalformedData() throws IOException {
    String filename = "malformed/malformed_signs.csv";
    try (FileReader fileReader = new FileReader("data/" + filename)) {
      Assert.expectThrows(
          IllegalArgumentException.class, () -> new CSVParser<>(fileReader, this.creator));
    }
  }

  @Test
  public void testSearchStringEmpty() throws IOException, FactoryFailureException {
    String data = "";
    String value = "";
    String column = "";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, false);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchComma() throws IOException, FactoryFailureException {
    String data = ",";
    String value = "";
    String column = "";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, false);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.UNSPECIFIED);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line = {"", ""};
    expectedOutput.add(line);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchBadParserFailure() {
    String data = ",a,b,\n,";
    StringReader stringReader = new StringReader(data);
    Assert.expectThrows(
        IllegalArgumentException.class, () -> new CSVParser<>(stringReader, this.creator));
  }

  @Test
  public void testSearchWrongColumn() throws IOException, FactoryFailureException {
    String data = "a,b,c\nA,B,C\nC,B,A";
    String value = "B";
    String column = "a";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.NAME);
    List<String[]> expectedOutput = new ArrayList<>();

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }

  @Test
  public void testSearchColumnNameWithoutHeader() throws IOException, FactoryFailureException {
    String data = "a,b,c\nA,B,C\nC,B,A";
    String value = "B";
    String column = "a";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, false);

    Assert.assertThrows(
        IllegalArgumentException.class, () -> searcher.search(value, column, ColumnSpecified.NAME));
  }

  @Test
  public void testSearchSameColumnNames() throws IOException, FactoryFailureException {
    String data = "A,A,B\na,b,c\nd,e,f\ne,f,g";
    String value = "e";
    String column = "A";
    StringReader stringReader = new StringReader(data);

    CSVParser<String[]> parser = new CSVParser<>(stringReader, this.creator);
    CSVSearcher searcher = new CSVSearcher(parser, true);

    List<String[]> actualOutput = searcher.search(value, column, ColumnSpecified.NAME);
    List<String[]> expectedOutput = new ArrayList<>();
    String[] line1 = {"d", "e", "f"};
    String[] line2 = {"e", "f", "g"};
    expectedOutput.add(line1);
    expectedOutput.add(line2);

    this.assertEqualsCSV(actualOutput, expectedOutput);
  }
}
