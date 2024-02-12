package edu.brown.cs.student.main.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import spark.Request;
import spark.Response;
import spark.Route;

public class APICache implements Route {
  private LoadingCache<Request, Object> requests;

  public APICache(Route route, CacheBuilder<Object, Object> cacheBuilder) {
    this.requests = cacheBuilder.build(
        new CacheLoader<Request, Object>() {
          @Override
          public Object load(Request request) throws Exception {
            return route.handle(request, null);
          }
        });
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    return this.requests.get(request);
  }
}
