package edu.brown.cs.student.main.server.broadband;

public interface BroadbandSource {
  public BroadbandData getBroadband(String state, String county);
}
