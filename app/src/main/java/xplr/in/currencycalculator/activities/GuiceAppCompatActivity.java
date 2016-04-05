package xplr.in.currencycalculator.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.inject.Key;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.util.RoboContext;

/**
 * Created by cheriot on 4/5/16.
 */
public class GuiceAppCompatActivity extends AppCompatActivity implements RoboContext {


    private HashMap<Key<?>, Object> scopedObjects = new HashMap<Key<?>, Object>();

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // RoboGuice doesn't have a base class equivalent for AppCompatActivity and I don't
        // want the event manager features.
        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        RoboGuice.getInjector( this ).injectViewMembers(this);
    }

    @Override
    protected void onDestroy() {
        try {
            RoboGuice.destroyInjector( this );
        }
        finally {
            super.onDestroy();
        }
    }
}
