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
    private final int fixedPosition;

    public SelectedCurrencyItemAnimator(int fixedPosition) {
        this.fixedPosition = fixedPosition;
        setSupportsChangeAnimations(true);
        setAddDuration(1300);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        boolean result = super.animateAdd(holder);
        Log.v(LOG_TAG, "animateAdd " + holder.getAdapterPosition() + " " + result);
        return result;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        if(fixedPosition == holder.getAdapterPosition()) {
            Log.v(LOG_TAG, "animateMove skipped");
            return false;
        }
        boolean result = super.animateMove(holder, fromX, fromY, toX, toY);
        if(holder instanceof SelectedCurrencyAdapter.CurrencyViewHolder) {
            SelectedCurrencyAdapter.CurrencyViewHolder cvh = (SelectedCurrencyAdapter.CurrencyViewHolder)holder;
            cvh.updateTypeForPosition();
        }
        Log.v(LOG_TAG, "animateMove " + holder.getItemId() + " @ " + holder.getAdapterPosition() + " " + result + " {" + fromX + "," + fromY + "} to {" + toX + "," + toY + "}");
        return result;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        boolean result = super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
        // Fade in new numbers?
        Log.v(LOG_TAG, "animateChange inner " + oldHolder.getItemId() + " @ " + oldHolder.getAdapterPosition() + " to " + newHolder.getItemId() + " @ " + newHolder.getAdapterPosition() + " " + result + " {" + fromX + "," + fromY + "} to {" + toX + "," + toY + "}");
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
        Log.v(LOG_TAG, "animateChange outer " + oldHolder.getAdapterPosition() + " to " + newHolder.getAdapterPosition() + " " + result);
        return result;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state, @NonNull RecyclerView.ViewHolder viewHolder, int changeFlags, @NonNull List<Object> payloads) {
        ItemHolderInfo result = super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
        Log.v(LOG_TAG, "recordPreLayoutInformation " + viewHolder.getOldPosition() + " to " + viewHolder.getAdapterPosition() + " " + viewHolder.itemView.getTop() + " - " + viewHolder.getItemId() + " " + viewHolder.getClass().getSimpleName());
        return result;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPostLayoutInformation(@NonNull RecyclerView.State state, @NonNull RecyclerView.ViewHolder viewHolder) {
        ItemHolderInfo result = super.recordPostLayoutInformation(state, viewHolder);
        Log.v(LOG_TAG, "recordPostLayoutInformation " + viewHolder.getOldPosition() + " to " + viewHolder.getAdapterPosition() + " " + viewHolder.itemView.getTop() + " - " + viewHolder.getItemId() + " " + viewHolder.getClass().getSimpleName());
        return result;
    }
}
