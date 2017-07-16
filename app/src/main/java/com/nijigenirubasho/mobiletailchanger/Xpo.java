package com.nijigenirubasho.mobiletailchanger;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xpo implements IXposedHookLoadPackage
{
	String tag="机型修改Xposed";
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p1) throws Throwable
	{
		String pn=p1.packageName;
		Log.d(tag, "PackageName:" + pn);
		XSharedPreferences xsp=new XSharedPreferences(this.getClass().getPackage().getName(), "xposed_blacklist");
		XSharedPreferences xsp2=new XSharedPreferences(this.getClass().getPackage().getName(), "config");
		String manufacturer=xsp2.getString("manufacturer", null);
		String model=xsp2.getString("model", null);
		String brand=xsp2.getString("brand", null);
		String product=xsp2.getString("product", null);
		String device=xsp2.getString("device", null);
		String blacklist=xsp.getString("main", null);
		if (!blacklist.contains(pn))
		{
			Log.d(tag, pn + ":return_OutOfBlackList");
			return;
		}
		Class<?> clz=XposedHelpers.findClass("android.os.Build", p1.classLoader);
		XposedHelpers.setStaticObjectField(clz, "MANUFACTURER", manufacturer);
		XposedHelpers.setStaticObjectField(clz, "BRAND", brand);
		XposedHelpers.setStaticObjectField(clz, "PRODUCT", product);
		XposedHelpers.setStaticObjectField(clz, "DEVICE", device);
		XposedHelpers.setStaticObjectField(clz, "MODEL", model);
	}

}
