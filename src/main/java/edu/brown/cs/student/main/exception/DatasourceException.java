package edu.brown.cs.student.main.exception;

// src: feb08_nws_api CS32 live code

import java.util.Map;

/**
 * This exception communicates that something went wrong with a requested datasource. It _wraps_ the
 * original cause as a field, which helps with debugging, but also allows the caller to handle the
 * issue uniformly if they wish, without looking inside.
 */
public class DatasourceException extends Exception {
  // The root cause of this datasource problem
  private final Throwable cause;
  private final Map<String, Object> helperFields;

  /**
   * Constructor for a DatasourceException, sets each of its fields
   *
   * @param message the String message for this exception
   * @param cause the exception that caused this exception to be thrown
   * @param helperFields a map of fields to their values that may have caused the exception
   */
  public DatasourceException(String message, Map<String, Object> helperFields, Throwable cause) {
    super(message); // Exception message
    this.helperFields = helperFields;
    this.cause = cause;
  }

  /**
   * Constructor for a DatasourceException, sets each of its fields
   *
   * @param message the String message for this exception
   * @param helperFields a map of fields to their values that may have caused the exception
   */
  public DatasourceException(String message, Map<String, Object> helperFields) {
    super(message); // Exception message
    this.helperFields = helperFields;
    this.cause = null;
  }

  /**
   * Constructor for a DatasourceException, sets each of its fields
   *
   * @param message the String message for this exception
   */
  public DatasourceException(String message) {
    super(message); // Exception message
    this.cause = null;
    this.helperFields = null;
  }

  /**
   * Constructor for a DatasourceException, sets each of its fields
   *
   * @param message the String message for this exception
   * @param cause the exception that caused this exception to be thrown
   */
  public DatasourceException(String message, Throwable cause) {
    super(message); // Exception message
    this.cause = cause;
    this.helperFields = null;
  }

  /**
   * Returns the Throwable provided (if any) as the root cause of this exception. We don't make a
   * defensive copy here because we don't anticipate mutation of the Throwable to be any issue, and
   * because this is mostly implemented for debugging support.
   *
   * @return the root cause Throwable
   */
  public Throwable getCause() {
    return this.cause;
  }

  /**
   * Returns helper fields (if any) which can help the user figure out what was the issue
   *
   * @return the fields to help user solve the issue
   */
  public Map<String, Object> getHelperFields() {
    return this.helperFields;
  }
}
