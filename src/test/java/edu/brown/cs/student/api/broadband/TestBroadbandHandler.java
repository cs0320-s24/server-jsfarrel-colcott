package edu.brown.cs.student.api.broadband;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.ParserState;
import edu.brown.cs.student.main.server.broadband.BroadbandHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

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
    // Re-initialize parser, state, etc. for every test method
    ParserState parserState = new ParserState();
    Spark.get("/broadband", new BroadbandHandler(new MockBroadbandSource(50.0))); // no need to mock
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

    HttpURLConnection broadbandConnection = tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody =
        this.adapter.fromJson(new Buffer().readFrom(broadbandConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("success", responseBody.get("type"));

    assertEquals(50.0, responseBody.get("percent"));
    assertEquals(state, responseBody.get("state"));
    assertEquals(county, responseBody.get("county"));

    broadbandConnection.disconnect();
  }

  @Test
  public void testBroadbandExtraParam() throws IOException {
    String state = "California";
    String county = "Kings";
    String params = "state=" + state + "&county=" + county + "&extra=param";

    HttpURLConnection broadbandConnection = tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody =
        this.adapter.fromJson(new Buffer().readFrom(broadbandConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("success", responseBody.get("type"));

    assertEquals(50.0, responseBody.get("percent"));
    assertEquals(state, responseBody.get("state"));
    assertEquals(county, responseBody.get("county"));

    broadbandConnection.disconnect();
  }

  @Test
  public void testBroadbandFailureMissingStateParam() throws IOException {
    String county = "Kings";
    String params = "county=" + county;

    HttpURLConnection broadbandConnection = tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody =
        this.adapter.fromJson(new Buffer().readFrom(broadbandConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("error", responseBody.get("type"));

    broadbandConnection.disconnect();
  }

  @Test
  public void testBroadbandFailureMissingCountyParam() throws IOException {
    String state = "California";
    String params = "state=" + state;

    HttpURLConnection broadbandConnection = tryRequest("broadband?" + params);
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody =
        this.adapter.fromJson(new Buffer().readFrom(broadbandConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("error", responseBody.get("type"));

    broadbandConnection.disconnect();
  }

  @Test
  public void testBroadbandFailureMissingBothParams() throws IOException {
    HttpURLConnection broadbandConnection = tryRequest("broadband");
    assertEquals(200, broadbandConnection.getResponseCode()); // successful *connection*
    Map<String, Object> responseBody =
        this.adapter.fromJson(new Buffer().readFrom(broadbandConnection.getInputStream()));
    showDetailsIfError(responseBody);
    assertEquals("error", responseBody.get("type"));

    broadbandConnection.disconnect();
  }

  /**
   * Helper to make working with a large test suite easier: if an error, print more info.
   *
   * @param body
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body.toString());
    }
  }
}
