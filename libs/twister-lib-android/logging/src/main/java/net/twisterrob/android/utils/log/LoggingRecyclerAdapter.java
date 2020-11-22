package net.twisterrob.android.utils.log;

import java.util.List;

import org.slf4j.*;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public abstract class LoggingRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingRecyclerAdapter.class);

	@Override public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
		log("onBindViewHolder", holder, position, payloads);
		super.onBindViewHolder(holder, position, payloads);
	}
	@Override public int getItemViewType(int position) {
		log("getItemViewType", position);
		return super.getItemViewType(position);
	}
	@Override public void setHasStableIds(boolean hasStableIds) {
		log("setHasStableIds", hasStableIds);
		super.setHasStableIds(hasStableIds);
	}
	@Override public long getItemId(int position) {
		log("getItemId", position);
		return super.getItemId(position);
	}
	@Override public void onViewRecycled(@NonNull VH holder) {
		log("onViewRecycled", holder);
		super.onViewRecycled(holder);
	}
	@Override public boolean onFailedToRecycleView(@NonNull VH holder) {
		log("onFailedToRecycleView", holder);
		return super.onFailedToRecycleView(holder);
	}
	@Override public void onViewAttachedToWindow(@NonNull VH holder) {
		log("onViewAttachedToWindow", holder);
		super.onViewAttachedToWindow(holder);
	}
	@Override public void onViewDetachedFromWindow(@NonNull VH holder) {
		log("onViewDetachedFromWindow", holder);
		super.onViewDetachedFromWindow(holder);
	}
	@Override public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
		log("registerAdapterDataObserver", observer);
		super.registerAdapterDataObserver(observer);
	}
	@Override public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
		log("unregisterAdapterDataObserver", observer);
		super.unregisterAdapterDataObserver(observer);
	}
	@Override public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		log("onAttachedToRecyclerView", recyclerView);
		super.onAttachedToRecyclerView(recyclerView);
	}
	@Override public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		log("onDetachedFromRecyclerView", recyclerView);
		super.onDetachedFromRecyclerView(recyclerView);
	}
	@Override public @NonNull VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		log("onCreateViewHolder", parent, viewType);
		return null;
	}
	@Override public void onBindViewHolder(@NonNull VH holder, int position) {
		log("onBindViewHolder", holder, position);
	}
	@Override public int getItemCount() {
		log("getItemCount");
		return 0;
	}

	private void log(@NonNull String name, @NonNull Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), name, null, args);
	}
}
