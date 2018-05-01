package com.example.pblsystem.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 郭聪聪 on 2017/3/26.
 */

public class MyDecoration extends RecyclerView.ItemDecoration{
    private Context context;
    private Drawable divider;
    public static final int[] ATRRS  = new int[]{
            android.R.attr.listDivider
    };

    public MyDecoration(Context context) {
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(ATRRS);
        divider = ta.getDrawable(0);
        ta.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childsCount = parent.getChildCount();
        for (int i = 0; i < childsCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
    }
}
