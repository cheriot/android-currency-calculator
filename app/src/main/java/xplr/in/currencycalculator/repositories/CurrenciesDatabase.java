package xplr.in.currencycalculator.repositories;

import android.database.Cursor;

import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.Query;
import com.yahoo.squidb.sql.Table;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 4/9/16.
 */
public class CurrenciesDatabase extends SquidDatabase {

    private static final String LOG_TAG = CurrenciesDatabase.class.getName();

    // Lazy to deal with a circular dependency. Maybe extract updater logic from the repository.
    private final Lazy<CurrencyRepository> lazyCurrencyRepository;

    @Inject
    public CurrenciesDatabase(App context, Lazy<CurrencyRepository> lazyCurrencyRepository) {
        super(context);
        this.lazyCurrencyRepository = lazyCurrencyRepository;
    }

    @Override
    public String getName() {
        return "currencies.db";
    }

    @Override
    protected int getVersion() {
        return 5;
    }

    @Override
    protected Table[] getTables() {
        return new Table[] { Currency.TABLE };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 3:
                tryAddColumn(Currency.NAME);
                tryAddColumn(Currency.ISSUING_COUNTRY_CODE);
                tryAddColumn(Currency.MINOR_UNITS);
        }

        // Need this call only after app code is updated, but there's no trigger for that so
        // remember to bump the database version when meta has changed.
        lazyCurrencyRepository.get().updateOrInitializeMeta();
        return true;
    }

    @Override
    protected void onMigrationFailed(MigrationFailedException failure) {
        super.onMigrationFailed(failure);
        // Blow away the entire database and set it up like new. This calls #onTablesCreated, right?
        recreate();
    }

    @Override
    protected void onTablesCreated(SQLiteDatabaseWrapper db) {
        super.onTablesCreated(db);
        lazyCurrencyRepository.get().initializeData();
    }

    /** Test helper. */
    public <T extends AbstractModel> List<T> queryAsList(Class<T> modelClass, Query query) {
        return cursorToModelList(modelClass, query(modelClass, query));
    }

    /** Test helper. */
    List<List<String>> rawAsList(String sql, String[] args) {
        return cursorToList(rawQuery(sql, args));
    }

    /** Test helper. */
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

    /** Test helper. */
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
