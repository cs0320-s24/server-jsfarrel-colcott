package edu.brown.cs.student.main.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import spark.Request;
import spark.Response;
import spark.Route;

/** APICache is a wrapper class to an endpoint handler. */
public class APICache implements Route {
  private final LoadingCache<CachedRequest, Object> requests;

  /**
   * APICache constructor takes in Route and CacheBuilder. The cacheBuilder is built with load()
   * going to route.handle().
   *
   * @param route is the endpoint handler we are wrapping
   * @param cacheBuilder is the configuration for the cache
   */
  public APICache(Route route, CacheBuilder<Object, Object> cacheBuilder) {
    this.requests =
        cacheBuilder.build(
            new CacheLoader<>() {
              @Override
              public Object load(CachedRequest request) throws Exception {
                return route.handle(request, null);
              }
            });
  }

  /**
   * Handle request for a given endpoint. Redirects to the defined cache (and route if undefined).
   *
   * @param request is the endpoint request
   * @param response is the endpoint response
   * @return Object in response to the request
   * @throws Exception any errors that may be thrown while handling request
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    return this.requests.get(new CachedRequest(request));
  }
}
