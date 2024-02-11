package edu.brown.cs.student.main.activity;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

/**
 * This class shows a possible implementation of deserializing JSON from the BoredAPI into an
 * Activity.
 */
public class ActivityAPIUtilities {

  /**
   * Deserializes JSON from the BoredAPI into an Activity object.
   *
   * @param jsonActivity
   * @return
   */
  public static Activity deserializeActivity(String jsonActivity) {
    try {
      // Initializes Moshi
      Moshi moshi = new Moshi.Builder().build();

      // Initializes an adapter to an Activity class then uses it to parse the JSON.
      JsonAdapter<Activity> adapter = moshi.adapter(Activity.class);

      Activity activity = adapter.fromJson(jsonActivity);

      return activity;
    }
    // Returns an empty activity... Probably not the best handling of this error case...
    // Notice an alternative error throwing case to the one done in OrderHandler. This catches
    // the error instead of pushing it up.
    catch (IOException e) {
      e.printStackTrace();
      return new Activity();
    }
  }
}
