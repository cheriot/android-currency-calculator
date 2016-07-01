package xplr.in.currencycalculator.repositories;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Criterion;
import com.yahoo.squidb.sql.Function;
import com.yahoo.squidb.sql.Query;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.CurrencyRate;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.models.OptionalMoney;
import xplr.in.currencycalculator.sources.CurrencyRateParser;
import xplr.in.currencycalculator.views.DisplayUtils;

import static android.text.TextUtils.isEmpty;

/**
 * Currency data manipulation to support UI interaction.
 *
 * Created by cheriot on 4/1/16.
 */
@Singleton
public class CurrencyRepository {

    private static final String LOG_TAG = CurrencyRepository.class.getSimpleName();
    public static final int BASE_CURRENCY_POSITION = 1;
    public static final int TARGET_CURRENCY_POSITION = BASE_CURRENCY_POSITION + 1;

    private final SharedPreferences appSharedPrefs;
    private final CurrenciesDatabase database;
    private final EventBus eventBus;

    @Inject
    public CurrencyRepository(SharedPreferences appSharedPrefs,
                              CurrenciesDatabase database,
                              EventBus eventBus) {
        this.appSharedPrefs = appSharedPrefs;
        this.database = database;
        this.eventBus = eventBus;
    }

    static final Query VALID_CURRENCIES = Query
            .select()
            .from(Currency.TABLE)
            .where(Currency.RATE.isNot("0"))
            .freeze();

    static final Criterion SELECTED_CRITERION = Currency.POSITION.isNotNull();
    static final Query SELECTED_CURRENCIES = VALID_CURRENCIES
            .where(SELECTED_CRITERION)
            .orderBy(Currency.POSITION.asc())
            .freeze();

    static final Query CALCULATED_CURRENCIES = SELECTED_CURRENCIES
            .where(Currency.POSITION.gt(BASE_CURRENCY_POSITION))
            .freeze();

    static final Query BASE_CURRENCY = SELECTED_CURRENCIES
            .where(Currency.POSITION.eq(BASE_CURRENCY_POSITION))
            .freeze();

    static final Query TARGET_CURRENCY = SELECTED_CURRENCIES
            .where(Currency.POSITION.eq(TARGET_CURRENCY_POSITION))
            .freeze();

    public Cursor findSelectedCursor() {
        return database.query(Currency.class, SELECTED_CURRENCIES);
    }

    private static final Query SEARCH_CURRENCIES = VALID_CURRENCIES.orderBy(Currency.NAME.asc()).freeze();
    public Cursor searchAllCursor(String query) {
        Log.v(LOG_TAG, "searchAllCursor for "+ query);
        if(query != null) query = query.trim();

        Query search = SEARCH_CURRENCIES.fork();
        if(!isEmpty(query)) {
            String fieldPrefix = query + "%";
            String wordPrefix = "% " + query + "%";
            search.where(
                    Currency.CODE.like(fieldPrefix)
                            .or(Function.lower(Currency.NAME).like(fieldPrefix.toLowerCase()))
                            .or(Function.lower(Currency.NAME).like(wordPrefix.toLowerCase())));
        }
        return database.query(Currency.class, search);
    }

    public int countSelected() {
        return database.count(Currency.class, SELECTED_CRITERION);
    }

    public Currency updateSelection(long id, boolean isSelected) {
        Currency currency = database.fetch(Currency.class, id, Currency.PROPERTIES);
        Log.v(LOG_TAG, "updateSelection " + isSelected + " " + currency.toString());
        if (isSelected) {
            insertAtPosition(2, currency);
        } else {
            deselectCurrency(currency);
            publishDataChange("remove selected");
        }
        return currency;
    }

    void deselectCurrency(Currency currency) {
        if(currency.getPosition() == null) return;
        // Move to the end of the list so currencies below the starting position will move up.
        insertAtPosition(Integer.MAX_VALUE, currency, false);
        currency.setPosition(null);
        database.persist(currency);
    }

    private static final String SHIFT_LIST_SQL = "update currencies set position = position";
    public void insertAtPosition(int newPosition, Currency currency) {
        insertAtPosition(newPosition, currency, true);
    }

