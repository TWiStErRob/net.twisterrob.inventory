package net.twisterrob.inventory.android.tasks;

import java.util.Collection;

import android.content.res.Resources;
import android.support.annotation.PluralsRes;

import net.twisterrob.inventory.android.view.Action;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.inventory.android.utils.Plurals.*;

public abstract class BaseAction implements Action {
	protected static String quant(Resources res, @PluralsRes int titleRes, Collection<String> toBeDeleted) {
		return res.getQuantityString(titleRes, toBeDeleted.size(), toPluralArgs(toBeDeleted));
	}

	protected static String buildConfirmString(Resources res, Collection<String> targets, Collection<String> children,
			@PluralsRes int delete_confirm,
			@PluralsRes int delete_confirm_empty, @PluralsRes int delete_details) {
		StringBuilder sb = new StringBuilder();
		if (children.isEmpty()) {
			sb.append(quant(res, delete_confirm_empty, targets));
		} else {
			sb.append(quant(res, delete_confirm, targets));
		}
		if (!children.isEmpty()) {
			sb.append("\n\n");
			sb.append(res.getQuantityString(delete_details,
					children.size(), children.size(), StringTools.join(children, ", ")));
		}
		return sb.toString();
	}
}
