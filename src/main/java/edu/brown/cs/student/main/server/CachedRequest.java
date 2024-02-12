package edu.brown.cs.student.main.server;

import java.util.Set;
import spark.Request;

// src:
// https://stackoverflow.com/questions/12284294/custom-equals-hash-when-inserting-key-guava-cache
/**
 * Proxy class to override equals and hashCode, so Cache recognizes Request is the same as another.
 */
public class CachedRequest extends Request {

  private Request request;
  private Set<String> params;

  public CachedRequest(Request request) {
    this.request = request;
    this.params = request.queryParams();
  }

  public String queryParams(String key) {
    return this.request.queryParams(key);
  }

  @Override
  public int hashCode() {
    return this.params.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    // src: https://www.sitepoint.com/implement-javas-equals-method-correctly/
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CachedRequest o = (CachedRequest) obj;
    return o.params.equals(this.params);
  }
}