    public void insertAtPosition(int newPosition, Currency currency, boolean notify) {
        Integer startPos = currency.getPosition();

        if (startPos != null) {
            StringBuilder sql = new StringBuilder(SHIFT_LIST_SQL);
            String[] args = new String[] {Integer.toString(newPosition), startPos.toString()};
            if(startPos < newPosition) {
                // Move down
                // startPosition 2, newPosition 5, first shift 3, 4, 5 up by one
                sql.append("-1 where position <= ? and position > ?");
                database.tryExecSql(sql.toString(), args);
            } else if (startPos > newPosition) {
                // Move up
                // startPosition 5, newPosition 2, first shift 2, 3, 4 down by one
                sql.append("+1 where position >= ? and position < ?");
                database.tryExecSql(sql.toString(), args);
            }

        } else {
            // Initial insert
            // newPosition 3, first shift 3, 4, 5, etc down by one
            String sql = SHIFT_LIST_SQL + "+1 where position >= ?";
            database.tryExecSql(sql.toString(), new String[] {Integer.toString(newPosition)});
        }

        currency.setPosition(newPosition);
        database.persist(currency);

        Log.v(LOG_TAG, "insertAtPosition  " + newPosition + " " + currency.toString());
        if(notify) publishDataChange("insertAtPosition");
    }

    public OptionalMoney findBaseMoney() {
        return constructBaseMoney(findBaseCurrency());
    }

    public Currency findBaseCurrency() {
        return database.fetchByQuery(Currency.class, BASE_CURRENCY);
    }

    public OptionalMoney instantiateBaseMoney(SquidCursor cursor) {
        Currency baseCurrency = new Currency(cursor);
        if(baseCurrency.getPosition() != BASE_CURRENCY_POSITION) {
            String msg = "Cannot construct baseMoney out of the currency " + baseCurrency;
            throw new IllegalStateException(msg);
        }
        return constructBaseMoney(baseCurrency);
    }

    private static final String BASE_CURRENCY_AMOUNT_KEY = "base_currency_amount";
    public OptionalMoney constructBaseMoney(Currency baseCurrency) {

        String amount = appSharedPrefs.getString(BASE_CURRENCY_AMOUNT_KEY, null);
        return new OptionalMoney(baseCurrency, amount);
    }

    public Currency findTargetCurrency() {
        return database.fetchByQuery(Currency.class, TARGET_CURRENCY);
    }

    public synchronized void setBaseMoney(Money money) {
        // synchronized so setting positions and amount will be atomic
        setBaseAmount(money.getCurrency(), money.getAmount().toString());
        insertAtPosition(1, money.getCurrency());
    }

    public synchronized void setBaseMoney(OptionalMoney optionalMoney) {
        // synchronized so setting positions and amount will be atomic
        setBaseAmount(optionalMoney.getCurrency(), optionalMoney.getAmount());
        insertAtPosition(1, optionalMoney.getCurrency());
    }

    public void setBaseAmount(Currency baseCurrency, String amount) {
        Log.v(LOG_TAG, "setBaseAmount " + baseCurrency.getCode() + " " + amount);
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putString(BASE_CURRENCY_AMOUNT_KEY, DisplayUtils.stripFormatting(amount));
        editor.apply();
    }

    public Currency findByCode(String code) {
        return findByCode(Currency.class, code);
    }

    public Currency findByCodeIncludeInvalid(String code) {
        Query q = Query.select().from(Currency.TABLE).where(Currency.CODE.eq(code));
        return database.fetchByQuery(Currency.class, q);
    }

    <T extends Currency> T findByCode(Class<T> modelClass, String code) {
        Query query = VALID_CURRENCIES.where(Currency.CODE.eq(code));
        return database.fetchByQuery(modelClass, query);
    }

    void publishDataChange(String sourceName) {
        // TODO Can this be triggered automatically by squidb's SimpleDataChangedNotifier?
        Log.v(LOG_TAG, "publishDataChange " + sourceName);
        this.eventBus.post(new CurrencyDataChangeEvent());
    }

    private static List<CurrencyRate> parseCurrencyJson(String json) {
        return new CurrencyRateParser().parse(json);
    }
}
