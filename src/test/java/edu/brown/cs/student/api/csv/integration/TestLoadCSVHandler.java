package edu.brown.cs.student.api.csv.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * An INTEGRATION TEST differs from a UNIT TEST in that it's testing
 * a combination of code units and their combined behavior.
 *
 * Test our API server: send real web requests to our server as it is
 * running. Note that for these, we prefer to avoid sending many
 * real API requests to the NWS, and use "mocking" to avoid it.
 * (There are many other reasons to use mock data here. What are they?)
 *
 * In short, there are two new techniques demonstrated here:
 * writing tests that send fake API requests; and
 * testing with mock data / mock objects.
 */
public class TestLoadCSVHandler {

    @BeforeAll
    public static void setupOnce() {
        Spark.port(0); // Pick an arbitrary free port
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root
    }

    private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
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

    /**
     * Helper to start a connection to a specific API endpoint/params
     *
     * The "throws" clause doesn't matter below -- JUnit will fail if an
     *     exception is thrown that hasn't been declared as a parameter to @Test.
     *
     * @param apiCall the call string, including endpoint
     *                (Note: this would be better if it had more structure!)
     * @return the connection for the given URL, just after connecting
     * @throws IOException if the connection fails for some reason
     */
    private HttpURLConnection tryRequest(String apiCall) throws IOException {
        // Configure the connection (but don't actually send a request yet)
        URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
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
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("success", responseBody.get("type"));

        assertEquals(filepath, responseBody.get("filepath"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadMultipleCSVs() throws IOException {
        String filepath = "data/stars/stardata.csv";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("success", responseBody.get("type"));

        assertEquals(filepath, responseBody.get("filepath"));

        loadConnection.disconnect();

        filepath = "data/census/postsecondary_education.csv";
        loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*
        responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("success", responseBody.get("type"));

        assertEquals(filepath, responseBody.get("filepath"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadDataNonExistantCSV() throws IOException {
        String filepath = "data/stars/nonexistantcsv.csv";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadDataNonCSVFile() throws IOException {
        String filepath = "data/menu.json";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadDataMalformedCSV() throws IOException {
        String filepath = "data/malformed/malformed_signs.csv";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadCSVOutsideDirectory() throws IOException {
        String filepath = "data/../../test.csv";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();

        filepath = "../test.csv";
        loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();

        filepath = "..";
        loadConnection = tryRequest("loadcsv?filepath="+filepath);
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }


    @Test
    public void testLoadDataNoParamsRequest() throws IOException {
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv");
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadDataEmptyParam() throws IOException {
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=");
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadNonExistentParam() throws IOException {
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filpath=data/abc.csv");
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("error", responseBody.get("type"));

        loadConnection.disconnect();
    }

    @Test
    public void testLoadExtraParam() throws IOException {
        String filepath = "data/census/income_by_race.csv";
        // request loadcsv
        HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + filepath + "&hello=hi");
        assertEquals(200, loadConnection.getResponseCode()); // successful *connection*

        Map<String, Object> responseBody = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        showDetailsIfError(responseBody);
        assertEquals("success", responseBody.get("type"));

        loadConnection.disconnect();
    }

    // TODO: test for csv they want us to load in docs. make sure to also test with search and view

    /**
     * Helper to make working with a large test suite easier: if an error, print more info.
     * @param body
     */
    private void showDetailsIfError(Map<String, Object> body) {
        if(body.containsKey("type") && "error".equals(body.get("type"))) {
            System.out.println(body.toString());
        }
    }


}
