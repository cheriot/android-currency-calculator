package xplr.in.currencycalculator.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import xplr.in.currencycalculator.models.Currency;
import xplr.in.currencycalculator.models.Money;
import xplr.in.currencycalculator.presenters.ComparisonPresenter;

/**
 * Created by cheriot on 6/26/16.
 */
public class Analytics {

    private static final String USER_PROP_DEFAULT_LOCALE = "default_locale";
    private static final String EVENT_CLEAR_TEXT = "clear_text";
    private static final String PARAM_CLEAR_TEXT_TYPE = "clear_text_type";
    private static final String PARAM_VALUE_BASE_AMOUNT = "base_amount";
    private static final String PARAM_ACTIVITY = "activity";
    private static final String PARAM_BASE_CURRENCY_CODE = "base_currency_code";
    private static final String PARAM_TARGET_CURRENCY_CODE = "target_currency_code";
    private static final String PARAM_BASE_CURRENCY_AMOUNT = "base_amount";

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

    public RateCompareActivityAnalytics getRateCompareActivityAnalytics() {
        return new RateCompareActivityAnalytics();
    }

    public TradeCompareAnalytics getTradeCompareAnalytics() {
        return new TradeCompareAnalytics();
    }

    public SyncAnalytics getSyncAnalytics() {
        return new SyncAnalytics();
    }

    private void recordClearText(Bundle bundle, String type) {
        bundle.putString(PARAM_CLEAR_TEXT_TYPE, type);
        firebaseAnalytics.logEvent(EVENT_CLEAR_TEXT, bundle);
    }

