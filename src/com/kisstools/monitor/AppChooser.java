package com.kisstools.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppChooser extends Activity {

	public static final String TAG = "AppChooser";

	public static final String PACKAGE_NAME = "pacakge_name";

	private class AppInfo {
		protected String appPackage;
		protected String appName;
		protected Drawable appIcon;
	}

	private List<AppInfo> applicationList;

	private ListView lvApplications;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		applicationList = new ArrayList<AppInfo>();

		setContentView(R.layout.acitivity_chooser);
		getApplicationList();

		lvApplications = (ListView) findViewById(R.id.lv_applications);
		lvApplications.setOnItemClickListener(listener);
		lvApplications.setAdapter(adapter);
	}

	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			AppInfo appInfo = applicationList.get(position);
			String packageName = appInfo.appPackage;
			Intent data = new Intent();
			data.putExtra(PACKAGE_NAME, packageName);
			setResult(RESULT_OK, data);
			finish();
		}
	};

	private void getApplicationList() {
		PackageManager pm = getPackageManager();

		List<ApplicationInfo> appInfoList = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo ai : appInfoList) {
			if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				// ignore system applications
				continue;
			}

			AppInfo appInfo = new AppInfo();
			appInfo.appName = ai.loadLabel(pm).toString();
			appInfo.appPackage = ai.packageName;
			appInfo.appIcon = ai.loadIcon(pm);
			applicationList.add(appInfo);
		}
	}

	private BaseAdapter adapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.application_item, parent, false);
			}

			AppInfo info = applicationList.get(position);
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvName.setText(info.appName);

			TextView tvPackage = (TextView) convertView
					.findViewById(R.id.tv_package);
			tvPackage.setText(info.appPackage);

			ImageView ivIcon = (ImageView) convertView
					.findViewById(R.id.iv_icon);
			ivIcon.setImageDrawable(info.appIcon);

			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			if (applicationList == null || applicationList.isEmpty()) {
				return 0;
			} else {
				return applicationList.size();
			}
		}
	};

}
