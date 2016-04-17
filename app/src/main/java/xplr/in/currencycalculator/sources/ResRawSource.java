package xplr.in.currencycalculator.sources;

import android.content.res.Resources;

import java.io.InputStream;
import java.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.App;

/**
 * Created by cheriot on 4/12/16.
 */
@Singleton
public class ResRawSource {

    private final String packageName;
    private final Resources appResources;

    @Inject
    public ResRawSource(App app) {
        packageName = app.getPackageName();
        appResources = app.getResources();
    }

    public String getString(int resourceId) {
        // Use try-with-resource when min API level rises to 19.
        InputStream inputStream = getInputStream(resourceId);
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        try {
            return scanner.useDelimiter("\\A").next();
        } finally {
            scanner.close();
        }
    }

    public InputStream getInputStream(int resourceId) {
        return appResources.openRawResource(resourceId);
    }

    public int getResourceIdFromName(String name) {
        return appResources.getIdentifier(name, "drawable", packageName);
    }
}
