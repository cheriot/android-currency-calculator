package xplr.in.currencycalculator.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * Hold a adapter.notify* call that needs to be made once the data backing the adapter has
 * been updated.
 */
public abstract class PendingNotify {
    public abstract void notify(RecyclerView.Adapter adapter);

    // notifyCalculated -> notifyItemRangeChanged with payload
    // CurrencyAmountChangeListener -> notifyDatasetChagned could be notifyItemRangeChanged (notifyCalculated?)
    // pendingNotifyCurrencyInserted -> notifyItemInserted
    // pendingNotifyCurrencyRemoved -> notifyItemRemoved
    // pendingNotifyItemMovedWithFixedRow -> notifyItemMoved
    // swipe -> notifyItemRemoved

    public static class Inserted extends PendingNotify {
        private int position;

        public Inserted(int position) {
            this.position = position;
        }

        @Override
        public void notify(RecyclerView.Adapter adapter) {
            adapter.notifyItemInserted(position);
        }
    }

    public static class Removed extends PendingNotify {
        private int position;

        public Removed(int position) {
            this.position = position;
        }

        @Override
        public void notify(RecyclerView.Adapter adapter) {
            adapter.notifyItemRemoved(position);
        }
    }

    public static class RangeChanged extends PendingNotify {
        private int position;
        private int itemCount;

        public RangeChanged(int position, int itemCount) {
            this.position = position;
            this.itemCount = itemCount;
        }

        @Override
        public void notify(RecyclerView.Adapter adapter) {
            adapter.notifyItemRangeChanged(position, itemCount);
        }
    }

    public static class Moved extends PendingNotify {
        private int fromPosition;
        private int toPosition;

        public Moved(int fromPosition, int toPosition) {
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
        }

        @Override
        public void notify(RecyclerView.Adapter adapter) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }
    }
}
