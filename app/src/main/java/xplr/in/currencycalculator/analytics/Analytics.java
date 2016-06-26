package xplr.in.currencycalculator.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import xplr.in.currencycalculator.models.Currency;

/**
 * Created by cheriot on 6/26/16.
 */
public class Analytics {

    private static final String USER_PROP_DEFAULT_LOCALE = "default_locale";
    private static final String EVENT_CLEAR_TEXT = "clear_text";
    private static final String PARAM_ACTIVITY = "activity";

    private FirebaseAnalytics firebaseAnalytics;

    public Analytics(Context context){
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void recordUserDefaultLocale() {
        firebaseAnalytics.setUserProperty(USER_PROP_DEFAULT_LOCALE, Locale.getDefault().toString());
    }

    public MainActivityAnalytics getMainActivityAnalytics() {
        return new MainActivityAnalytics();
    }

    public class RateCompareActivityAnalytics {
        private static final String PARAM_BASE_CURRENCY_CODE = "base_currency_code";
        private static final String PARAM_TARGET_CURRENCY_CODE = "target_currency_code";
        private static final String EVENT_FEES_YES = "fees_yes";
        private static final String EVENT_FEES_NO = "fees_no";
    }

    public class MainActivityAnalytics {
        private static final String PARAM_CURRENCY_CODE = "currency_code";
        private static final String PARAM_VALUE_ACTIVITY = "main";
        private static final String EVENT_FAB_CLICK = "fab_click";
        private static final String EVENT_REFRESH_REQUEST = "refresh_request";
        private static final String EVENT_SWIPE_REMOVED_CURRENCY = "swipe_removed_currency";
        private static final String EVENT_SELECT_CURRENCY = "select_currency";
        private static final String EVENT_EXCHANGE_BUTTON = "exchange_button";
        private static final String EVENT_TRADE_BUTTON = "trade_button";

        public void recordFabClick() {
            firebaseAnalytics.logEvent(EVENT_FAB_CLICK, bundle());
        }

        public void recordRefreshRequested() {
            firebaseAnalytics.logEvent(EVENT_REFRESH_REQUEST, bundle());
        }

        public void recordSwipeRemovedCurrency(Currency currency) {
            Bundle bundle = bundle();
            bundle.putString(PARAM_CURRENCY_CODE, currency.getCode());
            firebaseAnalytics.logEvent(EVENT_SWIPE_REMOVED_CURRENCY, bundle);
        }

        public void recordSelectCurrency(Currency currency) {
            Bundle bundle = bundle();
            bundle.putString(PARAM_CURRENCY_CODE, currency.getCode());
            firebaseAnalytics.logEvent(EVENT_SELECT_CURRENCY, bundle);
        }

        public void recordExchangeButtonPress() {
            firebaseAnalytics.logEvent(EVENT_EXCHANGE_BUTTON, bundle());
        }

        public void recordTradeButtonPress() {
            firebaseAnalytics.logEvent(EVENT_TRADE_BUTTON, bundle());
        }

        public void recordClearBaseAmount() {
            firebaseAnalytics.logEvent(EVENT_CLEAR_TEXT, bundle());
        }

        private Bundle bundle() {
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_ACTIVITY, PARAM_VALUE_ACTIVITY);
            return bundle;
        }
    }
}
