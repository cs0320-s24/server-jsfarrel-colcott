package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.exception.FactoryFailureException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVSearcher {

  /** Represents different column search specifications */
  public enum ColumnSpecified {
    NAME,
    INDEX,
    UNSPECIFIED
  }

  private CSVParser parser;
  private List<String[]> rows;
  private String[] header;
  private int numCols;

  /**
   * CSVSearcher constructor
   *
   * @param newParser - CSV Parser that creates String[] rows
   * @param hasHeader - boolean for if the CSV has a header or not.
   */
  public CSVSearcher(CSVParser<String[]> newParser, boolean hasHeader) {
    this.parser = newParser;
    try {
      this.rows = this.parser.parse();
    } catch (FactoryFailureException e) {
      throw new IllegalArgumentException("Bad CSVParser: creating columns.");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Bad CSVParser: invalid CSV formatting.");
    } catch (IOException e) {
      throw new IllegalArgumentException("Bad CSVParser: reading from Reader.");
    }

    this.numCols = 0;
    if (this.rows.size() > 0) {
      this.numCols = this.rows.get(0).length;
    }

    this.header = new String[0];
    if (hasHeader) {
      if (this.rows == null || this.rows.size() == 0) {
        throw new IllegalArgumentException("Invalid CSV: header specified for empty CSV");
      }
      this.header = this.rows.remove(0);
    }
  }

  /**
   * Determine if a String is a proper String representation of an int. Uses regex to determine if a
   * string matches pattern of an integer. Limitations: doesn't return false for values outside the
   * integer limit. <a
   * href="https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java">
   * source </a>
   *
   * @param s - a String to check if it is an integer
   * @return true if s represents an integer, false if else
   */
  private boolean isInteger(String s) {
    return s.matches("-?(0|[1-9]\\d*)");
  }

  /**
   * Determine a list of columns to check, given a queried column and headers. Attempts to first
   * find the queried in the specified headers. If nothing is found, checks if queried column is
   * representing an int index. Returns a list because it is possible for queried column to have
   * multiple matching header columns (duplicate header columns).
   *
   * @param column - String queried column
   * @param specification - ColumnSpecified value representing what kind of specification column is
   * @return a non-empty ArrayList of Integers, with column indexes that match column
   * @throws IllegalArgumentException if no columns are not found with the given input
   */
  private List<Integer> getColumns(String column, ColumnSpecified specification)
      throws IllegalArgumentException {
    List<Integer> columnIndexes = new ArrayList<>();

    // check if column specified is an index (integer)
    if (specification == ColumnSpecified.INDEX && isInteger(column)) {
      int x = Integer.parseInt(column);
      if (x >= 0 && x < this.numCols) {
        columnIndexes.add(x);
        return columnIndexes;
      }
    }

    // check if column specified matches any header columns
    if (specification == ColumnSpecified.NAME && this.header.length != this.numCols) {
      throw new IllegalArgumentException("Header length and column count must match.");
    }

    for (int i = 0; i < numCols; i++) {
      // if didn't specify column, add all indexes
      if (specification == ColumnSpecified.UNSPECIFIED || this.header[i].equals(column)) {
        columnIndexes.add(i);
      }
    }
    if (columnIndexes.size() > 0) {
      return columnIndexes;
    }

    throw new IllegalArgumentException("Column not found.");
  }

  /**
   * Checks the defined CSV parser for a matching value in an optionally specified column with an
   * optionally defined header.
   *
   * @param value - String value to match equal to a data point in the CSV parser.
   * @param column - String column to check. Either String name of the header column or String
   *     representation of the integer index of a column (0 = "0", 1 = "1", ...). Specifying a
   *     non-existent column will throw an error. If the headers are numbers, specifying one of
   *     those numbers as the column will take priority over indexes. (e.g. header = ["id", "0"],
   *     column = "0" --- the second column is searched)
   * @param specification - boolean, true if column is specified, false if no specification
   * @return a list of rows (rows created as String[] objects) that have a datapoint equal to value
   *     in the specified column or any column if no specification.
   * @throws IOException if failure to read CSV file
   * @throws FactoryFailureException if failure to create a row from raw CSV split data
   * @throws IllegalArgumentException if invalid CSV file or invalid specified column
   */
  public List<String[]> search(String value, String column, ColumnSpecified specification)
      throws FactoryFailureException, IllegalArgumentException {
    List<String[]> result = new ArrayList<>();

    if (this.rows.size() == 0) {
      return result;
    }

    List<Integer> checkColumns = getColumns(column, specification);
    // loop through rows and columns to check and add row if there is a matching value
    for (String[] row : this.rows) {
      for (int colIndex : checkColumns) {
        if (row[colIndex].equals(value)) {
          result.add(row);
          break;
        }
      }
    }

    return result;
  }
}
