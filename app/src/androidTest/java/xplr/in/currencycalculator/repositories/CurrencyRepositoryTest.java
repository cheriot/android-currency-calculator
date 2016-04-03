package xplr.in.currencycalculator.repositories;

import android.test.AndroidTestCase;

import com.orm.SugarContext;
import com.orm.SugarRecord;

import org.junit.Test;

import java.util.Iterator;

import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 4/3/16.
 */
public class CurrencyRepositoryTest extends AndroidTestCase {

    public CurrencyRepositoryTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        SugarContext.init(getContext());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        SugarContext.terminate();
    }

    @Test
    public void testFetchAll() {
        // TODO don't access the network in a test
        long a = SugarRecord.count(Currency.class);
        CurrencyRepository.fetchAll();
        Iterator<Currency> currencyItr = SugarRecord.findAll(Currency.class);
        long count = SugarRecord.count(Currency.class);
        assertEquals(163, count);
    }
}
