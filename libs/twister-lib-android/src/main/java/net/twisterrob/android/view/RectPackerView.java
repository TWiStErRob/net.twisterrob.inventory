package net.twisterrob.android.view.color.swatches;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import net.twisterrob.android.view.color.ColorMath;

/**
 * <code><pre>
 * <net.twisterrob.android.view.color.swatches.RectPackerView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/color"
 *     android:background="#ff888888"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:layout_margin="@dimen/activity_margin"
 *     />
 * </pre></code>
 */
public class RectPackerView extends View {
    public RectPackerView(Context context) {
        super(context);
        init();
    }

    public RectPackerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectPackerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    RectPacker.Node<Integer> root;

    // https://math.stackexchange.com/questions/466198/algorithm-to-get-the-maximum-size-of-n-squares-that-fit-into-a-rectangle-with-a/
    private void init() {
        RectPacker<Integer> packer = new RectPacker<Integer>(new Rect(0, 0, 1000, 1200));
        this.root = packer.root;
        int n = 6;
        float x = root.rect.width();
        float y = root.rect.height();
        float px = (float) Math.ceil(x / Math.sqrt(x * y / (float) n));
        float py = (float) Math.ceil(y / Math.sqrt(x * y / (float) n));
        int size = (int) Math.max(x / px, x / py);
        for (int i = 0; i < n; ++i)
            packer.insert(new Rect(0, 0, size, size), ColorMath.randomColor());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        draw(canvas, root);
    }

    Paint paint = new Paint();

    {
        paint.setStyle(Paint.Style.STROKE);
    }

    private void draw(Canvas canvas, RectPacker.Node<Integer> root) {
        if (root.data != null) {
            paint.setColor(root.data);
            paint.setStrokeWidth(10);
            paint.setAlpha(100);
        } else {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(1);
            paint.setAlpha(255);
        }
        canvas.drawRect(root.rect, paint);
        if (root.child1 != null) {
            draw(canvas, root.child1);
        }
        if (root.child2 != null) {
            draw(canvas, root.child2);
        }
    }
}
