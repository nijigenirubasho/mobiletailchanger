package com.nijigenirubasho.mobiletailchanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sp = getSharedPreferences("config", MODE_WORLD_WRITEABLE);
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
		magisk = (Button) findViewById(R.id.magisk);
		sModel = Build.MODEL;
		sManufacturer = Build.MANUFACTURER;
		sBrand = Build.BRAND;
		sProduct = Build.PRODUCT;
		sDevice = Build.DEVICE;
		filePath = getFilesDir().toString();

		apiLabel.setText(apiLabel.getText().toString() + "\nmodel:" + sModel + "\nbrand:" + sBrand + "\nmanufacturer:" + sManufacturer + "\nproduct:" + sProduct + "\ndevice:" + sDevice);
		if (!new File("/data/magisk/resetprop").exists())
		{
			magisk.setEnabled(false);
			toastText("无magisk", false);
		}
		if (!cmd(new String[]{"busybox echo test"}, false, false)[0].equals("test"))
		{
			change.setEnabled(false);
			reset.setEnabled(false);
			toastText("无busybox", false);
		}
		change.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					String tempCopy="/sdcard/bptemp_" + getTime();
					copyFile("/system/build.prop", tempCopy);
					changebp(tempCopy, false);
					replaceBuildProp(tempCopy);
					cmd(new String[]{"busybox rm " + tempCopy}, true, true);
					toastText("已经完成修改", false);
					saveEdittext();
					needReboot(false);
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
					ab.setMessage(e);
					ab.setPositiveButton("复制编码", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								ClipboardManager cm=(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
								cm.setText(e);
							}
						});
					ab.create().show();
				}
			});
		reset.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					String bfn=sp.getString("backup", null);
					replaceBuildProp(filePath + "/" + bfn);
					toastText("恢复完成", false);
					needReboot(false);
				}
			});
		help.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
					ab.setTitle("帮助");
					ab.setMessage(getString(R.string.help));
					ab.setPositiveButton("官方下载链接", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/com.nijigenirubasho.mobiletailchanger"));
								startActivity(i);
							}
						});
					ab.setNegativeButton("busybox下载地址", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/ru.meefik.busybox")));
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
		if (!new File(filePath + "/Hymen").exists())
		{
			AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			ab.setTitle("欢迎");
			ab.setMessage(getString(R.string.welcome));
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
						try
						{
							new File(filePath + "/Hymen").createNewFile();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						Intent i = getPackageManager().getLaunchIntentForPackage(getPackageName());  
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
						startActivity(i);
					}
				});
			ab.create().show();
		}
		else
		{
			requestRoot();
			eBrand.setText(sp.getString("brand", null));
			eModel.setText(sp.getString("model", null));
			eManufacturer.setText(sp.getString("manufacturer", null));
			eProduct.setText(sp.getString("product", null));
			eDevice.setText(sp.getString("device", null));
		}
	}
	@Override
	protected void onStop()
	{
		saveEdittext();
		toastText("编辑框的文本已保存", false);
		super.onStop();
	}

	void toastText(String text, Boolean isLong)
	{
		int i=Toast.LENGTH_SHORT;
		if (isLong)
		{
			i = Toast.LENGTH_LONG;
		}
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
			ab.setMessage(getString(R.string.reboot));
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
		String backupFileName="bpbackup_" + getTime();
		cmd(new String[]{"busybox cp /system/build.prop " + filePath + "/" + backupFileName}, false, true);
		spe.putString("backup", backupFileName);
		spe.commit();
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
			cmd(new String[]{"/data/magisk/resetprop " + k + " " + v}, true, true);
		}
	}  
	void saveEdittext()
	{
		spe.putString("brand", eBrand.getText().toString());
		spe.putString("model", eModel.getText().toString());
		spe.putString("manufacturer", eManufacturer.getText().toString());
		spe.putString("product", eProduct.getText().toString());
		spe.putString("device", eDevice.getText().toString());
		spe.commit();
	}
	void replaceBuildProp(String from)
	{
		String time=getTime();
		String temp="/system/build_rm_" + time + ".prop";
		String bsbx_head="busybox ";
		String mountrw="mount -o remount,rw /system";
		String bmountrw=bsbx_head + mountrw;
		String rename=bsbx_head + "mv -f /system/build.prop " + temp;
		String copy=bsbx_head + "cp -f " + from + " /system/build.prop";
		String change_mode=bsbx_head + "chmod 0644 /system/build.prop";
		String delete_temp=bsbx_head + "rm -f " + temp;
		String mountro="mount -o remount,ro /system";
		String bmountro=bsbx_head + mountro;
		cmd(new String[]{mountrw,bmountrw,rename,copy,change_mode,delete_temp,bmountro,mountro}, true, true);
	}
	void requestRoot()
	{
		if (!isRoot())
		{
			toastText("没有检测到root权限，你不能修改机型", true);
			change.setEnabled(false);
			magisk.setEnabled(false);
			reset.setEnabled(false);
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
}
