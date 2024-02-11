package edu.brown.cs.student.main.server.broadband;

public interface BroadbandSource {

  public BroadbandData getBroadBand(String state, String county) throws DatasourceException;
}
