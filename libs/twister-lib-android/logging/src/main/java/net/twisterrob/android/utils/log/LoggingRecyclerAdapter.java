package net.twisterrob.android.utils.log;

import java.util.List;

import org.slf4j.*;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.ViewGroup;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public abstract class LoggingRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingRecyclerAdapter.class);

	@Override public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
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
	@Override public void onViewRecycled(VH holder) {
		log("onViewRecycled", holder);
		super.onViewRecycled(holder);
	}
	@Override public boolean onFailedToRecycleView(VH holder) {
		log("onFailedToRecycleView", holder);
		return super.onFailedToRecycleView(holder);
	}
	@Override public void onViewAttachedToWindow(VH holder) {
		log("onViewAttachedToWindow", holder);
		super.onViewAttachedToWindow(holder);
	}
	@Override public void onViewDetachedFromWindow(VH holder) {
		log("onViewDetachedFromWindow", holder);
		super.onViewDetachedFromWindow(holder);
	}
	@Override public void registerAdapterDataObserver(AdapterDataObserver observer) {
		log("registerAdapterDataObserver", observer);
		super.registerAdapterDataObserver(observer);
	}
	@Override public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
		log("unregisterAdapterDataObserver", observer);
		super.unregisterAdapterDataObserver(observer);
	}
	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		log("onAttachedToRecyclerView", recyclerView);
		super.onAttachedToRecyclerView(recyclerView);
	}
	@Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		log("onDetachedFromRecyclerView", recyclerView);
		super.onDetachedFromRecyclerView(recyclerView);
	}
	@Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder", parent, viewType);
		return null;
	}
	@Override public void onBindViewHolder(VH holder, int position) {
		log("onBindViewHolder", holder, position);
	}
	@Override public int getItemCount() {
		log("getItemCount");
		return 0;
	}

	private void log(String name, Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), name, null, args);
	}
}
