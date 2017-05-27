package com.xperfect.tt.demo.widget.recyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Kyle on 2017/5/18.
 */
public class PagerHelper {

	public static final String TAG = PagerHelper.class.getSimpleName();

	public static final int ORIENTATION_HORIZONTAL = OrientationHelper.HORIZONTAL;
	public static final int ORIENTATION_VERTICAL = OrientationHelper.VERTICAL;
	public static final int ORIENTATION_NULL = -1;
	private static final float SCROLL_FACTOR = 1 / 2f;

	@IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL, ORIENTATION_NULL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Orientation {}

	protected @Orientation int mOrientation = ORIENTATION_HORIZONTAL;

	private int mOffsetY = 0;
	private int mOffsetX = 0;
	int mStartY = 0;
	int mStartX = 0;

	RecyclerView mRecyclerView;
	protected DefaultScrollListener mOnScrollListener;
	protected DefaultOnFlingListener mOnFlingListener;
	protected InnerOnTouchListener mOnTouchListener;
	protected OnPageChangedListener mOnPageChangedListener;

	ValueAnimator mAnimator = null;

	public PagerHelper(){
		init();
	}

	public void init(){
		mOnScrollListener = new DefaultScrollListener();
		mOnFlingListener = new DefaultOnFlingListener();
		mOnTouchListener = new InnerOnTouchListener();
	}

	public void attachToRecyclerView(@NonNull RecyclerView recycleView) {
		if (recycleView == null) {
			throw new IllegalArgumentException("recycleView must be not null");
		}
		mRecyclerView = recycleView;
		recycleView.setOnFlingListener(mOnFlingListener);
		recycleView.addOnScrollListener(mOnScrollListener);
		recycleView.setOnTouchListener(mOnTouchListener);
		resetLayoutManger();
	}

	public void resetLayoutManger() {
		RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		if (layoutManager != null) {
			if (layoutManager.canScrollVertically()) {
				mOrientation = ORIENTATION_VERTICAL;
			} else if (layoutManager.canScrollHorizontally()) {
				mOrientation = ORIENTATION_HORIZONTAL;
			} else {
				mOrientation = ORIENTATION_NULL;
			}
			if (mAnimator != null) {
				mAnimator.cancel();
			}
			mStartX = 0;
			mStartY = 0;
			mOffsetX = 0;
			mOffsetY = 0;
		}
	}

	public class DefaultOnFlingListener extends RecyclerView.OnFlingListener {
		@Override
		public boolean onFling(int velocityX, int velocityY) {
			if (mOrientation == ORIENTATION_NULL) {
				return false;
			}
			int pageIndex = getStartPageIndex();
			int endPoint = 0;
			int startPoint = 0;
			//如果是垂直方向
			if (mOrientation == ORIENTATION_VERTICAL) {
				startPoint = mOffsetY;
				if (velocityY < 0) {
					pageIndex--;
				} else if (velocityY > 0) {
					pageIndex++;
				}
				endPoint = pageIndex * mRecyclerView.getHeight();
			} else {
				startPoint = mOffsetX;
				if (velocityX < 0) {
					pageIndex--;
				} else if (velocityX > 0) {
					pageIndex++;
				}
				endPoint = pageIndex * mRecyclerView.getWidth();
			}
			if (endPoint < 0) {
				endPoint = 0;
			}
			if (mAnimator == null) {
				mAnimator = new ValueAnimator().ofInt(startPoint, endPoint);
				mAnimator.setDuration(300);
				mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int nowPoint = (int) animation.getAnimatedValue();
						if (mOrientation == ORIENTATION_VERTICAL) {
							int dy = nowPoint - mOffsetY;
							mRecyclerView.scrollBy(0, dy);
						} else {
							int dx = nowPoint - mOffsetX;
							mRecyclerView.scrollBy(dx, 0);
						}
					}
				});
				mAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if (null != mOnPageChangedListener) {
							mOnPageChangedListener.onPageChanged(getPageIndex());
						}
					}
				});
			} else {
				mAnimator.cancel();
				mAnimator.setIntValues(startPoint, endPoint);
			}
			mAnimator.start();
			return true;
		}
	}

	public class DefaultScrollListener extends RecyclerView.OnScrollListener {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			switch(newState){
				case RecyclerView.SCROLL_STATE_IDLE:
					if(mOrientation != ORIENTATION_NULL){
						boolean move;
						int vX = 0, vY = 0;
						if (mOrientation == ORIENTATION_VERTICAL) {
							int absY = Math.abs(mOffsetY - mStartY);
							move = absY > recyclerView.getHeight()*SCROLL_FACTOR;
							vY = 0;
							if (move) {
								vY = mOffsetY - mStartY < 0 ? -1000 : 1000;
							}
						} else {
							int absX = Math.abs(mOffsetX - mStartX);
							move = absX > recyclerView.getWidth() / 2;
							if (move) {
								vX = mOffsetX - mStartX < 0 ? -1000 : 1000;
							}
						}
						mOnFlingListener.onFling(vX, vY);
					}
					break;
				case RecyclerView.SCROLL_STATE_DRAGGING:
					break;
				case RecyclerView.SCROLL_STATE_SETTLING:
					break;
			}
		}
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			mOffsetY += dy;
			mOffsetX += dx;
		}
	}

	public class InnerOnTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					mStartY = mOffsetY;
					mStartX = mOffsetX;
					break;
			}
			return false;
		}
	}

	private int getPageIndex() {
		int pageIndex = 0;
		if (mOrientation == ORIENTATION_VERTICAL) {
			pageIndex = mOffsetY / mRecyclerView.getHeight();
		} else {
			pageIndex = mOffsetX / mRecyclerView.getWidth();
		}
		return pageIndex;
	}

	private int getStartPageIndex() {
		int pageIndex = 0;
		if (mOrientation == ORIENTATION_VERTICAL) {
			pageIndex = mStartY / mRecyclerView.getHeight();
		} else {
			pageIndex = mStartX / mRecyclerView.getWidth();
		}
		return pageIndex;
	}

	public void setOnPageChangedListener(@NonNull OnPageChangedListener listener) {
		mOnPageChangedListener = listener;
	}

	public interface OnPageChangedListener {
		void onPageChanged(int currentPageIndex);
	}

}