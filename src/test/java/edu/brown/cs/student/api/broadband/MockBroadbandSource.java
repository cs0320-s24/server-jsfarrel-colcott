package edu.brown.cs.student.api.broadband;

import edu.brown.cs.student.main.server.broadband.BroadbandData;
import edu.brown.cs.student.main.server.broadband.BroadbandSource;

public class MockBroadbandSource implements BroadbandSource {

  private final double percentBroadband;

  public MockBroadbandSource(double percentBroadband) {
    this.percentBroadband = percentBroadband;
  }

  public BroadbandData getBroadBand(String state, String county) {
    return new BroadbandData(this.percentBroadband);
  }
}
