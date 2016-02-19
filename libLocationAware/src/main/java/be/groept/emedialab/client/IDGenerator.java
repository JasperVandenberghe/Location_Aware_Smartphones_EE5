package be.groept.emedialab.client;

import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Generates a unique ID for this device.
 */
public class IDGenerator {
    /**
     * Checks if a unique id is present for this device.
     * If not a new unique id is generated and stored in the sharedPreferences.
     *
     * @param preferences A SharedPreferences instance for the current device.
     *
     * @return The generated ID.
     */
    public static String generate(SharedPreferences preferences){
        //Check the deviceID and create a new one when not set (=empty String).
        String deviceId = preferences.getString("PREFS_DEVICE_ID", "");
        if (deviceId.equals("")) {
            UUID uuid = UUID.randomUUID();
            deviceId = uuid.toString();
            // Write the value out to the prefs file
            /*Check http://stackoverflow.com/questions/5960678/whats-the-difference-between-commit-and-apply-in-shared-preference
            for difference between commit() and apply()*/
            preferences.edit()
                    .putString("PREFS_DEVICE_ID", deviceId)
                    .apply();
        }
        return deviceId;
    }
}
