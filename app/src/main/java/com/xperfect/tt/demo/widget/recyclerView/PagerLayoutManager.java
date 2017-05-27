package com.xperfect.tt.demo.widget.recyclerView;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Kyle on 2017/5/10.
 */
public class PagerLayoutManager extends RecyclerView.LayoutManager implements
		ItemTouchHelper.ViewDropHandler, RecyclerView.SmoothScroller.ScrollVectorProvider {

	public static final String TAG = PagerLayoutManager.class.getSimpleName();

	/////
	public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
	public static final int VERTICAL = OrientationHelper.VERTICAL;
	/////
	public static final int GRAVITY_CENTER = 0x00001;
	public static final int GRAVITY_LEFT = 0x00010;
	public static final int GRAVITY_TOP = 0x00100;
	public static final int GRAVITY_RIGHT = 0x01000;
	public static final int GRAVITY_BOTTOM = 0x10000;
	/////
	public static final int SCROLL_STATE_IDLE = RecyclerView.SCROLL_STATE_IDLE;
	public static final int SCROLL_STATE_DRAGGING = RecyclerView.SCROLL_STATE_DRAGGING;
	public static final int SCROLL_STATE_SETTLING = RecyclerView.SCROLL_STATE_SETTLING;
	private static final float MAX_SCROLL_FACTOR = 1 / 3f;

	@IntDef({HORIZONTAL, VERTICAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Orientation {}

	@IntDef({SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING})
	public @interface ScrollState {}

	public static final double DEFAULT_SPEED_RATIO = 0.5;

	private PagingBuilder mPagingBuilder;
	private Property mProperty;

	public PagerLayoutManager(){
		this.mProperty = new Property();
	}

	public PagerLayoutManager(PagingBuilder builder){
		this();
		this.mPagingBuilder = builder;
		this.mProperty.mPageNum = builder.mRowCount*builder.mColumnCount;
	}

	public PagingBuilder getPagingBuilder() {
		return mPagingBuilder;
	}

	public Property getProperty() {
		return mProperty;
	}

	@Override
	public RecyclerView.LayoutParams generateDefaultLayoutParams() {
		return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	public boolean canScrollHorizontally() {
		return true;
	}

	@Override
	public PointF computeScrollVectorForPosition(int targetPosition) {
		return null;
	}

	@Override
	public void prepareForDrop(View view, View target, int x, int y) {

	}

	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
		if( mPagingBuilder.mParentWidth<=0 || mPagingBuilder.mParentHeight<=0 ){
			return;
		}
		detachAndScrapAttachedViews(recycler);
		//item width height
		mProperty.mItemWidth = (mPagingBuilder.mParentWidth
				-mPagingBuilder.mPaddingLeft
				-mPagingBuilder.mPaddingRight)/mPagingBuilder.mColumnCount;
		mProperty.mItemHeight = (mPagingBuilder.mParentHeight
				-mPagingBuilder.mPaddingTop
				-mPagingBuilder.mPaddingBottom)/mPagingBuilder.mRowCount;
		///
		int itemCount = getItemCount();
		for(int position=0; position<itemCount; position++){
			mProperty.mTempPosition = position%mProperty.mPageNum;
			mProperty.mCurrentPage = position/mProperty.mPageNum;
			if( (mProperty.mTempPosition)%mPagingBuilder.mColumnCount==0 ){
				mProperty.mTempColumn=0;
			} else {
				mProperty.mTempColumn++;
			}
			//view
			View scrap = recycler.getViewForPosition(position);
			addView(scrap);
			measureChildWithMargins(scrap, 0, 0);
			//rect
			Rect itemRect = mProperty.mItemsFrameArray.get(position);
			if(itemRect == null){
				itemRect = new Rect();
			}
			switch(mPagingBuilder.mOrientation){
				case VERTICAL:
					mProperty.mOffsetX = mPagingBuilder.mPaddingLeft
							+mProperty.mItemWidth*mProperty.mTempColumn;
					mProperty.mOffsetY = mPagingBuilder.mPaddingTop
							+mPagingBuilder.mParentHeight*mProperty.mCurrentPage
							+mProperty.mItemHeight*(mProperty.mTempRow)+0;
					break;
				case HORIZONTAL:
					mProperty.mOffsetX = mPagingBuilder.mPaddingLeft
							+mPagingBuilder.mParentWidth*mProperty.mCurrentPage
							+mProperty.mItemWidth*mProperty.mTempColumn+0;
					mProperty.mOffsetY = mPagingBuilder.mPaddingTop
							+mProperty.mItemHeight*(mProperty.mTempRow);
					break;
			}
			itemRect.set(getRealRectByGravity(
					mProperty.mOffsetX,
					mProperty.mOffsetY,
					mProperty.mItemWidth,
					mProperty.mItemHeight,
					getDecoratedMeasuredWidth(scrap),
					getDecoratedMeasuredHeight(scrap),
					mPagingBuilder.mItemGravity
			));
			mProperty.mItemsFrameArray.put(position,itemRect);
			mProperty.mItemsAttachedArray.put(position,false);
			////
			if( mProperty.mTempColumn==mPagingBuilder.mColumnCount-1 ){
				mProperty.mTempRow++;
			}
			//tempRow置0
			if( position!=0&&mProperty.mTempPosition==(mProperty.mPageNum-1) ){
				mProperty.mTempRow = 0;
			}
			detachAndScrapView(scrap, recycler);
		}
		layoutItems(recycler,state);
	}

	@Override
	public void onLayoutCompleted(RecyclerView.State state) {
		super.onLayoutCompleted(state);
	}

	public Rect getRealRectByGravity(int scrollX, int scrollY, int itemCalculateX,
									 int itemCalculateY, int itemWidth, int itemHeight,
									 int gravity){
		itemWidth = itemWidth>itemCalculateX?itemCalculateX:itemWidth;
		itemHeight = itemHeight>itemCalculateY?itemCalculateY:itemHeight;
		Rect rect = new Rect();
		switch(gravity){
			case GRAVITY_LEFT:
			case GRAVITY_LEFT|GRAVITY_CENTER:
				rect.left = scrollX;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY + (itemCalculateY-itemHeight)/2;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_LEFT|GRAVITY_TOP:
				rect.left = scrollX;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_TOP:
			case GRAVITY_TOP|GRAVITY_CENTER:
				rect.left = scrollX + (itemCalculateX-itemWidth)/2;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_RIGHT|GRAVITY_TOP:
				rect.left = scrollX + itemCalculateX - itemWidth;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_RIGHT:
			case GRAVITY_RIGHT|GRAVITY_CENTER:
				rect.left = scrollX + itemCalculateX - itemWidth;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY + (itemCalculateY - itemHeight)/2;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_RIGHT|GRAVITY_BOTTOM:
				rect.left = scrollX + itemCalculateX - itemWidth;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY + itemCalculateY - itemHeight;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_BOTTOM:
			case GRAVITY_BOTTOM|GRAVITY_CENTER:
				rect.left = scrollX + (itemCalculateX-itemWidth)/2;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY + itemCalculateY - itemHeight;
				rect.bottom = rect.top+itemHeight;
				break;
			case GRAVITY_LEFT|GRAVITY_BOTTOM:
				rect.left = scrollX;
				rect.right = scrollX+itemWidth;
				rect.top = scrollX + itemCalculateY - itemHeight;
				rect.bottom = rect.top+itemHeight;
				break;
			default:
				rect.left = scrollX + (itemCalculateX-itemWidth)/2;
				rect.right = rect.left+itemWidth;
				rect.top = scrollY + (itemCalculateY - itemHeight)/2;
				rect.bottom = rect.top+itemHeight;
		}
		return rect;
	}

	/**
	 * lay out the item which need to show
	 * */
	private void layoutItems(RecyclerView.Recycler recycler, RecyclerView.State state){
		if(state.isPreLayout()) return;
		if( mPagingBuilder.mParentWidth<=0 || mPagingBuilder.mParentHeight<=0 ) {
			throw new IllegalArgumentException("ParentWidth and parentHeight must be more than 0." +
					"	Expected:[ParentWidth,ParentHeight]=["+mPagingBuilder.mParentWidth+"," +
					mPagingBuilder.mParentHeight+"]");
		}
		Rect displayedRect = mProperty.getDisplayedRect();
		//remove the views which out of range
		int childCount = getChildCount();
		Rect childRect = new Rect();
		for (int position = 0; position < childCount; position++) {
			View child = getChildAt(position);
			if( child != null ){
				childRect.set(
						getDecoratedLeft(child),
						getDecoratedTop(child),
						getDecoratedRight(child),
						getDecoratedBottom(child)
				);
				if (!Rect.intersects(displayedRect, childRect)) {
					mProperty.mItemsAttachedArray.put(getPosition(child), false);
					removeAndRecycleView(child, recycler);
				}
			}
		}
		//add the views which do not attached and in the range
		int itemCount = getItemCount();
		for (int position = 0; position < itemCount; position++) {
			if (Rect.intersects(displayedRect, mProperty.mItemsFrameArray.get(position))) {
				if (!mProperty.mItemsAttachedArray.get(position)) {
					View scrap = recycler.getViewForPosition(position);
					if( scrap == null ){
						continue;
					}
					measureChildWithMargins(scrap, 0, 0);
					addView(scrap);
					Rect frame = mProperty.mItemsFrameArray.get(position);
					layoutDecorated(scrap,
							frame.left - mProperty.mDisplayedRect.left,
							frame.top - mProperty.mDisplayedRect.top,
							frame.right - mProperty.mDisplayedRect.left,
							frame.bottom - mProperty.mDisplayedRect.top);
					mProperty.mItemsAttachedArray.put(position, true);
				}
			}
		}
	}

	@Override
	public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
									RecyclerView.State state) {
		if (getChildCount() == 0 || dx == 0) {
			return 0;
		}
		int willScroll = dx;
		int pageNum = state.getItemCount()/(mPagingBuilder.mRowCount*mPagingBuilder.mColumnCount);
		pageNum = state.getItemCount()%(mPagingBuilder.mRowCount*mPagingBuilder.mColumnCount)==0
				?pageNum:pageNum+1;
		if (mProperty.mScrollOffsetX + dx < 0) {
			willScroll = -mProperty.mScrollOffsetX;
		} else if (mProperty.mScrollOffsetX + dx >
				pageNum*mPagingBuilder.mParentWidth - mPagingBuilder.mParentWidth) {
			willScroll = pageNum*mPagingBuilder.mParentWidth
					- mPagingBuilder.mParentWidth - mProperty.mScrollOffsetX;
		}
		if (willScroll == 0) {
			return -0;
		}
		mProperty.mScrollOffsetX += willScroll;
		offsetChildrenHorizontal(-willScroll);
		layoutItems(recycler, state);
		return willScroll;
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
									   int position) {
		LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext());
		scroller.setTargetPosition(position);
		startSmoothScroll(scroller);
	}

	public class Property {
		////paging
		private int mPageNum = 0;
		private int mCurrentPage = 0;
		private int mPageSize = 0;
		private int mFirstVisiblePosition = 0;
		private int mLastVisiblePosition = 0;
		////offsets
		protected int mScrollOffsetX = 0;
		protected int mScrollOffsetY = 0;
		////
		protected int mOffsetX = 0;
		protected int mOffsetY = 0;
		////
		protected int mTempRow = 0;
		protected int mTempColumn = 0;
		protected int mTempPosition = 0;
		////
		protected int mItemWidth = 0;
		protected int mItemHeight = 0;
		////first view and last view
		protected View mFirstView;
		protected View mLastView;
		////scroll state
		protected @ScrollState int mScrollState = SCROLL_STATE_IDLE;

		////存放所有item的位置和尺寸
		SparseArray<Rect> mItemsFrameArray = new SparseArray<>();

		////记录item是否已经展示
		SparseBooleanArray mItemsAttachedArray = new SparseBooleanArray();

		protected Rect mDisplayedRect = new Rect();

		public Rect getDisplayedRect(){
			mDisplayedRect.set(mScrollOffsetX,
					mScrollOffsetY,
					mScrollOffsetX+mPagingBuilder.mParentWidth,
					mScrollOffsetY+mPagingBuilder.mParentHeight);
			return mDisplayedRect;
		}

		public void resetScrollXY(){
			mScrollOffsetX = 0;
			mScrollOffsetY = 0;
		}

		public void resetCurrentPage(int currentPage){
			switch (mPagingBuilder.mOrientation){
				case HORIZONTAL:
					mScrollOffsetX = currentPage*mPagingBuilder.mParentWidth;
					break;
				case VERTICAL:
					mScrollOffsetY = currentPage*mPagingBuilder.mParentHeight;
					break;
				default:
					mScrollOffsetX = currentPage*mPagingBuilder.mParentWidth;
			}
		}

		public int getCurrentPage(){
			return mScrollOffsetX/mPagingBuilder.mParentWidth;
		}

		public @ScrollState int getScrollState(){
			return mScrollState;
		}

		public void setScrollState(@ScrollState int scrollState){
			mScrollState = scrollState;
		}

		public int getPageSize() {
			int pageNum = getChildCount()/(mPagingBuilder.mRowCount*mPagingBuilder.mColumnCount);
			pageNum = getChildCount()%(mPagingBuilder.mRowCount*mPagingBuilder.mColumnCount)==0
					?pageNum:pageNum+1;
			return pageNum;
		}
	}

	public static class PagingBuilder {

		////layout property
		private int mRowCount = 0;
		private int mColumnCount = 0;
		protected int mParentWidth = 0;
		protected int mParentHeight = 0;
		////orientation
		public int mOrientation = HORIZONTAL;
		////item gravity
		protected int mItemGravity;
		////margins
		protected int mPadding = 0;
		protected int mPaddingLeft = 0;
		protected int mPaddingTop = 0;
		protected int mPaddingRight = 0;
		protected int mPaddingBottom = 0;
		////
		protected boolean mShouldReverseLayout;
		protected boolean mSmoothScrollbarEnabled;
		////
		protected double mSpeedRatio = DEFAULT_SPEED_RATIO;

		public PagingBuilder(){}

		public PagingBuilder setRowCount(int rowCount) {
			this.mRowCount = rowCount;
			return this;
		}

		public PagingBuilder setColumnCount(int columnCount) {
			this.mColumnCount = columnCount;
			return this;
		}

		public PagingBuilder setParentWidth(int parentWidth) {
			this.mParentWidth = parentWidth;
			return this;
		}

		public PagingBuilder setParentHeight(int parentHeight) {
			this.mParentHeight = parentHeight;
			return this;
		}

		public PagingBuilder setOrientation(@Orientation int orientation) {
			this.mOrientation = orientation;
			return this;
		}

		public PagingBuilder setItemGravity(int mItemGravity) {
			this.mItemGravity = mItemGravity;
			return this;
		}

		public PagingBuilder setPadding(int margin){
			mPadding = margin;
			mPaddingLeft = margin;
			mPaddingTop = margin;
			mPaddingRight = margin;
			mPaddingBottom = margin;
			return this;
		}

		public PagingBuilder setPadding(int paddingLeft,
										int paddingTop,int paddingRight,int paddingBottom){
			mPaddingLeft = paddingLeft;
			mPaddingTop = paddingTop;
			mPaddingRight = paddingRight;
			mPaddingBottom = paddingBottom;
			return this;
		}

		public PagingBuilder setPaddingLeft(int paddingLeft){
			mPaddingLeft = paddingLeft;
			return this;
		}

		public PagingBuilder setPaddingTop(int paddingTop){
			mPaddingTop = paddingTop;
			return this;
		}

		public PagingBuilder setPaddingRight(int paddingRight){
			mPaddingRight = paddingRight;
			return this;
		}

		public PagingBuilder setPaddingBottom(int paddingBottom){
			mPaddingBottom = paddingBottom;
			return this;
		}

		public PagingBuilder setSpeedRatio(double speedRatio) {
			mSpeedRatio = speedRatio;
			return this;
		}

	}

}