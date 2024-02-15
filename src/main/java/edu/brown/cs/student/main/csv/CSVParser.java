package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CSVParser<T> {

  private final Reader reader;
  private final CreatorFromRow<T> create;

  private List<T> parsed;
  private List<T> public_parsed;

  // https://github.com/cs0320/class-livecode/
  // Path: main/old/F23/vignettes/csvRegex/src/test/java/TestRegex.java
  // regex string from cs0320 class livecode (old/F23/vignettes/csvRegex/src/test/java/TestRegex)
  final Pattern rgx = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");

  /**
   * CSVParser constructor
   *
   * @param newReader - a Reader that has the contents of a CSV file
   * @param newCreate - a CreatorFromRow object for creating rows
   */
  public CSVParser(Reader newReader, CreatorFromRow<T> newCreate)
      throws IOException, FactoryFailureException, IllegalArgumentException {
    this.reader = newReader;
    this.create = newCreate;
    this.parse();
  }

  /**
   * getParsed simply returns the parsed rows as an unmodifiableList, also while making a copy of
   * the parsed rows to be retrieved by callers
   *
   * @return the list of rows that was parsed from the file
   */
  public List<T> getParsed() {
    if (this.public_parsed == null) {
      this.public_parsed = Collections.unmodifiableList(this.parsed);
    }
    return this.public_parsed;
  }

  /**
   * Parse the CSV, reading from reader, create rows, and set parsed to the list of rows in CSV.
   *
   * @throws IOException if failure reading CSV file
   * @throws FactoryFailureException if failure creating row
   * @throws IllegalArgumentException if invalid CSV file
   */
  private void parse() throws IOException, FactoryFailureException, IllegalArgumentException {
    List<T> rows = new ArrayList<>();
    BufferedReader bufferedReader = new BufferedReader(this.reader);

    String line;
    int numCols = -1;
    // loop through lines until reaching line break or end of file (line is null)
    // source: https://ioflood.com/blog/java-read-line/
    while ((line = bufferedReader.readLine()) != null) {
      if (!line.isEmpty()) {
        /* add '.' to line because regex has an issue missing. only for when the line length is
         * greater than 0 to avoid creating more issues (if length is 0 there can't be a comma too)
         */
        line += ".";
      }
      String[] values = this.rgx.split(line);
      if (!line.isEmpty()) {
        // remove added '.'
        int cut = values.length - 1;
        values[cut] = values[cut].substring(0, values[cut].length() - 1);
      }
      if (numCols == -1) {
        numCols = values.length;
      }
      if (values.length != numCols) {
        throw new IllegalArgumentException("Invalid CSV file: mismatching row dimensions");
      }
      rows.add(this.create.create(List.of(values)));
    }

    this.parsed = rows;
  }
}
