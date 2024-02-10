package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class CSVParser<T> implements Iterable<T> {

  private Reader reader;
  private CreatorFromRow<T> create;

  private List<T> parsed;

  private int index = 0;

  // https://github.com/cs0320/class-livecode/
  // Path: main/old/F23/vignettes/csvRegex/src/test/java/TestRegex.java
  // regex string from cs0320 class livecode (old/F23/vignettes/csvRegex/src/test/java/TestRegex)
  final Pattern rgx = Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /**
   * CSVParser constructor
   *
   * @param newReader - a Reader that has the contents of a CSV file
   * @param newCreate - a CreatorFromRow object for creating rows
   */
  public CSVParser(Reader newReader, CreatorFromRow<T> newCreate) {
    this.reader = newReader;
    this.create = newCreate;
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        boolean hN = parsed.size() > index;
        if (!hN) {
          index = 0;
        }
        return hN;
      }

      @Override
      public T next() {
        if (this.hasNext()) {
          index = index + 1;
          return parsed.get(index - 1);
        }
        return null;
      }
    };
  }

  /**
   * Parse the CSV, reading from reader, create rows, and return list of rows in CSV.
   *
   * @return
   * @throws IOException if failure reading CSV file
   * @throws FactoryFailureException if failure creating row
   * @throws IllegalArgumentException if invalid CSV file
   */
  public List<T> parse() throws IOException, FactoryFailureException, IllegalArgumentException {
    List<T> rows = new ArrayList<>();
    BufferedReader bufferedReader = new BufferedReader(reader);

    String line;
    int numCols = -1;
    // loop through lines until reaching line break or end of file (line is null)
    // source: https://ioflood.com/blog/java-read-line/
    while ((line = bufferedReader.readLine()) != null) {
      if (line.length() > 0) {
        /* add '.' to line because regex has an issue missing. only for when the line length is
         * greater than 0 to avoid creating more issues (if length is 0 there can't be a comma too)
         */
        line += ".";
      }
      String[] values = rgx.split(line);
      if (line.length() > 0) {
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
    return rows;
  }
}
