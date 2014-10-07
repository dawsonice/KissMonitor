package com.kisstools.monitor.data;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DisplayInfo {

	public static final String TAG = "DisplayInfo";

	private int width;
	private int height;
	private float density;
	private double inch;

	public DisplayInfo(Context context) {
		WindowManager winMgr = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = winMgr.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		this.width = metrics.widthPixels;
		this.height = metrics.heightPixels;

		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
			try {
				this.width = (Integer) Display.class.getMethod("getRawWidth")
						.invoke(display);
				this.height = (Integer) Display.class.getMethod("getRawHeight")
						.invoke(display);
			} catch (Exception e) {
			}
		}

		if (Build.VERSION.SDK_INT >= 17) {
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize", Point.class).invoke(
						display, realSize);
				this.width = realSize.x;
				this.height = realSize.y;
			} catch (Exception ignored) {
			}
		}

		this.density = metrics.density;
		this.inch = Math.sqrt(Math.pow(width / metrics.xdpi, 2)
				+ Math.pow(height / metrics.ydpi, 2));
		DecimalFormat df = new DecimalFormat("#.0");
		this.inch = Double.valueOf(df.format(this.inch));
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public float getDensity() {
		return this.density;
	}

	public double getInch() {
		return this.inch;
	}
}
