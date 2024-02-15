package edu.brown.cs.student.api.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.csv.LoadCSVHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class TestLoadCSVHandler {

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
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    this.adapter = moshi.adapter(this.mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/loadcsv");
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
  public void testLoadDataSuccess() throws IOException {
    String filepath = "data/stars/stardata.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(filepath, responseBody.get("filepath"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadMultipleCSVs() throws IOException {
    String filepath = "data/stars/stardata.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(filepath, responseBody.get("filepath"));

      loadConnection.disconnect();
    }

    filepath = "data/census/postsecondary_education.csv";
    loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(filepath, responseBody.get("filepath"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadDataNonExistentCSV() throws IOException {
    String filepath = "data/stars/nonexistantcsv.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadDataNonCSVFile() throws IOException {
    String filepath = "data/menu.json";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadDataMalformedCSV() throws IOException {
    String filepath = "data/malformed/malformed_signs.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadCSVOutsideDirectory() throws IOException {
    String filepath = "data/../../test.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }

    filepath = "../test.csv";
    loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }

    filepath = "..";
    loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadDataNoParamsRequest() throws IOException {
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv");
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadDataEmptyParam() throws IOException {
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=");
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadNonExistentParam() throws IOException {
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filpath=data/abc.csv");
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadExtraParam() throws IOException {
    String filepath = "data/census/income_by_race.csv";
    // request loadcsv
    HttpURLConnection loadConnection =
        this.tryRequest("loadcsv?filepath=" + filepath + "&hello=hi");
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      loadConnection.disconnect();
    }
  }

  @Test
  public void testLoadRhodeIslandIncome() throws IOException {
    String filepath = "data/rhode_island_income.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(filepath, responseBody.get("filepath"));

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
