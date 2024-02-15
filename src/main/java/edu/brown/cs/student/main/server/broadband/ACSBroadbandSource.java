package edu.brown.cs.student.main.server.broadband;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.exception.DatasourceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.Buffer;

/**
 * ACSBroadbandSource Uses the ACS API to get information about broadband coverage in given county,
 * state Takes in params: state, county
 */
public class ACSBroadbandSource implements BroadbandSource {

  /** Map with keys = state names, values = state ids */
  private Map<String, String> states;

  private final Moshi moshi = new Moshi.Builder().build();
  private final Type listType = Types.newParameterizedType(List.class, List.class, String.class);
  private final JsonAdapter<List<List<String>>> listJsonAdapter = this.moshi.adapter(this.listType);

  /**
   * fetchStateId is a helper function fetch state id and define state name to id map if undefined
   *
   * @param state is String representation of state name
   * @return List of State
   * @throws Exception that may occur while fetching from census api, or an exception if state input
   *     is not valid
   */
  private String fetchStateId(String state) throws Exception {
    // create state map if undefined
    if (this.states == null) {
      // Endpoint for state ids:
      // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
      URL requestURL =
          new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientConnection = connect(requestURL);
      List<List<String>> statesFromJson =
          this.listJsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      Map<String, String> statesMap = new HashMap<>();
      if (statesFromJson != null) {
        for (List<String> stateStateId : statesFromJson) {
          // skips header
          if (!stateStateId.get(0).equals("NAME")) {
            statesMap.put(stateStateId.get(0), stateStateId.get(1));
          }
        }

        this.states = statesMap;
      }
    }

    if (this.states == null) {
      throw new DatasourceException("There was an issue fetching states. Please re-query.");
    }

    String stateId = this.states.get(state);
    if (stateId == null) {
      Map<String, Object> helperFields = new HashMap<>();
      List<String> validStates = new ArrayList<>(this.states.keySet());
      helperFields.put("valid-states", validStates);
      throw new DatasourceException("State input not valid", helperFields);
    }

    return stateId;
  }

  /**
   * fetchCountyId is a helper function to get a county id from stateID and countyName
   *
   * @param stateId is the stateId of the which the county is in
   * @param countyName is the String of the county searching for
   * @return String representation of county id
   * @throws Exception that may occur while fetching from census api, or an exception if county
   *     input is not valid
   */
  private String fetchCountyId(String stateId, String countyName) throws Exception {
    URL requestURL =
        new URL(
            "https",
            "api.census.gov",
            "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateId);
    HttpURLConnection clientConnection = connect(requestURL);
    List<List<String>> countiesFromJson =
        this.listJsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    List<String> validCounties = new ArrayList<>();
    if (countiesFromJson != null) {
      for (List<String> countyCountyId : countiesFromJson) {
        if (!countyCountyId.get(0).equals("NAME")) {
          validCounties.add(countyCountyId.get(0));
          if (countyCountyId.get(0).startsWith(countyName + " County, ")) {
            return countyCountyId.get(2);
          }
        }
      }
    }

    Map<String, Object> helperFields = new HashMap<>();
    helperFields.put("valid-counties", validCounties);

    throw new DatasourceException("County input not valid", helperFields);
  }

  /**
   * fetchPercentBroadband is a helper function to get the percent broadband coverage for a specific
   * county within a state
   *
   * @param stateId is the stateId of which the county is in
   * @param countyId is the countyID of the county searching for
   * @return double the percent broadband coverage
   * @throws Exception that may occur while fetching from census api, or an exception if
   *     state/county id input is not valid
   */
  private double fetchPercentBroadband(String stateId, String countyId) throws Exception {
    URL requestURL =
        new URL(
            "https",
            "api.census.gov",
            "/data/2022/acs/acs1/subject?get=NAME,S2801_C01_014E,S2801_C01_001E&for=county:"
                + countyId
                + "&in=state:"
                + stateId);
    HttpURLConnection clientConnection = connect(requestURL);

    List<List<String>> broadBandResponse =
        this.listJsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    if (broadBandResponse != null && !broadBandResponse.isEmpty()) {
      List<String> broadBandResponseRow = broadBandResponse.get(1);
      String broadbandHouseholds = broadBandResponseRow.get(1);
      String totalHouseholds = broadBandResponseRow.get(2);
      // todo: is there better source for this
      return 100.0 * Integer.parseInt(broadbandHouseholds) / Integer.parseInt(totalHouseholds);
    }

    throw new DatasourceException("Failed to fetch broadband coverage data.");
  }

  /**
   * getBroadBand returns BroadbandData (percent broadband coverage) for a state and county
   *
   * @param state is String representation of state we are looking for broadband coverage of
   * @param county is String representation of county we are looking for broadband coverage of
   * @return BroadbandData from ACS API for result
   * @throws DatasourceException that may occur while fetching from census api, or an exception if
   *     state/county id input is not valid
   */
  @Override
  public BroadbandData getBroadBand(String state, String county) throws DatasourceException {
    try {
      String stateId = this.fetchStateId(state);

      String countyId = this.fetchCountyId(stateId, county);

      double percentBroadband = this.fetchPercentBroadband(stateId, countyId);

      return new BroadbandData(percentBroadband);
    } catch (DatasourceException e) {
      throw new DatasourceException(e.getMessage(), e.getHelperFields(), e);
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage(), e);
    }
  }

  /**
   * Private helper method for setting up an HttpURLConnection connection with the provided URL
   *
   * @return an HttpURLConnection with the provided URL
   * @param requestURL the URL which we want to set up a connection to
   * @throws DatasourceException if API connection doesn't result in success
   * @throws IOException so different callers can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection clientConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200)
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }
}
