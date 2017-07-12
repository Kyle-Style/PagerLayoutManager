package com.xperfect.tt.demo.ui.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.xperfect.tt.demo.R;
import com.xperfect.tt.demo.ui.adapter.PagingAdapter;
import com.xperfect.tt.demo.utils.ScreenUtils;
import com.xperfect.tt.demo.widget.indicator.PagerIndicatorView;
import com.xperfect.tt.demo.widget.recyclerView.PagerHelper;
import com.xperfect.tt.demo.widget.recyclerView.PagerLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
		implements View.OnClickListener {

	@BindView(R.id.btn_pager)
	Button btnPager;
	@BindView(R.id.btn_horizontal)
	Button btnHorizontal;
	@BindView(R.id.btn_vertical)
	Button btnVertical;
	@BindView(R.id.rv_test1)
	RecyclerView rvTest1;
	@BindView(R.id.piv_test1)
	PagerIndicatorView pivTest1;
	@BindView(R.id.rv_test2)
	RecyclerView rvTest2;
	@BindView(R.id.piv_test2)
	PagerIndicatorView pivTest2;

	PagingAdapter pagingAdapter1;
	PagingAdapter pagingAdapter2;
	PagerLayoutManager pagerLayoutManager1;
	PagerLayoutManager pagerLayoutManager2;
	PagerHelper pagerHelper1;
	PagerHelper pagerHelper2;

	private ArrayList<String> dataList = new ArrayList<>();
	private ArrayList<PagingAdapter.AppInfo> appList = new ArrayList<>();

	private int length = 20;

	public static final int COUNT_ROW = 3;
	public static final int COUNT_COLUMN = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		initView();
	}

	public void onResume() {
		super.onResume();
		initData();
	}

	public void initData() {
		initAppInfo();
		for (int i = 0; i < length; i++) {
			dataList.add(i + "");
		}
		pagingAdapter1.setItems(appList);
		pagingAdapter2.setItems(appList.subList(0, 15));
		////
		int indicatorCount1 = appList.size() / (COUNT_ROW * COUNT_COLUMN) + (appList.size() % (COUNT_ROW * COUNT_COLUMN) == 0 ? 0 : 1);
		pivTest1.initIndicator(indicatorCount1);
		int indicatorCount2 = appList.subList(0, 15).size() / (COUNT_ROW * COUNT_COLUMN) + (appList.subList(0, 15).size() % (COUNT_ROW * COUNT_COLUMN) == 0 ? 0 : 1);
		pivTest2.initIndicator(indicatorCount2);
	}

	public void initAppInfo() {
		appList.clear();
		PackageManager pm = getPackageManager();
		List<PackageInfo> mPacks = pm.getInstalledPackages(0);
		PagingAdapter.AppInfo mInfo;
		for (PackageInfo info : mPacks) {
			if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				//第三方应用
				mInfo = new PagingAdapter.AppInfo();
				mInfo.setAppIcon(info.applicationInfo.loadIcon(pm))
						.setAppName(info.applicationInfo.loadLabel(pm).toString())
						.setPackageName(info.packageName);
				appList.add(mInfo);
			} else {
				//系统应用
			}
		}
	}

	public void initView() {
		/////
		pagingAdapter1 = new PagingAdapter(this);
		pagerHelper1 = new PagerHelper();
		PagerLayoutManager.PagingBuilder builder1 =
				new PagerLayoutManager.PagingBuilder()
						.setColumnCount(COUNT_COLUMN)
						.setRowCount(COUNT_ROW)
						.setParentHeight(dip2px(300))
						.setParentWidth(ScreenUtils.getScreenWidth(this))
						.setPadding(10)
						.setOrientation(PagerLayoutManager.HORIZONTAL)
						.setItemGravity(PagerLayoutManager.GRAVITY_CENTER);
		pagerLayoutManager1 = new PagerLayoutManager(builder1);
		rvTest1.setAdapter(pagingAdapter1);
		rvTest1.setLayoutManager(pagerLayoutManager1);
		pagerHelper1.attachToRecyclerView(rvTest1);
		pagerHelper1.setOnPageChangedListener((currentPageIndex) -> pivTest1.setSelectedPage(currentPageIndex));
		////
		pagingAdapter2 = new PagingAdapter(this);
		pagerHelper2 = new PagerHelper();
		PagerLayoutManager.PagingBuilder builder2 =
				new PagerLayoutManager.PagingBuilder()
						.setColumnCount(COUNT_COLUMN)
						.setRowCount(COUNT_ROW)
						.setParentHeight(dip2px(300))
						.setParentWidth(ScreenUtils.getScreenWidth(this))
						.setPadding(10)
						.setOrientation(PagerLayoutManager.HORIZONTAL)
						.setItemGravity(PagerLayoutManager.GRAVITY_CENTER);
		pagerLayoutManager2 = new PagerLayoutManager(builder2);
		rvTest2.setAdapter(pagingAdapter2);
		rvTest2.setLayoutManager(pagerLayoutManager2);
		pagerHelper2.attachToRecyclerView(rvTest2);
		pagerHelper2.setOnPageChangedListener((currentPageIndex) -> pivTest2.setSelectedPage(currentPageIndex));
		/////
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_pager:
				break;
			case R.id.btn_horizontal:
				break;
			case R.id.btn_vertical:
				break;
		}
	}

	public int dip2px(float dpValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

}