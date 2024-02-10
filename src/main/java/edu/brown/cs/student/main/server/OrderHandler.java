package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.soup.Soup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handler class for the soup ordering API endpoint.
 *
 * <p>This endpoint is similar to the endpoint(s) you'll need to create for Sprint 2. It takes a
 * basic GET request with no Json body, and returns a Json object in reply. The responses are more
 * complex, but this should serve as a reference.
 */
// TODO 2: Check out this Handler. What does it do right now? How is the menu formed (deserialized)?
public class OrderHandler implements Route {
  private final List<Soup> menu;

  /**
   * Constructor accepts some shared state
   *
   * @param menu the shared state (note: we *DON'T* want to make a defensive copy here!
   */
  public OrderHandler(List<Soup> menu) {
    this.menu = menu;
  }

  /**
   * Pick a convenient soup and make it. the most "convenient" soup is the first recipe we find in
   * the unordered set of recipe cards.
   *
   * <p>NOTE: beware this "return Object" and "throws Exception" idiom. We need to follow it because
   * the library uses it, but in general this lowers the protection of the type system.
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return response content
   * @throws Exception This is part of the interface; we don't have to throw anything.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    // TODO 2: Right now, we only serialize the first soup, let's make it so you can choose which
    // one you want!
    // Get Query parameters, can be used to make your search more specific
    String soupName = request.queryParams("soupname");
    // Initialize a map for our informative response.
    Map<String, Object> responseMap = new HashMap<>();
    // Iterate through the soups in the menu and return the first one
    for (Soup soup : this.menu) {
      if (soup.getSoupName().equals(soupName)) {
        responseMap.put(soup.getSoupName(), soup);
        responseMap.put("Number of ingredients", soup.getIngredients().size());
        return new SoupSuccessResponse(responseMap).serialize();
      }
    }
    return new SoupNoRecipesFailureResponse().serialize();
  }

  /*
   * Ultimately up to you how you want to structure your success and failure responses, but they
   * should be distinguishable in some form! We show one form here and another form in ActivityHandler
   * and you are also free to do your own way!
   */

  /** Response object to send, containing a soup with certain ingredients in it */
  public record SoupSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public SoupSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<SoupSuccessResponse> adapter = moshi.adapter(SoupSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // For debugging purposes, show in the console _why_ this fails
        // Otherwise we'll just get an error 500 from the API in integration
        // testing.
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send if someone requested soup from an empty Menu */
  public record SoupNoRecipesFailureResponse(String response_type) {
    public SoupNoRecipesFailureResponse() {
      this("error");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SoupNoRecipesFailureResponse.class).toJson(this);
    }
  }
}
