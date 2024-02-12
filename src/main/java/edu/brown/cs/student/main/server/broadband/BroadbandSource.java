package edu.brown.cs.student.main.server.broadband;

import edu.brown.cs.student.main.exception.DatasourceException;

public interface BroadbandSource {

  public BroadbandData getBroadBand(String state, String county) throws DatasourceException;
}
