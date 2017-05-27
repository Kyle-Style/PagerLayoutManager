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
import com.xperfect.tt.demo.widget.indicator.PagerIndicatorView;
import com.xperfect.tt.demo.widget.recyclerView.PagerHelper;
import com.xperfect.tt.demo.widget.recyclerView.PagerLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
		implements View.OnClickListener, PagerHelper.OnPageChangedListener {

	Button btnPager;
	Button btnHorizontal;
	Button btnVertical;
	RecyclerView rcvContent;
	PagerIndicatorView pivPager;

	PagingAdapter pagingAdapter;
	PagerLayoutManager pagerLayoutManager;
	PagerHelper	pagerHelper;

	PagingAdapter pagingAdapter2;
	PagerLayoutManager pagerLayoutManager2;
	PagerHelper	pagerHelper2;

	private ArrayList<String> dataList = new ArrayList<>();
	private ArrayList<PagingAdapter.AppInfo> appList = new ArrayList<>();

	private int length = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	public void onResume(){
		super.onResume();
		initData();
	}

	public void initData(){
		initAppInfo();
		for (int i = 0; i < length; i++) {
			dataList.add(i+"");
		}
		pagingAdapter.setItems(appList);
		pagingAdapter2.setItems(appList);
		////
		PagerLayoutManager.PagingBuilder builder2 =
				new PagerLayoutManager.PagingBuilder()
						.setColumnCount(4)
						.setRowCount(2)
						.setParentHeight(600)
						.setParentWidth(1080)
						.setPadding(10)
						.setOrientation(PagerLayoutManager.HORIZONTAL)
						.setItemGravity(PagerLayoutManager.GRAVITY_CENTER);
		pagerLayoutManager = new PagerLayoutManager(builder2);
		////
		pagerHelper = new PagerHelper();
	}

	public void initAppInfo(){
		appList.clear();
		PackageManager pm = getPackageManager();
		List<PackageInfo> mPacks = pm.getInstalledPackages(0);
		PagingAdapter.AppInfo mInfo;
		for(PackageInfo info : mPacks){
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

	public void initView(){
		btnPager = (Button)findViewById(R.id.btn_pager);
		btnHorizontal = (Button)findViewById(R.id.btn_horizontal);
		btnVertical = (Button)findViewById(R.id.btn_vertical);
		rcvContent = (RecyclerView)findViewById(R.id.rcv_content);
		pivPager = (PagerIndicatorView)findViewById(R.id.piv_pager);
		/////
		pagingAdapter = new PagingAdapter(this);
		pagingAdapter2 = new PagingAdapter(this);
		/////
		rcvContent.setAdapter(pagingAdapter);
		rcvContent.setLayoutManager(pagerLayoutManager);
		pagerHelper.attachToRecyclerView(rcvContent);
		pagerHelper.setOnPageChangedListener(this);
		////
		pivPager.initIndicator(5);
		pivPager.setSelectedPage(0);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.btn_pager:
				break;
			case R.id.btn_horizontal:
				break;
			case R.id.btn_vertical:
				break;
		}
	}

	@Override
	public void onPageChanged(int currentPageIndex) {
		pivPager.setSelectedPage(currentPageIndex);
	}

}