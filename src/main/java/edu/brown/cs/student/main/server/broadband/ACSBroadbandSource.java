package edu.brown.cs.student.main.server.broadband;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import okio.Buffer;

public class ACSBroadbandSource implements BroadbandSource {
  public BroadbandData getBroadBand(String state, String county) throws DatasourceException {
    try{
      // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
      URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientConnection = connect(requestURL);
      Moshi moshi = new Moshi.Builder().build();

      // NOTE WELL: THE TYPES GIVEN HERE WOULD VARY ANYTIME THE RESPONSE TYPE VARIES
      JsonAdapter<StateResponse> stateAdapter = moshi.adapter(StateResponse.class).nonNull();

      String stateId = "-1";
      StateResponse states = stateAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      if (states == null) {
        throw new DatasourceException("Failed to fetch state codes");
      }
      for (List<String> stateSpecification : states.states) {
        if(state.equals(stateSpecification.get(0))) {
          stateId = stateSpecification.get(1);
        }
      }
      if (stateId.equals("-1")) {
        throw new DatasourceException("Invalid state input");
      }

      requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateId);
      clientConnection = connect(requestURL);
      JsonAdapter<CountyResponse> countyAdapter = moshi.adapter(CountyResponse.class).nonNull();

      String countyId = "-1";
      CountyResponse counties = countyAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      if (counties == null) {
        throw new DatasourceException("Failed to fetch county codes");
      }
      for(List<String> countySpecification : counties.counties) {
        if (countySpecification.get(0).equals(county + " County, " + state)) {
          countyId = countySpecification.get(2);
        }
      }
      if (stateId.equals("-1")) {
        throw new DatasourceException("Invalid county input");
      }


      // https://api.census.gov/data/2022/acs/acs1/subject?get=NAME,group(S2801)&for=state:10,county:10
      requestURL = new URL("https", "api.census.gov", "/data/2022/acs/acs1/subject?get=NAME,group(S2801)&for=county:" + county + "&in=state:" + stateId);
      clientConnection = connect(requestURL);
      JsonAdapter<BroadBandResponse> broadAdapter = moshi.adapter(BroadBandResponse.class).nonNull();
      BroadBandResponse broadBandResponse = broadAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      // S2801_C01_014E
      String broadbandHouseholds = broadBandResponse.broadbands.get(1).get(1);
      // S2801_C01_001E
      String totalHouseholds = broadBandResponse.broadbands.get(1).get(1);

      double percentBroadband = 100 * Integer.parseInt(broadbandHouseholds) / Integer.parseInt(totalHouseholds);

      return new BroadbandData(percentBroadband);
    } catch(IOException e) {
      throw new DatasourceException(e.getMessage(), e);
    }
  }

  /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(!(urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }

  public record StateResponse(List<List<String>> states) {}

  public record CountyResponse(List<List<String>> counties) {}

  public record BroadBandResponse(List<List<String>> broadbands) {}
}
