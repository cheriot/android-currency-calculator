package xplr.in.currencycalculator.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

/**
 * Created by cheriot on 6/29/16.
 */
public class SelectedCurrencyItemAnimator extends DefaultItemAnimator {

    private static final String LOG_TAG = SelectedCurrencyItemAnimator.class.getSimpleName();

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        boolean result = super.animateAdd(holder);
        Log.v(LOG_TAG, "animateAdd " + holder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        boolean result = super.animateMove(holder, fromX, fromY, toX, toY);
        Log.v(LOG_TAG, "animateMove " + holder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        boolean result = super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
        Log.v(LOG_TAG, "animateChange " + oldHolder.getAdapterPosition() + " to " + newHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
        boolean result = super.canReuseUpdatedViewHolder(viewHolder, payloads);
        Log.v(LOG_TAG, "canReuseUpdatedViewHolder(2) " + viewHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        boolean result = super.canReuseUpdatedViewHolder(viewHolder);
        Log.v(LOG_TAG, "canReuseUpdatedViewHolder(1) " + viewHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        boolean result = super.animateDisappearance(viewHolder, preLayoutInfo, postLayoutInfo);
        Log.v(LOG_TAG, "animateDisappearance " + viewHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        boolean result = super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo);
        Log.v(LOG_TAG, "animateAppearance " + viewHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        boolean result = super.animatePersistence(viewHolder, preInfo, postInfo);
        Log.v(LOG_TAG, "animatePersistence " + viewHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        boolean result = super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        Log.v(LOG_TAG, "animateChange " + oldHolder.getAdapterPosition() + " to " + newHolder.getAdapterPosition() + " " + result);
        return result;
    }
}
