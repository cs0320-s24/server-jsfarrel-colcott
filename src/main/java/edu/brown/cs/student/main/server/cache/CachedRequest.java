package edu.brown.cs.student.main.server.cache;

import spark.Request;

// src:
// https://stackoverflow.com/questions/12284294/custom-equals-hash-when-inserting-key-guava-cache
/**
 * Proxy class to override equals and hashCode, so Cache recognizes Request is the same as another.
 */
public class CachedRequest extends Request {

  private final Request request;
  private final String paramsIdentifier;

  /**
   * Create CachedRequest
   *
   * @param request is request to proxy
   */
  public CachedRequest(Request request) {
    this.request = request;

    StringBuilder s = new StringBuilder();
    for (String param : request.queryParams()) {
      s.append(param).append(request.queryParams(param));
    }
    this.paramsIdentifier = s.toString();
  }

  /**
   * Re-implement queryParams for proxy
   *
   * @param key is a parameter with a value attached
   * @return value attached to key
   */
  public String queryParams(String key) {
    return this.request.queryParams(key);
  }

  /**
   * Override hashCode so hashed by just Request params
   *
   * @return integer hash code
   */
  @Override
  public int hashCode() {
    return this.paramsIdentifier.hashCode();
  }

  /**
   * Override equals so equality compared by Request params
   *
   * @return true if equal to obj, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    // src: https://www.sitepoint.com/implement-javas-equals-method-correctly/
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    CachedRequest o = (CachedRequest) obj;
    return o.paramsIdentifier.equals(this.paramsIdentifier);
  }
}
