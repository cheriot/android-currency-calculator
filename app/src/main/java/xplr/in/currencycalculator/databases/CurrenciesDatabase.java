package xplr.in.currencycalculator.databases;

import android.content.Context;
import android.database.Cursor;

import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.Query;
import com.yahoo.squidb.sql.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheriot on 4/9/16.
 */
public class CurrenciesDatabase extends SquidDatabase {

    // TODO Can this be done with Dagger?
    private static CurrenciesDatabase db;
    public static void init(Context context) {
        db = new CurrenciesDatabase(context);
    }
    public static CurrenciesDatabase getInstance() {
        return db;
    }

    /**
     * Create a new SquidDatabase
     *
     * @param context the Context, must not be null
     */
    public CurrenciesDatabase(Context context) {
        super(context);
    }

    @Override
    public String getName() {
        return "currencies.db";
    }

    @Override
    protected int getVersion() {
        return 1;
    }

    @Override
    protected Table[] getTables() {
        return new Table[] { Currency.TABLE };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
    }

    @Override
    protected boolean onDowngrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
    }

    @Override
    protected void onTablesCreated(SQLiteDatabaseWrapper db) {
        super.onTablesCreated(db);
        // TODO load local currency data
    }

    public <T extends AbstractModel> List<T> queryAsList(Class<T> modelClass, Query query) {
        return cursorToModelList(modelClass, query(modelClass, query));
    }

    public List<List<String>> rawAsList(String sql, String[] args) {
        return cursorToList(rawQuery(sql, args));
    }

    List<List<String>> cursorToList(Cursor cursor) {
        List<List<String>> list = new ArrayList(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                String[] columnNames = cursor.getColumnNames();
                List<String> values = new ArrayList<>(columnNames.length);
                for(String name : columnNames) {
                    values.add(cursor.getString(cursor.getColumnIndexOrThrow(name)));
                }
                list.add(values);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    <T extends AbstractModel> List<T> cursorToModelList(Class<T> modelClass, SquidCursor cursor) {
        List<T> list = new ArrayList(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                T model = modelClass.newInstance();
                model.readPropertiesFromCursor(cursor);
                list.add(model);
            }
        } catch(Exception e) {
            throw new RuntimeException("Unable to create list from cursor of type " + modelClass.getCanonicalName(), e);
        } finally {
            cursor.close();
        }
        return list;
    }
}
