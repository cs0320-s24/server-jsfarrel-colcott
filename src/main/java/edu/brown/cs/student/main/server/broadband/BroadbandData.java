package edu.brown.cs.student.main.server.broadband;

/**
 * BroadbandData represents the response for searching a county's broadband coverage
 *
 * @param percentBroadband is double representation of percentage broadband coverage
 */
public record BroadbandData(double percentBroadband) {}
