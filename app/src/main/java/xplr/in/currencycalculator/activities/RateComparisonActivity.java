package xplr.in.currencycalculator.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 5/24/16.
 */
public class RateComparisonActivity extends AppCompatActivity {

    private static final String LOG_TAG = RateComparisonActivity.class.getSimpleName();
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.rate_form) View rateForm;
    @Bind(R.id.trade_form) View tradeForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_comparison);

        // ((App)getApplication()).newActivityScope(this).inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Grab the base currency
        // Grab the desired currency
        // Hide/show based on fees and taxes
        // Accept rate input
        // Calculate!

        // Better display:
        // Use material guidelines to show example text while typing (market exchange rate)
        //
    }

    public void setFeesYes(View view) {
        Log.v(LOG_TAG, "setFeesYes");
        rateForm.setVisibility(View.GONE);
        tradeForm.setVisibility(View.VISIBLE);
    }

    public void setFeesNo(View view) {
        Log.v(LOG_TAG, "setFeesNo");
        rateForm.setVisibility(View.VISIBLE);
        tradeForm.setVisibility(View.GONE);
    }
}
