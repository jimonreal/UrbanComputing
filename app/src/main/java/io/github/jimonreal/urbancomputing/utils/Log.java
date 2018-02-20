package io.github.jimonreal.urbancomputing.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by jmonreal on 18/02/18.
 */

public class Log {
    private static String filename = "LogData-20022018.csv";

    public static void sensorDataToCSV(Context context, float[] G, double la, double lo, double al, double ac, float battery, long timestamp) {
        try {
            File filePath = new File(context.getFilesDir(), "UrbanComputing");
            System.out.println(filePath.getAbsolutePath());

            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            File file = new File(filePath, filename);
            if (!file.exists()) {
                //System.out.println("File " + filePath.getAbsolutePath() + "/" + filename + "does not exist!");
                file.createNewFile();
            }

            OutputStreamWriter csvFile = new OutputStreamWriter(new FileOutputStream(file, true));

            csvFile.append(""
                    + String.valueOf(timestamp) + ";"
                    + String.valueOf(G[0]) + ";"
                    + String.valueOf(G[1]) + ";"
                    + String.valueOf(G[2]) + ";"
                    + String.valueOf(la) + ";"
                    + String.valueOf(lo) + ";"
                    + String.valueOf(al) + ";"
                    + String.valueOf(ac) + ";"
                    + String.valueOf(battery) + ";"
                    + "\n"
            );

            csvFile.flush();
            csvFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
