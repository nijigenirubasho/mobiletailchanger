package com.nijigenirubasho.mobiletailchanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.http.util.EncodingUtils;

public class MainActivity extends Activity 
{
	TextView apiLabel;
	String sBrand;
	String sModel;
	String sManufacturer;
	String sProduct;
	String sDevice;
	EditText eBrand;
	EditText eModel;
	EditText eManufacturer;
	EditText eProduct;
	EditText eDevice;
	String filePath;
	SharedPreferences sp;
	SharedPreferences.Editor spe;
	Button change;
	Button x_import;
	Button export;
	Button reset;
	Button help;
	Button magisk;
	Button mgzip;
	Button xposed;
	Button compatibilityCheck;
	CheckBox busybox;
	CheckBox anzhuo;
	String bsbx_head="busybox ";
	String fileinfo;
	String tag="机型更改";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sp = getSharedPreferences("config", MODE_WORLD_READABLE);
		spe = sp.edit();
		apiLabel = (TextView) findViewById(R.id.apilabel);
		eModel = (EditText) findViewById(R.id.model);
		eBrand = (EditText) findViewById(R.id.brand);
		eManufacturer = (EditText) findViewById(R.id.manufacturer);
		eDevice = (EditText) findViewById(R.id.device);
		eProduct = (EditText) findViewById(R.id.product);
		change = (Button) findViewById(R.id.change);
		x_import = (Button) findViewById(R.id.x_import);
		export = (Button) findViewById(R.id.export);
		reset = (Button) findViewById(R.id.reset);
		help = (Button) findViewById(R.id.help);
		xposed = (Button) findViewById(R.id.xposed_bl);
		mgzip = (Button) findViewById(R.id.mgzip);
		magisk = (Button) findViewById(R.id.magisk);
		busybox = (CheckBox) findViewById(R.id.bybx);
		anzhuo = (CheckBox) findViewById(R.id.anzhuo);
		compatibilityCheck = (Button) findViewById(R.id.cch);
		sModel = Build.MODEL;
		sManufacturer = Build.MANUFACTURER;
		sBrand = Build.BRAND;
		sProduct = Build.PRODUCT;
		sDevice = Build.DEVICE;
		filePath = getFilesDir().toString();
		apiLabel.setText(apiLabel.getText().toString() + "\nmodel:" + sModel + "\nbrand:" + sBrand + "\nmanufacturer:" + sManufacturer + "\nproduct:" + sProduct + "\ndevice:" + sDevice);
		if (!new File("/data/magisk/").exists())
		{
			magisk.setEnabled(false);
			toastText("无magisk", false);
		}
		if (!cmd(new String[]{"busybox echo test"}, false, false)[0].equals("test"))
		{
			busybox.setChecked(false);
			busybox.setEnabled(false);
			toastText("无busybox", false);
		}
		if (!new File(getFilesDir() + "/" + sp.getString("backup", null)).exists())
		{
			toastText("备份文件已丢失！", true);
			reset.setEnabled(false);
		}
		xposed.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					startActivity(new Intent(MainActivity.this, XposedCfgActivity.class));
				}
			});
		change.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("二次确定");
					ab.setMessage(getString(R.string.enter2));
					ab.setNegativeButton("确定", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								String tempCopy="/sdcard/bptemp_" + getTime();
								copyFile("/system/build.prop", tempCopy);
								changebp(tempCopy, false);
								replaceBuildProp(tempCopy);
								cmd(new String[]{"rm " + tempCopy}, true, true);
								if (anzhuo.isChecked())
								{
									fuckAnzhuoProp("/hw_oem/prop/local.prop", 0);
									fuckAnzhuoProp("/system/customize/clientid/default.prop", 0);
									fuckAnzhuoProp("/product/etc/prop/local.prop", 0);
								}
								toastText("已经完成修改", false);
								saveEdittext();
								needReboot(false);
							}
						});
					ab.create().show();
				}
			});
		x_import.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					LayoutInflater li=LayoutInflater.from(MainActivity.this);
					View v=li.inflate(R.layout.iedialog, null);
					final EditText ieE=(EditText) v.findViewById(R.id.iedialogE);
					TextView ieT=(TextView) v.findViewById(R.id.iedialogT);
					ieT.setText("请填入字符编码");
					ieE.requestFocus();
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("导入至编辑框");
					ab.setView(v);
					ab.setPositiveButton("确定导入", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								String[] s;
								s = cryptoS(ieE.getText().toString(), false).split("@");
								if (s.length != 5)
								{
									toastText("出现错误，编码格式有误", true);
									return;
								}
								eModel.setText(s[0]);
								eBrand.setText(s[1]);
								eManufacturer.setText(s[2]);
								eProduct.setText(s[3]);
								eDevice.setText(s[4]);
							}
						});
					ab.create().show();
				}
			});
		export.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					final String e=cryptoS(String.format("%s@%s@%s@%s@%s", eModel.getText().toString(), eBrand.getText().toString(), eManufacturer.getText().toString(), eProduct.getText().toString(), eDevice.getText().toString()), true);
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("导出编辑框上的机型信息");
					if (e.equals("QEBAQA==\n"))
						ab.setMessage("编辑框上内容全为空，无法备份！");
					else
					{
						ab.setMessage(e);
						ab.setPositiveButton("复制编码", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface p1, int p2)
								{
									ClipboardManager cm=(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
									cm.setText(e);
								}
							});
					}
					ab.create().show();
				}
			});
		mgzip.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					magiskZip();
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("已生成magisk模块");
					if (!new File("/data/magisk/").exists())
					{
						ab.setMessage("当前设备似乎没安装magisk，但仍可生成magisk模块");
					}
					ab.setNegativeButton("打开magisk manager", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								PackageManager pm=getPackageManager();
								try
								{
									startActivity(pm.getLaunchIntentForPackage("com.topjohnwu.magisk"));
								}
								catch (NullPointerException e)
								{
									e.printStackTrace();
									toastText("你似乎没有安装(或者冻结了)magisk manager...", false);
								}
							}
						});
					ab.create().show();
					return true;
				}
			});
		mgzip.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (new File("/magisk/MTC/").exists())
					{
						cmd(new String[]{"cp -f /magisk/MTC/system.prop /sdcard/CtrlTemp.a"}, true, true);
						changebp("/sdcard/CtrlTemp.a", false);
						cmd(new String[]{"cp -f /sdcard/CtrlTemp.a /magisk/MTC/system.prop"
								,"chmod 0644 /magisk/MTC/system.prop"
								,"rm -f /sdcard/CtrlTemp.a"}, true, true);
						toastText("修改完成，重启生效", true);
					}
					else
					{
						magiskZipCtrl();
						toastText("检测到你尚未安装magisk控制模块\nmagisk控制模块生成在" + Environment.getExternalStorageDirectory() + "/MTCmagiskMod_ControlBridge.zip", true);
					}
				}
			});
		reset.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("二次确定");
					ab.setMessage(getString(R.string.enter3));
					ab.setNegativeButton("确定", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								if (anzhuo.isChecked())
								{
									fuckAnzhuoProp("/hw_oem/prop/local.prop", 1);
									fuckAnzhuoProp("/system/customize/clientid/default.prop", 1);
									fuckAnzhuoProp("/product/etc/prop/local.prop", 1);
								}
								String bfn=sp.getString("backup", null);
								replaceBuildProp(filePath + "/" + bfn);
								toastText("恢复完成", false);
								needReboot(false);
							}
						});
					ab.create().show();
				}
			});
		help.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("帮助");
					ab.setMessage(getString(R.string.help));
					ab.setPositiveButton("官方链接", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/com.nijigenirubasho.mobiletailchanger"));
								startActivity(i);
							}
						});
					ab.setNegativeButton("busybox下载", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/ru.meefik.busybox")));
							}
						});
					ab.setNeutralButton("捐赠", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								try
								{
									startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://QR.ALIPAY.COM/FKX040845ILLL6VFEL7K06")));
								}
								catch (ActivityNotFoundException e)
								{
									Toast.makeText(getApplicationContext(), "你的系统没有安装浏览器或者不兼容此操作", Toast.LENGTH_SHORT).show();
								}
							}
						});
					ab.create().show();
				}
			});
		magisk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					changebp(null, true);
					toastText("已经完成修改", false);
					saveEdittext();
					needReboot(true);
				}
			});
		apiLabel.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("机型环境检测");
					ab.setMessage(getAllModelInfo());
					ab.create().show();
					return false;
				}
			});
		compatibilityCheck.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					String wrBody="test data";
					String fDir=getFilesDir().toString();
					String fileName="test.data";
					String report="1.shell读写\n###标准输出(以下应该显示为三段连续的test data和权限为rwxrwxrwx的文件信息):\n" + stringArrayToString(
						cmd(new String[]{
								"mount -o rw,remount /",
								"mount -o rw,remount /system",
								"mount -o rw,remount /dev/block/bootdevice/by-name/system /system",
								"echo " + wrBody + " > /system/" + fileName,
								"echo " + wrBody + " > /sdcard/" + fileName,
								"echo " + wrBody + " > " + fDir + "/" + fileName,
								"cat /system/" + fileName,
								"cat /sdcard/" + fileName,
								"cat " + fDir + "/" + fileName,
								"chmod 777 /system/" + fileName,
								"ls -l /system/" + fileName,
								"rm /system/" + fileName,
								"rm /sdcard/" + fileName,
								"rm " + fDir + "/" + fileName
							}, true, true)
						, "\n###标准错误输出(以下应该显示null):\n");
					String report2="\n2.build.prop备份验证\n###build.prop备份文件特征(请检查文件日期大小是否正常):\n" + 
						stringArrayToString(cmd(new String[]{"ls -l " + getFilesDir() + "/" + sp.getString("backup", null)}, true, true), "\n###标准错误输出(以下应该显示null):\n") + 
						"\n!!!请仔细检查报告是否符合要求，若不符合则表明使用本应用有风险";
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("兼容性检查报告");
					ab.setMessage(report + report2);
					ab.setNegativeButton("关闭", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								toastText("一次的结果可能不够准确，请测试三次以上以确认最终结果", false);
								return;
							}
						});
					ab.create().show();
				}
			});
		try
		{
			if (sp.getInt("version", 0) != getPackageManager().getPackageInfo(getPackageName(), 0).versionCode)
			{
				AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				ab.setTitle("欢迎");
				ab.setMessage(getString(R.string.welcome));
				if (sp.getInt("version", 0) == 0)
					ab.setOnCancelListener(new DialogInterface.OnCancelListener(){

							@Override
							public void onCancel(DialogInterface p1)
							{
								requestRoot();
								spe.putString("brand", sBrand);
								spe.putString("model", sModel);
								spe.putString("manufacturer", sManufacturer);
								spe.putString("product", sProduct);
								spe.putString("device", sDevice);
								spe.commit();
								backup();
								Intent i = getPackageManager().getLaunchIntentForPackage(getPackageName());  
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
								startActivity(i);
							}
						});
				else
				{
					eBrand.setText(sp.getString("brand", null));
					eModel.setText(sp.getString("model", null));
					eManufacturer.setText(sp.getString("manufacturer", null));
					eProduct.setText(sp.getString("product", null));
					eDevice.setText(sp.getString("device", null));
					busybox.setChecked(sp.getBoolean("busybox", false));
					anzhuo.setChecked(sp.getBoolean("anzhuo", false));
					requestRoot();
				}
				try
				{
					spe.putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
				}
				catch (PackageManager.NameNotFoundException e)
				{}
				spe.commit();
				ab.create().show();
			}
			else
			{
				eBrand.setText(sp.getString("brand", null));
				eModel.setText(sp.getString("model", null));
				eManufacturer.setText(sp.getString("manufacturer", null));
				eProduct.setText(sp.getString("product", null));
				eDevice.setText(sp.getString("device", null));
				busybox.setChecked(sp.getBoolean("busybox", false));
				anzhuo.setChecked(sp.getBoolean("anzhuo", false));
				requestRoot();
			}
		}
		catch (PackageManager.NameNotFoundException e)
		{}
	}
	@Override
	protected void onStop()
	{
		saveEdittext();
		toastText("主界面设置已保存", false);
		super.onStop();
	}
	void toastText(String text, Boolean isLong)
	{
		int i;
		if (isLong)
			i = Toast.LENGTH_LONG;
		else
			i = Toast.LENGTH_SHORT;
		Toast.makeText(getApplicationContext(), text, i).show();
	}
	String getTime()
	{
		SimpleDateFormat spf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date d= new Date(System.currentTimeMillis());
		return spf.format(d);
	}
	String[] cmd(String[] command, boolean isRoot, boolean retError)
	{
		StringBuilder sb = new StringBuilder();
		StringBuilder sberr=new StringBuilder();
		String[]ret=new String[2];
		try
		{
			Process p ;
			if (isRoot)
			{
				p = Runtime.getRuntime().exec("su");
			}
			else
			{
				p = Runtime.getRuntime().exec("sh");
			}
			OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream(), "UTF-8");
			for (int i=0;i < command.length;i++)
			{
				osw.write(command[i] + "\n");
			}
			osw.write("exit\n");
			osw.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader brerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String lerr="";
			String l = "";
			while ((l = br.readLine()) != null)
			{
				sb.append(l);
			}
			ret[0] = sb.toString();
			while ((lerr = brerr.readLine()) != null)
			{
				sberr.append(lerr + "\n");
			}
			p.getErrorStream().close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		if (sberr.length() > 0)
		{
			sberr.deleteCharAt(sberr.length() - 1);
			ret[1] = sberr.toString();
		}
		if (retError)
		{
			if (!sberr.toString().equals(""))
			{
				toastText(getString(R.string.shell_error) + sberr.toString(), true);
			}
		}
		return ret;
	}
	void copyFile(String oldPath, String newPath)
	{ 
		try
		{ 
			int bytesum = 0; 
			int byteread = 0;
			InputStream inStream = new FileInputStream(oldPath);
			FileOutputStream fs = new FileOutputStream(newPath); 
			byte[] buffer = new byte[1024]; 
			while ((byteread = inStream.read(buffer)) != -1)
			{ 
				bytesum += byteread;
				fs.write(buffer, 0, byteread); 
			} 
			inStream.close(); 
		} 
		catch (Exception e)
		{ 
			e.printStackTrace(); 
		} 
	} 
	synchronized boolean isRoot()  
	{  
		Process process = null;  
		DataOutputStream os = null;  
		try  
		{  
			process = Runtime.getRuntime().exec("su");  
			os = new DataOutputStream(process.getOutputStream());  
			os.writeBytes("exit\n");  
			os.flush();  
			int exitValue = process.waitFor();  
			if (exitValue == 0)  
			{  
				return true;  
			}
			else  
			{  
				return false;  
			}  
		}
		catch (Exception e)  
		{  
			e.printStackTrace();
			return false;  
		}
		finally  
		{  
			try  
			{  
				if (os != null)  
				{  
					os.close();  
				}  
				process.destroy();  
			}
			catch (Exception e)  
			{  
				e.printStackTrace();  
			}  
		}  
	}  
	String cryptoS(String s, Boolean switi)
	{
		if (switi)
		{
			return Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
		}
		else
		{
			return new String(Base64.decode(s.getBytes(), Base64.DEFAULT));
		}
	}
	void needReboot(boolean isMagisk)
	{
		AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		if (!isMagisk)
		{
			ab.setTitle("是否需要重启？");
			ab.setMessage(getString(R.string.reboot) + "\n\n***所修改的文件特征如下***\n\n" + fileinfo);
			ab.setNegativeButton("强制重启", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						cmd(new String[]{"busybox reboot -f","reboot"}, true, true);
					}
				});
			if (cmd(new String[]{"svc power"}, false, false)[1].contains("reboot"))
			{
				ab.setPositiveButton("普通重启", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							cmd(new String[]{"svc power reboot"}, true, true);	
						}
					});
			}
			else
			{
				toastText("你的系统不支持普通重启！请使用其他重启方式！", false);
			}			
			ab.create().show();
		}
		else
		{
			ab.setTitle("是否需要热重启？");
			ab.setMessage(getString(R.string.magisk_reboot));
			ab.setPositiveButton("热重启", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						cmd(new String[]{"setprop ctl.restart zygote"}, true, true);
					}
				});
			ab.create().show();
		}
	}
	void backup()
	{
		fuckAnzhuoProp("/hw_oem/prop/local.prop", 2);
		fuckAnzhuoProp("/system/customize/clientid/default.prop", 2);
		fuckAnzhuoProp("/product/etc/prop/local.prop", 2);
		String backupFileName="bpbackup_" + getTime();
		String sESDdir=Environment.getExternalStorageDirectory() + "/BuildPropBackup";
		cmd(new String[]{"cp /system/build.prop " + filePath + "/" + backupFileName}, false, true);
		spe.putString("backup", backupFileName);
		spe.commit();
		cmd(new String[]{"mkdir " + sESDdir}, false, false);
		copyFile("/system/build.prop", sESDdir + "/build_" + getTime() + ".prop");
	}
	void propWrite(String k, String v, String p, boolean isMagisk)
	{
		StringBuilder sb=new StringBuilder();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(p)));
			for (String line = br.readLine(); line != null; line = br.readLine())
			{  
				if (line.contains(k + "=") && line.charAt(0) != '#')
				{
					sb.append(k + "=" + v + "\n");
				}
				else
				{
					sb.append(line + "\n");
				}
			}
			if (!sb.toString().contains(k + "="))
			{
				sb.append(k + "=" + v);
			}
			else
			{
				sb.deleteCharAt(sb.length() - 1);
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}  
		if (!isMagisk)
		{
			writeFile(sb.toString(), p);
		}
		else
		{
			if (new File("/data/magisk/resetprop").exists())
				cmd(new String[]{"/data/magisk/resetprop \"" + k + "\" \"" + v + "\""}, true, true);
			else
				cmd(new String[]{"/data/magisk/magisk resetprop \"" + k + "\" \"" + v + "\""}, true, true);
		}
	}  
	void saveEdittext()
	{
		spe.putString("brand", eBrand.getText().toString());
		spe.putString("model", eModel.getText().toString());
		spe.putString("manufacturer", eManufacturer.getText().toString());
		spe.putString("product", eProduct.getText().toString());
		spe.putString("device", eDevice.getText().toString());
		spe.putBoolean("busybox", busybox.isChecked());
		spe.putBoolean("anzhuo", anzhuo.isChecked());
		spe.commit();
	}
	void replaceBuildProp(String from)
	{
		String time=getTime();
		String temp="/system/build_rm_" + time + ".prop";
		String selinuxoff="setenforce 0";
		String mount2="mount -o rw,remount /dev/block/bootdevice/by-name/system /system";
		String rwpercfg="chattr -i /system/build.prop";
		String mountrw="mount -o remount,rw /system";
		String rename="mv -f /system/build.prop " + temp;
		String copy="cp -f " + from + " /system/build.prop";
		String change_mode="chmod 0644 /system/build.prop";
		String delete_temp="rm -f " + temp;
		String sync="sync";
		String mountro="mount -o remount,ro /system";
		if (busybox.isChecked())
		{
			mount2 = bsbx_head + mount2;
			rwpercfg = bsbx_head + rwpercfg;
			mountrw = bsbx_head + mountrw;
			rename = bsbx_head + rename;
			copy = bsbx_head + copy;
			change_mode = bsbx_head + change_mode;
			delete_temp = bsbx_head + delete_temp;
			mountro = bsbx_head + mountro;
			sync = bsbx_head + sync;
		}
		cmd(new String[]{selinuxoff,mount2,mountrw,rwpercfg,rename,copy,change_mode,delete_temp,sync,mountro}, true, true);
		fileinfo = stringArrayToString(cmd(new String[]{"ls -l /system/build.prop"}, false, true), "\n");
	}
	void fuckAnzhuoProp(String src, int flag)
	{
		switch (flag)
		{
			case 0:
				if (new File(src).exists())
				{
					String t=getTime();
					String tmp=Environment.getExternalStorageDirectory() + "/AnzhuoBackup"
						+ src.subSequence(src.lastIndexOf("/"), src.length()).toString() + t;
					Log.d(tag, "src:" + src + " tmp:" + tmp);
					cmd(new String[]{"mkdir "
							+ Environment.getExternalStorageDirectory() + "/AnzhuoBackup",
							"cp " + src + " " + tmp}, false, false);
					changebp(tmp, false);
					cmd(new String[]{
							"setenforce 0",
							"mount -o rw,remount /",
							"mount -o rw,remount /system",
							"mount -o rw,remount /dev/block/bootdevice/by-name/system /system",
							"chattr -i " + src,
							"cp " + src + " " + src + "bak" + t,
							"cp -f " + tmp + " " + src,
							"chmod 0644 " + src,
							"rm -f " + src + "bak" + t,
							"sync"
						}
						, true, true);
					fileinfo += "\n\n\"" + src + "\":\n" + stringArrayToString(cmd(new String[]{"ls -l " + src}, false, true), "\n");
				}
				else
					Log.e(tag, src + " not exist");
				break;
			case 1:
				String b=sp.getString(src, null);
				if (b != null)
				{
					cmd(new String[]{
							"setenforce 0",
							"mount -o rw,remount /",
							"mount -o rw,remount /system",
							"mount -o rw,remount /dev/block/bootdevice/by-name/system /system",
							"chattr -i " + src,
							"cp -f " + b + " " + src,
							"chmod 0644 " + src,
							"sync"
						}
						, true, true);
					fileinfo += "\n\n\"" + src + "\":\n" + stringArrayToString(cmd(new String[]{"ls -l " + src}, false, true), "\n");
				}
				else
					toastText(src + "文件的备份已删除，还原失败", false);
				break;
			case 2:
				if (new File(src).exists())
				{
					String t=getTime();
					String tmp=Environment.getExternalStorageDirectory() + "/AnzhuoBackup"
						+ src.subSequence(src.lastIndexOf("/"), src.length()).toString() + t;
					cmd(new String[]{"mkdir "
							+ Environment.getExternalStorageDirectory() + "/AnzhuoBackup",
							"cp " + src + " " + tmp}, false, false);
					spe.putString(src, tmp);
					spe.commit();
				}
				break;
		}
	}
	void requestRoot()
	{
		if (!isRoot())
		{
			toastText("没有检测到root权限，你不能修改机型", true);
			change.setEnabled(false);
			magisk.setEnabled(false);
			reset.setEnabled(false);
			anzhuo.setChecked(false);
			anzhuo.setEnabled(false);
			busybox.setChecked(false);
			busybox.setEnabled(false);
		}
	}
	void changebp(String tempCopy, boolean isMagisk)
	{
		String tempMA=eManufacturer.getText().toString();
		String tempBR=eBrand.getText().toString();
		String tempMO=eModel.getText().toString();
		String tempPR=eProduct.getText().toString();
		String tempDE=eDevice.getText().toString();
		if (!tempMA.equals("") && !tempMA.equals(sManufacturer) && !tempMA.equals("#nc#"))
		{
			propWrite("ro.product.manufacturer", tempMA, tempCopy, isMagisk);
		}
		if (!tempBR.equals("") && !tempBR.equals(sBrand) && !tempBR.equals("#nc#"))
		{
			propWrite("ro.product.brand", tempBR , tempCopy, isMagisk);
		}
		if (!tempMO.equals("") && !tempMO.equals(sModel) && !tempMO.equals("#nc#"))
		{
			propWrite("ro.product.model", tempMO, tempCopy, isMagisk);
		}
		if (!tempPR.equals("") && !tempPR.equals(sProduct) && !tempPR.equals("#nc#"))
		{
			propWrite("ro.product.name", tempPR, tempCopy, isMagisk);
		}
		if (!tempDE.equals("") && !tempDE.equals(sDevice) && !tempDE.equals("#nc#"))
		{
			propWrite("ro.product.device", tempDE, tempCopy, isMagisk);
		}
	}
	void writeFile(String s, String p)
	{  
		try
		{
			File file = new File(p);  
			File dir=new File(p.subSequence(0, p.lastIndexOf("/")).toString());
			if (!dir.exists())
			{
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);  
			byte [] bytes = s.getBytes(); 
			fos.write(bytes); 
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
	} 
	String getAllModelInfo()
	{
		String enter="\n";
		String tab="    ";
		StringBuilder sb=new StringBuilder("(model,manufacturer,brand,product,device)");
		sb.append(enter + "1.from android.os.Build");
		sb.append(enter + tab + Build.MODEL);
		sb.append(enter + tab + Build.MANUFACTURER);
		sb.append(enter + tab + Build.BRAND);
		sb.append(enter + tab + Build.PRODUCT);
		sb.append(enter + tab + Build.DEVICE);
		sb.append(enter + "2.from getprop");
		sb.append(enter + tab + cmd(new String[]{"getprop ro.product.model"}, false, false)[0]);
		sb.append(enter + tab + cmd(new String[]{"getprop ro.product.manufacturer"}, false, false)[0]);
		sb.append(enter + tab + cmd(new String[]{"getprop ro.product.brand"}, false, false)[0]);
		sb.append(enter + tab + cmd(new String[]{"getprop ro.product.name"}, false, false)[0]);
		sb.append(enter + tab + cmd(new String[]{"getprop ro.product.device"}, false, false)[0]);
		sb.append(enter + "3.from /system/build.prop");
		Properties bp=new Properties();
		try
		{
			bp.load(new FileInputStream(new File("/system/build.prop")));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		sb.append(enter + tab + (String) bp.get("ro.product.model"));
		sb.append(enter + tab + (String) bp.get("ro.product.manufacturer"));
		sb.append(enter + tab + (String) bp.get("ro.product.brand"));
		sb.append(enter + tab + (String) bp.get("ro.product.name"));
		sb.append(enter + tab + (String) bp.get("ro.product.device"));
		sb.append(enter + "#END");
		return sb.toString();
	}
	String fileTxtRead(String fileName)
	{  
		String res="";
        try
		{
			File file = new File(fileName);  
			FileInputStream fis = new FileInputStream(file);  
			int length = fis.available(); 
			byte [] buffer = new byte[length]; 
			fis.read(buffer);     
			res = EncodingUtils.getString(buffer, "UTF-8"); 
			fis.close();     
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}  
		return res;
	}  
	void magiskZip()
	{
		String t=getTime();
		String tmpdir=Environment.getExternalStorageDirectory() + "/MTCmagiskTmp_" + t;
		unzipAssetRes("mgzip_MTC.zip", tmpdir + "/unzipTmp", true);
		String tmzdir=tmpdir + "/unzipTmp/";
		propWrite("MODID", t, tmzdir + "config.sh", false);
		propWrite("id", t, tmzdir + "module.prop", false);
		propWrite("name", "机型修改_ID:" + t, tmzdir + "module.prop", false);
		changebp(tmzdir + "common/system.prop", false);
		String intro="这是由\"机型更改\"(com.nijigenirubasho.mobiletailchanger)自动生成的magisk模块(ID:" + t + ")";
		propWrite("description", intro, tmzdir + "module.prop", false);
		String cfgshbody=fileTxtRead(tmzdir + "config.sh");
		cfgshbody = cfgshbody.replace("**********", intro);
		writeFile(cfgshbody, tmzdir + "config.sh");
		zip(tmzdir, Environment.getExternalStorageDirectory() + "/MTCmagiskMod_" + t + ".zip", false);
		clearDir(new File(tmpdir));
	}
	void magiskZipCtrl()
	{
		String t=getTime();
		String tmpdir=Environment.getExternalStorageDirectory() + "/MTCmagiskTmp_" + t;
		unzipAssetRes("mgzip_MTC.zip", tmpdir + "/unzipTmp", true);
		String tmzdir=tmpdir + "/unzipTmp/";
		propWrite("MODID", "MTC", tmzdir + "config.sh", false);
		propWrite("id", "MTC", tmzdir + "module.prop", false);
		propWrite("name", "机型修改_magisk控制模块", tmzdir + "module.prop", false);
		String intro="这是\"机型更改\"(com.nijigenirubasho.mobiletailchanger)的magisk控制模块";
		propWrite("description", intro, tmzdir + "module.prop", false);
		String cfgshbody=fileTxtRead(tmzdir + "config.sh");
		cfgshbody = cfgshbody.replace("**********", intro);
		writeFile(cfgshbody, tmzdir + "config.sh");
		zip(tmzdir, Environment.getExternalStorageDirectory() + "/MTCmagiskMod_ControlBridge.zip", false);
		clearDir(new File(tmpdir));
	}
	String stringArrayToString(String[] in, String dot)
	{
		String out = "";
		for (int i=0;i < in.length;i++)
		{
			if (i == in.length - 1)
			{
				out += in[i];
			}
			else
			{
				out += in[i] + dot;
			}
		}
		return out;
	}
	void unzipAssetRes(String assetName, String outputDirectory, boolean isReWrite)
	{
		File f = new File(outputDirectory);
		if (!f.exists())
		{
			f.mkdirs();
		}
		try
		{
			InputStream is = getApplicationContext().getAssets().open(assetName);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();
			byte[] buffer = new byte[1024];
			int i = 0;
			while (ze != null)
			{
				if (ze.isDirectory())
				{
					f = new File(outputDirectory + File.separator + ze.getName());
					if (isReWrite || !f.exists())
					{
						f.mkdir();
					}
				}
				else
				{
					f = new File(outputDirectory + File.separator + ze.getName());
					if (isReWrite || !f.exists())
					{
						f.createNewFile();
						FileOutputStream fos = new FileOutputStream(f);
						while ((i = zis.read(buffer)) > 0)
						{
							fos.write(buffer, 0, i);
						}
						fos.close();
					}
				}
				ze = zis.getNextEntry();
			}
			zis.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	void zip(String path, String zippath, boolean isContainPrimaryDir)
	{
		try
		{
			File f = new File(path);
			File zipFile = new File(zippath);
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
			if (f.isDirectory())
			{
				File[] files = f.listFiles();
				for (File fileSec:files)
				{
					if (isContainPrimaryDir)
						zip2(zos, fileSec, f.getName() + File.separator);
					else
						zip2(zos, fileSec, "");
				}
			}
			zos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
    private void zip2(ZipOutputStream zos, File f, String baseDir) throws Exception
	{
        if (f.isDirectory())
		{
            File[] fs = f.listFiles();
            for (File fileSec:fs)
			{
                zip2(zos, fileSec, baseDir + f.getName() + File.separator);
            }
        }
		else
		{
            byte[] buf = new byte[1024];
            InputStream is = new FileInputStream(f);
            zos.putNextEntry(new ZipEntry(baseDir + f.getName()));
            int len;
            while ((len = is.read(buf)) != -1)
			{
                zos.write(buf, 0, len);
            }
            is.close();
        }
    }
	void clearDir(File f1)
	{  
        if (f1.isDirectory())
		{  
            for (File f2 : f1.listFiles())
			{  
                clearDir(f2);  
                f2.delete();  
            }  
        }  
        f1.delete();  
    }  
}
