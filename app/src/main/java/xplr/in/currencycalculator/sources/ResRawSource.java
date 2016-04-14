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

    protected String getString(int resourceId) {
        // Use try-with-resource when min API level rises to 19.
        InputStream inputStream = getInputStream(resourceId);
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        try {
            return scanner.useDelimiter("\\A").next();
        } finally {
            scanner.close();
        }
    }

    protected InputStream getInputStream(int resourceId) {
        return appResources.openRawResource(resourceId);
    }
}
