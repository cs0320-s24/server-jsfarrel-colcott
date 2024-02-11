package edu.brown.cs.student.main.csv;

public class ParserState {

  private CSVParser<String[]> parser;

  public void setParser(CSVParser<String[]> parser) {
    this.parser = parser;
  }

  public CSVParser<String[]> getParser() {
    return this.parser;
  }
}
