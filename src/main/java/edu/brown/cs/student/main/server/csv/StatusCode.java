package edu.brown.cs.student.main.server.csv;

/**
 * StatusCode represents a status response
 *
 * @param code of response (e.g. 200 = success, 400 = error)
 * @param message of response, acting as a description
 */
public record StatusCode(int code, String message) {}
