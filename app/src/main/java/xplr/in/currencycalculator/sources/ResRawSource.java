package xplr.in.currencycalculator.sources;

import android.app.Application;
import android.content.res.Resources;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by cheriot on 4/12/16.
 */
public class ResRawSource {
    private final Resources appResources;

    public ResRawSource(Application context) {
        appResources = context.getResources();
    }

    public String get(int resourceId) {
        // Use try-with-resource when min API level rises to 19.
        InputStream inputStream = appResources.openRawResource(resourceId);
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        try {
            return scanner.useDelimiter("\\Z").next();
        } finally {
            scanner.close();
        }
    }
}