    public class SyncAnalytics {
        private static final String EVENT_SYNC = "sync";
        private static final String PARAM_SYNC_MANUAL = "sync_manual";
        public void recordSync(boolean isManual) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(PARAM_SYNC_MANUAL, isManual);
            firebaseAnalytics.logEvent(EVENT_SYNC, bundle);
        }
    }

    public class TradeCompareAnalytics {
        private static final String PARAM_VALUE_ACTIVITY = "trade_compare";
        private static final String EVENT_TRADE_COMPARE_SUCCESS = "trade_compare_success";
        private static final String EVENT_TRADE_COMPARE_FAILURE = "trade_compare_failure";
        private static final String EVENT_INVALIDATE_RESULT = "invalidate_trade_result";
        private static final String PARAM_VALUE_TRADE_TO_COMPARE = "trade_to_compare";
        private final String paramValueActivity;

        public TradeCompareAnalytics() {
            this.paramValueActivity = PARAM_VALUE_ACTIVITY;
        }

        public TradeCompareAnalytics(String paramValueActivity) {
            this.paramValueActivity = paramValueActivity;
        }

        public void recordRateCompareSuccess(Money base, Currency target) {
            firebaseAnalytics.logEvent(EVENT_TRADE_COMPARE_SUCCESS, bundle(base.getCurrency(), target));
        }

        public void recordRateCompareFailure(Money base, Currency target) {
            firebaseAnalytics.logEvent(EVENT_TRADE_COMPARE_FAILURE, bundle(base, target));
        }

        public void recordClearTradeFor(Money base, Currency target) {
            recordClearText(bundle(base, target), PARAM_VALUE_TRADE_TO_COMPARE);
        }

        public void recordInvalidateResult(Currency base, Currency target) {
            firebaseAnalytics.logEvent(EVENT_INVALIDATE_RESULT, bundle(base, target));
        }

        private Bundle bundle(Money base, Currency target) {
            Bundle bundle = bundle(base.getCurrency(), target);
            bundle.putFloat(PARAM_BASE_CURRENCY_AMOUNT, base.getAmount().floatValue());
            return bundle;
        }

        private Bundle bundle(Currency base, Currency target) {
            Bundle bundle = bundle();
            if(base != null) bundle.putString(PARAM_BASE_CURRENCY_CODE, base.getCode());
            if(target != null) bundle.putString(PARAM_TARGET_CURRENCY_CODE, target.getCode());
            return bundle;
        }

        private Bundle bundle() {
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_ACTIVITY, paramValueActivity);
            return bundle;
        }
    }

    public class RateCompareActivityAnalytics {
        private static final String PARAM_VALUE_ACTIVITY = "rate_compare";
        private static final String PARAM_VALUE_RATE_TO_COMPARE = "rate_to_compare";
        private static final String PARAM_LHS_AMOUNT = "lhs_amount";
        private static final String PARAM_LHS_CURRENCY_CODE = "lhs_currency_code";
        private static final String EVENT_FEES_YES = "fees_yes";
        private static final String EVENT_FEES_NO = "fees_no";
        private static final String EVENT_START_RATE_COMPARE = "start_rate_compare";
        private static final String EVENT_RATE_COMPARE_SUCCESS = "rate_compare_success";
        private static final String EVENT_RATE_COMPARE_FAILURE = "rate_compare_failure";
        private static final String EVENT_RATE_LHS_SELECTED = "rate_lhs_selected";
        private static final String EVENT_SWAP_RATE = "swap_rate";
        private static final String EVENT_INVALIDATE_RESULT = "invalidate_rate_result";

        public TradeCompareAnalytics getTradeCompareAnalytics() {
            return new TradeCompareAnalytics(PARAM_VALUE_ACTIVITY);
        }

        public void recordStartRateCompare(Currency base, Currency target) {
            firebaseAnalytics.logEvent(EVENT_START_RATE_COMPARE, bundle(base, target));
        }

        public void recordFeesYes(ComparisonPresenter cp) {
            Bundle bundle = bundle(cp);
            firebaseAnalytics.logEvent(EVENT_FEES_YES, bundle);
        }

        public void recordFeesNo(ComparisonPresenter cp) {
            firebaseAnalytics.logEvent(EVENT_FEES_NO, bundle(cp));
        }

        public void recordClearBaseAmount(ComparisonPresenter cp) {
            recordClearText(bundle(cp), PARAM_VALUE_BASE_AMOUNT);
        }

        public void recordClearRate(ComparisonPresenter cp) {
            recordClearText(bundle(cp), PARAM_VALUE_RATE_TO_COMPARE);
        }

        public void recordRateCompareSuccess(ComparisonPresenter cp) {
            firebaseAnalytics.logEvent(EVENT_RATE_COMPARE_SUCCESS, bundle(cp));
        }

        public void recordRateCompareFailure(ComparisonPresenter cp) {
            firebaseAnalytics.logEvent(EVENT_RATE_COMPARE_FAILURE, bundle(cp));
        }

        public void recordLhsAmountSelected(ComparisonPresenter cp, Currency lhsCurrency, int amount) {
            Bundle bundle = bundle(cp);
            bundle.putString(PARAM_LHS_CURRENCY_CODE, lhsCurrency.getCode());
            bundle.putInt(PARAM_LHS_AMOUNT, amount);
            firebaseAnalytics.logEvent(EVENT_RATE_LHS_SELECTED, bundle);
        }

        public void recordSwapRate(ComparisonPresenter cp) {
            firebaseAnalytics.logEvent(EVENT_SWAP_RATE, bundle(cp));
        }

        public void recordInvalidateResult(ComparisonPresenter cp) {
            firebaseAnalytics.logEvent(EVENT_INVALIDATE_RESULT, bundle(cp));
        }

        private Bundle bundle(ComparisonPresenter cp) {
            return cp != null ?
                    bundle(cp.getBaseCurrency(), cp.getTargetCurrency()) : bundle();
        }

        private Bundle bundle(Currency base, Currency target) {
            Bundle bundle = bundle();
            bundle.putString(PARAM_BASE_CURRENCY_CODE, base.getCode());
            bundle.putString(PARAM_TARGET_CURRENCY_CODE, target.getCode());
            return bundle;
        }

        private Bundle bundle() {
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_ACTIVITY, PARAM_VALUE_ACTIVITY);
            return bundle;
        }
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
            recordClearText(bundle(), PARAM_VALUE_BASE_AMOUNT);
        }

        private Bundle bundle() {
            Bundle bundle = new Bundle();
            bundle.putString(PARAM_ACTIVITY, PARAM_VALUE_ACTIVITY);
            return bundle;
        }
    }
}
