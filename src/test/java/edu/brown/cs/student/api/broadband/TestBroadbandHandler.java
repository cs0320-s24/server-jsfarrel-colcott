package edu.brown.cs.student.api.broadband;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.broadband.BroadbandHandler;
import edu.brown.cs.student.main.server.cache.APICache;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.utils.Assert;

public class TestBroadbandHandler {
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
    CacheBuilder<Object, Object> cacheBuilder =
        CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS);
    // Re-initialize parser, state, etc. for every test method
    Spark.get(
        "/broadband",
        new APICache(
            new BroadbandHandler(new MockBroadbandSource(50.0)), cacheBuilder)); // no need to mock
    Spark.awaitInitialization(); // don't continue until the server is listening

    Moshi moshi = new Moshi.Builder().build();
    this.adapter = moshi.adapter(this.mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/broadband");
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
  public void testBroadbandSuccess() throws IOException {
    String state = "California";
    String county = "Kings";
    String params = "state=California&county=Kings";

    HttpURLConnection broadbandConnection = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      Assert.notNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(50.0, responseBody.get("percent"));
      assertEquals(state, responseBody.get("state"));
      assertEquals(county, responseBody.get("county"));

      broadbandConnection.disconnect();
    }
  }

  @Test
  public void testBroadbandFailureBetweenCache() throws IOException, InterruptedException {
    String params = "state=California&county=Kings";

    HttpURLConnection broadbandConnection1 = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection1.getResponseCode()); // successful *connection*
    try (Buffer b1 = new Buffer().readFrom(broadbandConnection1.getInputStream())) {
      Map<String, Object> responseBody1 = this.adapter.fromJson(b1);
      Assert.notNull(responseBody1);
      this.showDetailsIfError(responseBody1);
      assertEquals("success", responseBody1.get("result"));

      Thread.sleep(1000);
      String difParams = "state=New%20Yrke";
      HttpURLConnection broadbandConnection3 = this.tryRequest("broadband?" + difParams);
      assertEquals(200, broadbandConnection3.getResponseCode()); // successful *connection*
      Buffer b3 = new Buffer().readFrom(broadbandConnection3.getInputStream());
      Map<String, Object> responseBody3 = this.adapter.fromJson(b3);
      Assert.notNull(responseBody3);
      this.showDetailsIfError(responseBody3);
      assertEquals("error_bad_request", responseBody3.get("result"));

      Thread.sleep(1000);
      HttpURLConnection broadbandConnection2 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection2.getResponseCode()); // successful *connection*
      Buffer b2 = new Buffer().readFrom(broadbandConnection2.getInputStream());
      Map<String, Object> responseBody2 = this.adapter.fromJson(b2);
      Assert.notNull(responseBody2);
      this.showDetailsIfError(responseBody2);
      assertEquals("success", responseBody2.get("result"));
      assertEquals(responseBody1.get("date"), responseBody2.get("date")); // same date/time

      Thread.sleep(6000);
      HttpURLConnection broadbandConnection4 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection4.getResponseCode()); // successful *connection*
      Buffer b4 = new Buffer().readFrom(broadbandConnection4.getInputStream());
      Map<String, Object> responseBody4 = this.adapter.fromJson(b4);
      Assert.notNull(responseBody4);
      this.showDetailsIfError(responseBody4);
      assertEquals("success", responseBody4.get("result"));

      assertNotEquals(responseBody1.get("date"), responseBody4.get("date")); // different date/time
      assertNotEquals(responseBody2.get("date"), responseBody4.get("date")); // different date/time
      assertNotEquals(responseBody3.get("date"), responseBody4.get("date")); // different date/time

      broadbandConnection1.disconnect();
      broadbandConnection2.disconnect();
      broadbandConnection3.disconnect();
    }
  }

  @Test
  public void testBroadbandSuccessCaching() throws IOException, InterruptedException {
    String params = "state=California&county=Kings";

    HttpURLConnection broadbandConnection1 = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection1.getResponseCode()); // successful *connection*
    try (Buffer b1 = new Buffer().readFrom(broadbandConnection1.getInputStream())) {
      Map<String, Object> responseBody1 = this.adapter.fromJson(b1);
      Assert.notNull(responseBody1);
      this.showDetailsIfError(responseBody1);
      assertEquals("success", responseBody1.get("result"));

      Thread.sleep(1000);
      HttpURLConnection broadbandConnection2 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection2.getResponseCode()); // successful *connection*
      Buffer b2 = new Buffer().readFrom(broadbandConnection2.getInputStream());
      Map<String, Object> responseBody2 = this.adapter.fromJson(b2);
      Assert.notNull(responseBody2);
      this.showDetailsIfError(responseBody2);
      assertEquals("success", responseBody2.get("result"));

      assertEquals(responseBody1.get("date"), responseBody2.get("date")); // same date/time

      String difParams = "state=New%20York&county=Monroe";
      HttpURLConnection broadbandConnection3 = this.tryRequest("broadband?" + difParams);
      assertEquals(200, broadbandConnection3.getResponseCode()); // successful *connection*
      Buffer b3 = new Buffer().readFrom(broadbandConnection3.getInputStream());
      Map<String, Object> responseBody3 = this.adapter.fromJson(b3);
      Assert.notNull(responseBody3);
      this.showDetailsIfError(responseBody3);
      assertEquals("success", responseBody3.get("result"));

      assertNotEquals(responseBody1.get("date"), responseBody3.get("date")); // different date/time
      assertNotEquals(responseBody2.get("date"), responseBody3.get("date")); // different date/time

      Thread.sleep(6000);
      HttpURLConnection broadbandConnection4 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection4.getResponseCode()); // successful *connection*
      Buffer b4 = new Buffer().readFrom(broadbandConnection4.getInputStream());
      Map<String, Object> responseBody4 = this.adapter.fromJson(b4);
      Assert.notNull(responseBody4);
      this.showDetailsIfError(responseBody4);
      assertEquals("success", responseBody4.get("result"));

      assertNotEquals(responseBody1.get("date"), responseBody4.get("date")); // different date/time
      assertNotEquals(responseBody2.get("date"), responseBody4.get("date")); // different date/time
      assertNotEquals(responseBody3.get("date"), responseBody4.get("date")); // different date/time

      broadbandConnection1.disconnect();
      broadbandConnection2.disconnect();
      broadbandConnection3.disconnect();
    }
  }

  @Test
  public void testBroadbandExtraParamCaching() throws IOException, InterruptedException {
    String state = "California";
    String county = "Kings";
    String params = "state=" + state + "&county=" + county + "&extra=param";

    HttpURLConnection broadbandConnection1 = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection1.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection1.getInputStream())) {
      Map<String, Object> responseBody1 = this.adapter.fromJson(b);
      Assert.notNull(responseBody1);
      this.showDetailsIfError(responseBody1);
      assertEquals("success", responseBody1.get("result"));

      Thread.sleep(1000);
      HttpURLConnection broadbandConnection2 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection2.getResponseCode()); // successful *connection*
      Buffer b2 = new Buffer().readFrom(broadbandConnection2.getInputStream());
      Map<String, Object> responseBody2 = this.adapter.fromJson(b2);
      Assert.notNull(responseBody2);
      this.showDetailsIfError(responseBody2);
      assertEquals("success", responseBody2.get("result"));

      assertEquals(responseBody1.get("date"), responseBody2.get("date")); // same date/time

      Thread.sleep(6000);
      HttpURLConnection broadbandConnection3 = this.tryRequest("broadband?" + params);
      assertEquals(200, broadbandConnection3.getResponseCode()); // successful *connection*
      Buffer b3 = new Buffer().readFrom(broadbandConnection3.getInputStream());
      Map<String, Object> responseBody3 = this.adapter.fromJson(b3);
      Assert.notNull(responseBody3);
      this.showDetailsIfError(responseBody3);
      assertEquals("success", responseBody2.get("result"));

      assertNotEquals(responseBody1.get("date"), responseBody3.get("date")); // different date/time
      assertNotEquals(responseBody2.get("date"), responseBody3.get("date")); // different date/time

      broadbandConnection1.disconnect();
      broadbandConnection2.disconnect();
      broadbandConnection3.disconnect();
    }
  }

  @Test
  public void testBroadbandExtraParam() throws IOException {
    String state = "California";
    String county = "Kings";
    String params = "state=" + state + "&county=" + county + "&extra=param";

    HttpURLConnection broadbandConnection = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      Assert.notNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("success", responseBody.get("result"));

      assertEquals(50.0, responseBody.get("percent"));
      assertEquals(state, responseBody.get("state"));
      assertEquals(county, responseBody.get("county"));

      broadbandConnection.disconnect();
    }
  }

  @Test
  public void testBroadbandFailureMissingStateParam() throws IOException {
    String county = "Kings";
    String params = "county=" + county;

    HttpURLConnection broadbandConnection = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      Assert.notNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      broadbandConnection.disconnect();
    }
  }

  @Test
  public void testBroadbandFailureMissingCountyParam() throws IOException {
    String state = "California";
    String params = "state=" + state;

    HttpURLConnection broadbandConnection = this.tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      Assert.notNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      broadbandConnection.disconnect();
    }
  }

  @Test
  public void testBroadbandFailureMissingBothParams() throws IOException {
    HttpURLConnection broadbandConnection = this.tryRequest("broadband");
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    try (Buffer b = new Buffer().readFrom(broadbandConnection.getInputStream())) {
      Map<String, Object> responseBody = this.adapter.fromJson(b);
      Assert.notNull(responseBody);
      this.showDetailsIfError(responseBody);
      assertEquals("error_bad_request", responseBody.get("result"));

      broadbandConnection.disconnect();
    }
  }

  /**
   * Helper to make working with a large test suite easier: if an error, print more info.
   *
   * @param body prints
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("result") && "error".equals(body.get("result"))) {
      System.out.println(body);
    }
  }
}
