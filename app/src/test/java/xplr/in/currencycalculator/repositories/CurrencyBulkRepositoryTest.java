package xplr.in.currencycalculator.repositories;

import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import dagger.Lazy;
import xplr.in.currencycalculator.App;
import xplr.in.currencycalculator.BuildConfig;
import xplr.in.currencycalculator.sources.CurrencyMetaParser;
import xplr.in.currencycalculator.sources.CurrencyMetaSource;
import xplr.in.currencycalculator.sources.ResRawRateSource;
import xplr.in.currencycalculator.sources.ResRawSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by cheriot on 4/19/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CurrencyBulkRepositoryTest {

    private CurrenciesDatabase database;
    private TelephonyManager telephonyManager;
    private CurrencyBulkRepository repository;

    @Before
    public void setUp() throws Exception {
        App app = (App) RuntimeEnvironment.application;

        Lazy<CurrencyBulkRepository> lazy = mock(Lazy.class);
        when(lazy.get()).thenReturn(mock(CurrencyBulkRepository.class));
        database = new CurrenciesDatabase(app, lazy);

        SharedPreferences mockSharedPrefs = mock(SharedPreferences.class);
        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPrefs.edit()).thenReturn(mockEditor);

        CurrencyRepository currencyRepository = new CurrencyRepository(
                mockSharedPrefs,
                database,
                new EventBus());
        CurrencyMetaRepository metaRepository = new CurrencyMetaRepository(
                new CurrencyMetaSource(app),
                new CurrencyMetaParser(),
                new ResRawSource(app));
        telephonyManager = mock(TelephonyManager.class);

        repository = new CurrencyBulkRepository(
                database, currencyRepository,
                metaRepository,
                new ResRawRateSource(app),
                null,
                telephonyManager,
                new EventBus());
    }

    @Test
    public void testInitializeData() {
        repository.initializeDefaultSelections();
    }
}
