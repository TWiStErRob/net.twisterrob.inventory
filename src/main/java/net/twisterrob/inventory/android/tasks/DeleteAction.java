package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.content.res.Resources;
import android.support.annotation.PluralsRes;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.Action;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.inventory.android.utils.Plurals.*;

public abstract class DeleteAction extends BaseAction {
	protected final long[] IDs;
	private final int targetNameRes;
	private final int childNameRes;
	private final boolean affectsChildren;

	protected Collection<String> targets;
	protected final Collection<String> children = new TreeSet<>();

	public DeleteAction(@PluralsRes int targetNameRes, @PluralsRes int childNameRes, long... IDs) {
		this(targetNameRes, childNameRes, true, IDs);
	}
	public DeleteAction(@PluralsRes int targetNameRes, @PluralsRes int childNameRes, boolean affectsChildren,
			long... IDs) {
		this.IDs = IDs;

		this.affectsChildren = affectsChildren;
		this.targetNameRes = targetNameRes;
		this.childNameRes = childNameRes;
	}

	@Override protected void doPrepare() {
		if (IDs == null || IDs.length == 0) {
			throw new ValidationException(R.string.action_delete_error_empty);
		}
	}
	private String buildPlural(Resources res, @PluralsRes int titleRes) {
		String name = res.getQuantityString(targetNameRes, targets.size());
		return res.getQuantityString(titleRes, targets.size(), toPluralArgs(targets, name));
	}

	@Override public String getConfirmationTitle(Resources res) {
		return buildPlural(res, R.plurals.action_delete_title);
	}

	@Override public String getConfirmationMessage(Resources res) {
		StringBuilder sb = new StringBuilder();
		if (children.isEmpty()) {
			sb.append(buildPlural(res, R.plurals.action_delete_confirm_empty));
		} else {
			int confirm = affectsChildren? R.plurals.action_delete_confirm : R.plurals.action_delete_confirm_only;
			sb.append(buildPlural(res, confirm));
		}
		if (!children.isEmpty()) {
			sb.append("\n\n");
			int msg = affectsChildren? R.plurals.action_delete_details : R.plurals.action_delete_details_keep;
			sb.append(res.getQuantityString(msg, children.size(),
					children.size(),
					res.getQuantityString(childNameRes, children.size()),
					StringTools.join(children, ", ")
			));
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage(Resources res) {
		return buildPlural(res, R.plurals.action_delete_success);
	}

	@Override protected String getGenericFailureMessage(Resources res) {
		return buildPlural(res, R.plurals.action_delete_failed);
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// no undo
	}
}
