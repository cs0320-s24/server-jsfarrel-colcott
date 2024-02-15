package edu.brown.cs.student.api.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.csv.LoadCSVHandler;
import edu.brown.cs.student.main.server.csv.SearchCSVHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * An INTEGRATION TEST differs from a UNIT TEST in that it's testing a combination of code units and
 * their combined behavior.
 *
 * <p>Test our API server: send real web requests to our server as it is running. Note that for
 * these, we prefer to avoid sending many real API requests to the NWS, and use "mocking" to avoid
 * it. (There are many other reasons to use mock data here. What are they?)
 *
 * <p>In short, there are two new techniques demonstrated here: writing tests that send fake API
 * requests; and testing with mock data / mock objects.
 */
public class TestSearchCSVHandler {

  @BeforeAll
  public static void setupOnce() {
    Spark.port(0); // Pick an arbitrary free port
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root
  }

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  @BeforeEach
  public void setup() {
    // Re-initialize parser, state, etc. for every test method
    ParserState parserState = new ParserState();
    Spark.get("/loadcsv", new LoadCSVHandler(parserState)); // no need to mock
    Spark.get("/searchcsv", new SearchCSVHandler(parserState)); // no need to mock
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    this.adapter = moshi.adapter(this.mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/loadcsv");
    Spark.unmap("/searchcsv");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  @AfterAll
  public static void shutDown() throws InterruptedException {
    // Gracefully stop Spark listening on both endpoints
    Spark.stop();
    Thread.sleep(3000); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * <p>The "throws" clause doesn't matter below -- JUnit will fail if an exception is thrown that
   * hasn't been declared as a parameter to @Test.
   *
   * @param apiCall the call string, including endpoint (Note: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send a request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    // The request body contains a Json object
    clientConnection.setRequestProperty("Content-Type", "application/json");
    // We're expecting a Json object in the response body
    clientConnection.setRequestProperty("Accept", "application/json");

    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testSearchCSVSuccess() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "3";
    String columnSpecifier = "name";
    String columnIdentifier = "StarID";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      assertEquals(result, responseBody.get("data"));

      loadConnection.disconnect();
      searchConnection.disconnect();
    }
  }

  @Test
  public void testSearchCSVDupe() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "3";
    String columnSpecifier = "name";
    String columnIdentifier = "StarID";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      assertEquals(result, responseBody.get("data"));

      searchConnection.disconnect();
    }

    searchConnection = this.tryRequest("searchcsv?" + params);

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      assertEquals(result, responseBody.get("data"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchCSVLoadNewCSV() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "3";
    String columnSpecifier = "name";
    String columnIdentifier = "StarID";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      assertEquals(result, responseBody.get("data"));

      loadConnection.disconnect();
      searchConnection.disconnect();

      filepath = "data/stars/ten-star.csv";
      // request loadcsv
      loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
      assertEquals(200, loadConnection.getResponseCode());
    }

    searchConnection = this.tryRequest("searchcsv?" + params);

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      assertEquals(result, responseBody.get("data"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchNothingLoaded() throws IOException {
    String toSearch = "Proxima%20Centauri";
    String columnSpecifier = "name";
    String columnIdentifier = "StarID";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_json", responseBody.get("result"));

      searchConnection.disconnect();
    }
  }

  @Test
  public void testSearchWithSpace() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "Proxima%20Centauri";
    String columnSpecifier = "name";
    String columnIdentifier = "ProperName";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));
    }

    searchConnection.disconnect();
    loadConnection.disconnect();
  }

  @Test
  public void testSearchMultipleResults() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "";
    String columnSpecifier = "name";
    String columnIdentifier = "ProperName";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("1", "", "282.43485", "0.00449", "5.36884"));
      result.add(Arrays.asList("2", "", "43.04329", "0.00285", "-15.24144"));
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      result.add(Arrays.asList("118721", "", "-2.28262", "0.64697", "0.29354"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchByIndex() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "index";
    String columnIdentifier = "0";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier
            + "&hasHeaders="
            + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchNoColumnSpecification() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String hasHeaders = "true";
    String params = "toSearch=" + toSearch + "&hasHeaders=" + hasHeaders;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchNoColumnSpecificationAsNone() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "none";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchNoColumnHeader() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "index";
    String columnIdentifier = "0";
    String hasHeaders = "false";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchBigIndexNoHeader() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "index";
    String columnIdentifier = "8888";
    String hasHeaders = "false";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchColumnSpecifierNoIdentifier() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "index";
    String hasHeaders = "false";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchColumnIdentifierNoSpecifier() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnIdentifier = "0";
    String hasHeaders = "false";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnIdentifier="
            + columnIdentifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchColumnNoHasHeaders() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String params = "toSearch=" + toSearch;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchColumnNoColumnSpecification() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String params = "toSearch=" + toSearch + "&hasHeaders=" + "true";
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      assertEquals("success", responseBody.get("result"));
      assertEquals(result, responseBody.get("data"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchColumnNotFound() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "0";
    String columnSpecifier = "name";
    String columnIdentifier = "NonExistantColumn";
    String hasHeaders = "false";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testSearchRhodeIslandIncome() throws IOException {
    String filepath = "data/rhode_island_income.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    String toSearch = "Barrington";
    String columnSpecifier = "name";
    String columnIdentifier = "City/Town";
    String hasHeaders = "true";
    String params =
        "toSearch="
            + toSearch
            + "&hasHeaders="
            + hasHeaders
            + "&columnSpecifier="
            + columnSpecifier
            + "&columnIdentifier="
            + columnIdentifier;
    HttpURLConnection searchConnection = this.tryRequest("searchcsv?" + params);
    assertEquals(200, searchConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(searchConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("Barrington", "\"130,455.00\"", "\"154,441.00\"", "\"69,917.00\""));
      assertEquals(result, responseBody.get("data"));
      assertEquals("success", responseBody.get("result"));

      searchConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  /**
   * Helper to make working with a large test suite easier: if an error, print more info.
   *
   * @param body prints
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}
