package net.twisterrob.inventory.android.backup.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import net.twisterrob.inventory.android.backup.R;

public class BackupActionView extends FrameLayout {
	public enum Direction {
		NORMAL,
		REVERSED,
	}
	public BackupActionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		inflate(context, R.layout.backup_action, this);

		final ImageView imageView = findViewById(R.id.stuff_image);
		final TextView textView = findViewById(R.id.stuff_text);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BackupActionView);
		TypedArrayKt.use(array, new Function1<TypedArray, Unit>() {
			@Override public Unit invoke(TypedArray attributes) {
				Drawable image = attributes.getDrawable(R.styleable.BackupActionView_image);
				String text = attributes.getString(R.styleable.BackupActionView_text);
				int dir = attributes.getInt(R.styleable.BackupActionView_dir, 0);
				Direction direction = Direction.values()[dir];

				imageView.setImageDrawable(image);
				textView.setText(text);
				if (direction == Direction.REVERSED) {
					int layoutDir = getLayoutDirection() == LAYOUT_DIRECTION_LTR
							? LAYOUT_DIRECTION_RTL
							: LAYOUT_DIRECTION_LTR;
					setLayoutDirection(layoutDir);
				}
				return Unit.INSTANCE;
			}
		});
	}
}
