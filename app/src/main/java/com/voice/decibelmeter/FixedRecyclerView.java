/**
 * FixedRecyclerView
 * 다음의 링크에서 가져옴
 * https://github.com/lurbas/Android-MVC/blob/master/app/src/main/java/com/lucasurbas/mvc/view/FixedRecyclerView.java
 */

package com.voice.decibelmeter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

/**
 * RecyclerView 클래스
 * @author https://github.com/plinioseniore/soulissapp-plinio/blob/master/SoulissApp/src/main/java/it/angelic/soulissclient/helpers/ContextMenuRecyclerView.java
 */
public class FixedRecyclerView extends RecyclerView {

    private RecyclerContextMenuInfo mContextMenuInfo;

    public FixedRecyclerView(Context context) {
        super(context);
    }

    public FixedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if(direction < 1) {
            boolean original = super.canScrollVertically(direction);
            return !original && getChildAt(0) != null && getChildAt(0).getTop() < 0 || original;
        }
        return super.canScrollVertically(direction);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildAdapterPosition(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public RecyclerContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }

}
