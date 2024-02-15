package edu.brown.cs.student.api.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.csv.LoadCSVHandler;
import edu.brown.cs.student.main.server.csv.ViewCSVHandler;
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

public class TestViewCSVHandler {

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
    Spark.get("/viewcsv", new ViewCSVHandler(parserState)); // no need to mock
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    this.adapter = moshi.adapter(this.mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/loadcsv");
    Spark.unmap("/viewcsv");
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
  public void testViewDataSuccess() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
    }
    HttpURLConnection viewConnection = this.tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody;
    try (Buffer b = new Buffer().readFrom(viewConnection.getInputStream())) {
      responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
    }

    List<List<String>> result = new ArrayList<>();
    result.add(Arrays.asList("StarID", "ProperName", "X", "Y", "Z"));
    result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
    result.add(Arrays.asList("1", "", "282.43485", "0.00449", "5.36884"));
    result.add(Arrays.asList("2", "", "43.04329", "0.00285", "-15.24144"));
    result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
    result.add(Arrays.asList("3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"));
    result.add(Arrays.asList("70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"));
    result.add(Arrays.asList("71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"));
    result.add(Arrays.asList("71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"));
    result.add(Arrays.asList("87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"));
    result.add(Arrays.asList("118721", "", "-2.28262", "0.64697", "0.29354"));

    assertEquals("success", responseBody.get("result"));
    assertEquals(result, responseBody.get("data"));

    loadConnection.disconnect();
    viewConnection.disconnect();
  }

  @Test
  public void testViewExtraParam() throws IOException {
    String filepath = "data/stars/ten-star.csv";
    // request loadcsv
    HttpURLConnection loadConnection =
        this.tryRequest("loadcsv?filepath=" + filepath + "&hello=hi");
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    HttpURLConnection viewConnection = this.tryRequest("viewcsv?hello=hi");
    assertEquals(200, viewConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(viewConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      List<List<String>> result = new ArrayList<>();
      result.add(Arrays.asList("StarID", "ProperName", "X", "Y", "Z"));
      result.add(Arrays.asList("0", "Sol", "0", "0", "0"));
      result.add(Arrays.asList("1", "", "282.43485", "0.00449", "5.36884"));
      result.add(Arrays.asList("2", "", "43.04329", "0.00285", "-15.24144"));
      result.add(Arrays.asList("3", "", "277.11358", "0.02422", "223.27753"));
      result.add(Arrays.asList("3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"));
      result.add(Arrays.asList("70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"));
      result.add(Arrays.asList("71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"));
      result.add(Arrays.asList("71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"));
      result.add(Arrays.asList("87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"));
      result.add(Arrays.asList("118721", "", "-2.28262", "0.64697", "0.29354"));

      assertEquals("success", responseBody.get("result"));
      assertEquals(result, responseBody.get("data"));

      loadConnection.disconnect();
      viewConnection.disconnect();
    }
  }

  @Test
  public void testViewWithoutLoading() throws IOException {
    HttpURLConnection viewConnection = this.tryRequest("viewcsv?hello=hi");
    assertEquals(200, viewConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(viewConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);

      assertEquals("error_bad_json", responseBody.get("result"));

      viewConnection.disconnect();
    }
  }

  @Test
  public void testViewAfterFailureLoading() throws IOException {
    String filepath = "data/malformed/malformed_signs.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_datasource", responseBody.get("result"));
    }

    HttpURLConnection viewConnection = this.tryRequest("viewcsv?hello=hi");
    assertEquals(200, viewConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(viewConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);

      assertEquals("error_bad_json", responseBody.get("result"));

      viewConnection.disconnect();
      loadConnection.disconnect();
    }
  }

  @Test
  public void testViewRhodeIslandIncome() throws IOException {
    String filepath = "data/rhode_island_income.csv";
    // request loadcsv
    HttpURLConnection loadConnection = this.tryRequest("loadcsv?filepath=" + filepath);
    try (Buffer b = new Buffer().readFrom(loadConnection.getInputStream())) {
      assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);
    }

    Map<String, Object> responseBody;
    HttpURLConnection viewConnection = this.tryRequest("viewcsv");
    try (Buffer b = new Buffer().readFrom(viewConnection.getInputStream())) {
      assertEquals(200, viewConnection.getResponseCode()); // successful *connection*
      responseBody = this.adapter.fromJson(b);
      assertNotNull(responseBody);
      this.showDetailsIfError(responseBody);

      assertEquals("success", responseBody.get("result"));
      assertNotNull(responseBody.get("data"));
      assertEquals(ArrayList.class, responseBody.get("data").getClass());

      loadConnection.disconnect();
      viewConnection.disconnect();
    }
  }

  /**
   * Helper to make working with a large test suite easier: if an error, print more info.
   *
   * @param body is printed
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}
