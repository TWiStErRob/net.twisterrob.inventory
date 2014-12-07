package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.content.res.Resources;
import android.support.annotation.PluralsRes;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.Action;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.inventory.android.utils.Plurals.*;

public abstract class DeleteAction extends BaseAction {
	private final int targetNameRes;
	private final int childNameRes;
	protected final long[] IDs;

	protected Collection<String> targets;
	protected final Collection<String> children = new TreeSet<>();

	public DeleteAction(@PluralsRes int targetNameRes, @PluralsRes int childNameRes, long... IDs) {
		if (IDs == null || IDs.length == 0) {
			throw new IllegalArgumentException("Nothing to delete.");
		}

		this.targetNameRes = targetNameRes;
		this.childNameRes = childNameRes;
		this.IDs = IDs;
	}

	protected String getTargetPlural(Resources res, @PluralsRes int titleRes) {
		String name = res.getQuantityString(targetNameRes, targets.size());
		return res.getQuantityString(titleRes, targets.size(), toPluralArgs(targets, name));
	}

	@Override public String getConfirmationTitle(Resources res) {
		return getTargetPlural(res, R.plurals.action_delete_title);
	}

	@Override public String getConfirmationMessage(Resources res) {
		StringBuilder sb = new StringBuilder();
		if (children.isEmpty()) {
			sb.append(getTargetPlural(res, R.plurals.action_delete_confirm_empty));
		} else {
			sb.append(getTargetPlural(res, R.plurals.action_delete_confirm));
		}
		if (!children.isEmpty()) {
			sb.append("\n\n");
			sb.append(res.getQuantityString(R.plurals.action_delete_details, children.size(),
					children.size(),
					res.getQuantityString(childNameRes, children.size()),
					StringTools.join(children, ", ")
			));
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage(Resources res) {
		return getTargetPlural(res, R.plurals.action_delete_success);
	}

	@Override public String getFailureMessage(Resources res) {
		return getTargetPlural(res, R.plurals.action_delete_failed);
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// no undo
	}
}
