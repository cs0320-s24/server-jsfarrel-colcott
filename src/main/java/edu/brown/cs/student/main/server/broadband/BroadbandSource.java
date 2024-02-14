package edu.brown.cs.student.main.server.broadband;

import edu.brown.cs.student.main.exception.DatasourceException;

/** BroadbandSource interface Has function to get broadband data from state and county */
public interface BroadbandSource {

  /**
   * getBroadBand gets the broadband data of county, state
   *
   * @param state is the String representation of a state data is wanted from
   * @param county is the String representation of a county data is wanted from
   * @return BroadbandData, including the double percent coverage of broadband internet in county
   * @throws DatasourceException is any exception from getting the broadband data
   */
  BroadbandData getBroadBand(String state, String county) throws DatasourceException;
}
