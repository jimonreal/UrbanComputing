package io.github.jimonreal.urbancomputing.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jmonreal on 18/02/18.
 */

public class Message {

    public static String getAccelerometerMessage(float G[]) {
        DateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String isoTimestamp = isoDateTimeFormat.format(new Date());
        if (!isoTimestamp.endsWith("Z")) {
            int pos = isoTimestamp.length() - 2;
            isoTimestamp = isoTimestamp.substring(0, pos) + ':' + isoTimestamp.substring(pos);
        }
        return "{ \"d\": {" +
                "\"acceleration_x\":" + G[0] + ", " +
                "\"acceleration_y\":" + G[1] + ", " +
                "\"acceleration_z\":" + G[2] + ", " +
                "\"timestamp\":\"" + isoTimestamp + "\" " +
                "} }";
    }
}
