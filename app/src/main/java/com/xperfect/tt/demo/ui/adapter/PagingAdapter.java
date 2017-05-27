package com.xperfect.tt.demo.ui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xperfect.tt.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle on 2017/5/12.
 */
public class PagingAdapter extends RecyclerView.Adapter<PagingAdapter.DefaultViewHolder> {

	ArrayList<AppInfo> items = new ArrayList<>();

	Context context;

	public PagingAdapter(Context context){
		this.context = context;
	}

	public ArrayList<AppInfo> getItems() {
		return items;
	}

	public void setItems(ArrayList<AppInfo> items) {
		this.items = items;
	}

	@Override
	public DefaultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		DefaultViewHolder holder = new DefaultViewHolder(LayoutInflater.from(
				context).inflate(R.layout.item, parent,
				false));
		return holder;
	}

	@Override
	public void onBindViewHolder(final DefaultViewHolder holder, final int position) {
//		if( position % 3 == 0 ){
//			holder.llItem.setBackgroundColor(context.getResources().getColor(R.color.colorOne));
//		} else if( position % 3 == 1 ) {
//			holder.llItem.setBackgroundColor(context.getResources().getColor(R.color.colorTwo));
//		} else if( position % 3 == 2 ) {
//			holder.llItem.setBackgroundColor(context.getResources().getColor(R.color.colorThree));
//		}
		holder.tvAppName.setText(items.get(position).getAppName());
		holder.ivAppIcon.setImageDrawable(items.get(position).appIcon);
		holder.ivAppIcon.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						OpenApp(holder.ivAppIcon.getContext(),items.get(position).packageName);
					}
				}
		);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	class DefaultViewHolder extends RecyclerView.ViewHolder {

		LinearLayout llItem;
		ImageView ivAppIcon;
		TextView tvAppName;

		public DefaultViewHolder(View view) {
			super(view);
			llItem = (LinearLayout) view.findViewById(R.id.ll_item);
			ivAppIcon = (ImageView) view.findViewById(R.id.iv_app_icon);
			tvAppName = (TextView) view.findViewById(R.id.tv_app_name);
		}

	}

	//打开APP
	public void OpenApp(final Context context, String packageName ){
		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageInfo == null) {
			Toast.makeText(context,"应用不存在",Toast.LENGTH_SHORT).show();
			return;
		}
		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageInfo.packageName);
		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packageName = 参数packageName
			String pkName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName.mainActivityName]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			// 设置ComponentName参数1:packageName:MainActivity路径
			ComponentName cn = new ComponentName(pkName, className);
			intent.setComponent(cn);
			context.startActivity(intent);
		}
	}

	public static class AppInfo {

		private Drawable appIcon;
		private String appName;
		private String packageName;

		public Drawable getAppIcon() {
			return appIcon;
		}

		public AppInfo setAppIcon(Drawable appIcon) {
			this.appIcon = appIcon;
			return this;
		}

		public String getAppName() {
			return appName;
		}

		public AppInfo setAppName(String appName) {
			this.appName = appName;
			return this;
		}

		public String getPackageName() {
			return packageName;
		}

		public AppInfo setPackageName(String packageName) {
			this.packageName = packageName;
			return this;
		}
	}

}