/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

    public LoadingLayout mHeaderLoadingView;
    public FrameLayout mHeaderSearchLayout;
    private LoadingLayout mFooterLoadingView;
    private FrameLayout mLvFooterLoadingFrame;
    private boolean mListViewExtrasEnabled;
    private boolean dragMode;


    public PullToRefreshListView(Context context) {
        super(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshListView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshListView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    public void onRefreshing(final boolean doScroll) {
        final LoadingLayout origLoadingView, listViewLoadingView, oppositeListViewLoadingView;
        final int selection, scrollToY;

        if (listviewState != SCROLL_STATE_FLING) {
            ListAdapter adapter = mRefreshableView.getAdapter();
            if (!mListViewExtrasEnabled || !getShowViewWhileRefreshing() || null == adapter
                    || adapter.isEmpty()) {
                super.onRefreshing(doScroll);
                return;
            }

            super.onRefreshing(false);

            switch (getCurrentMode()) {
                case MANUAL_REFRESH_ONLY:
                case PULL_FROM_END:
                    origLoadingView = getFooterLayout();
                    listViewLoadingView = mFooterLoadingView;
                    oppositeListViewLoadingView = mHeaderLoadingView;
                    selection = mRefreshableView.getCount() - 1;
                    scrollToY = getScrollY() - getFooterSize();
                    break;
                case PULL_FROM_START:
                default:
                    origLoadingView = getHeaderLayout();
                    listViewLoadingView = mHeaderLoadingView;
                    oppositeListViewLoadingView = mFooterLoadingView;
                    selection = 0;
                    scrollToY = getScrollY() + getHeaderSize();
                    break;
            }
            origLoadingView.reset();
            origLoadingView.hideAllViews();
            oppositeListViewLoadingView.setVisibility(View.GONE);
            if (getCurrentMode() == Mode.PULL_FROM_START) {
                mHeaderLoadingView.setVisibility(View.VISIBLE);
            }


            listViewLoadingView.refreshing();
            if (doScroll) {
                disableLoadingLayoutVisibilityChanges();
                setHeaderScroll(scrollToY);
                mRefreshableView.setSelection(selection);
                smoothScrollTo(0);
            }
        }
    }

    @Override
    protected void onReset() {
        if (!mListViewExtrasEnabled) {
            super.onReset();
            return;
        }

        final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
        final int scrollToHeight, selection;
        final boolean scrollLvToEdge;

        switch (getCurrentMode()) {
            case MANUAL_REFRESH_ONLY:
            case PULL_FROM_END:
                originalLoadingLayout = getFooterLayout();
                listViewLoadingLayout = mFooterLoadingView;
                selection = mRefreshableView.getCount() - 1;
                scrollToHeight = getFooterSize();
                scrollLvToEdge = Math.abs(mRefreshableView.getLastVisiblePosition() - selection) <= 1;
                if (listViewLoadingLayout.getVisibility() == View.VISIBLE) {
                    originalLoadingLayout.showInvisibleViews();
                    listViewLoadingLayout.setVisibility(View.GONE);
                    if (scrollLvToEdge && getState() != State.MANUAL_REFRESHING) {
                        mRefreshableView.setSelection(selection);
                        setHeaderScroll(scrollToHeight);
                    }
                }
                break;
            case PULL_FROM_START:
            default:
                originalLoadingLayout = getHeaderLayout();
                listViewLoadingLayout = mHeaderLoadingView;
                scrollToHeight = -getHeaderSize();
                selection = 0;
                scrollLvToEdge = Math.abs(mRefreshableView.getFirstVisiblePosition() - selection) <= 1;
                if (mHeaderSearchLayout != null) {
                    mHeaderSearchLayout.setVisibility(VISIBLE);
                }
//                    originalLoadingLayout.showInvisibleViews();
                listViewLoadingLayout.hideAllViews();//  setVisibility(View.GONE);
                if (scrollLvToEdge && getState() != State.MANUAL_REFRESHING) {
                    mRefreshableView.setSelection(selection);
                    setHeaderScroll(scrollToHeight);
                }
                break;
        }

        // Finally, call up to super
        super.onReset();
    }

    @Override
    protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart,
                                                          final boolean includeEnd) {
        LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(includeStart, includeEnd);

        if (mListViewExtrasEnabled) {
            final Mode mode = getMode();

            if (includeStart && mode.showHeaderLoadingLayout()) {
                proxy.addLayout(mHeaderLoadingView);
            }
            if (includeEnd && mode.showFooterLoadingLayout()) {
                proxy.addLayout(mFooterLoadingView);
            }
        }

        return proxy;
    }

    protected ListView createListView(Context context, AttributeSet attrs) {
        final ListView lv;
        lv = new InternalListView(context, attrs);
        return lv;
    }

    @Override
    protected ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView lv = createListView(context, attrs);
        lv.setId(android.R.id.list);
        return lv;
    }

    @Override
    protected void handleStyledAttributes(TypedArray a) {
        super.handleStyledAttributes(a);

        mListViewExtrasEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrListViewExtrasEnabled,
                true);

        if (mListViewExtrasEnabled) {
            final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL);

            // Create Loading Views ready for use later
            FrameLayout frame = new FrameLayout(getContext());
            mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, a);
            if (mHeaderSearchLayout != null) {
                mHeaderSearchLayout.setVisibility(VISIBLE);
                mHeaderLoadingView.setVisibility(View.VISIBLE);
            } else {
                mHeaderLoadingView.setVisibility(View.GONE);
            }
            frame.addView(mHeaderLoadingView, lp);
            mRefreshableView.addHeaderView(frame, null, false);

            mLvFooterLoadingFrame = new FrameLayout(getContext());
            mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, a);
            mFooterLoadingView.setVisibility(View.GONE);
            mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);
            if (!a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
                setScrollingWhileRefreshingEnabled(true);
            }
        }
    }

    protected class InternalListView extends ListView implements EmptyViewMethodAccessor {

        private boolean mAddedLvFooter = false;

        public InternalListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            try {
                if (isDragMode()) {
                    int childcount = getChildCount();
                    for (int i = 0; i < childcount; i++) {
                        View v = getChildAt(i);
                        v.dispatchTouchEvent(ev);
                    }
                    return true;
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                return false;
            }
        }


        @Override
        public void setAdapter(ListAdapter adapter) {
            if (null != mLvFooterLoadingFrame && !mAddedLvFooter) {
                addFooterView(mLvFooterLoadingFrame, null, false);
                mAddedLvFooter = true;
            }

            super.setAdapter(adapter);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (isDragMode()) {
                int childcount = getChildCount();
                for (int i = 0; i < childcount; i++) {
                    View v = getChildAt(i);
                    v.dispatchTouchEvent(ev);
                }
                return true;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        } catch (Exception e) {
            return true;
        }
    }

    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
    }

    public boolean isDragMode() {
        return dragMode;
    }

    public boolean setHeaderView(FrameLayout ll) {
        mHeaderSearchLayout = ll;
        mHeaderLoadingView.addView(mHeaderSearchLayout);
        mHeaderSearchLayout.setVisibility(VISIBLE);
        mHeaderLoadingView.setVisibility(View.VISIBLE);

        return true;
    }
}
