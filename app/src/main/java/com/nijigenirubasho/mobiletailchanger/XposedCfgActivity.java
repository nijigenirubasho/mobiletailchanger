package com.nijigenirubasho.mobiletailchanger;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class XposedCfgActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xposed);
		final EditText et=(EditText) findViewById(R.id.xposededit);
		Button btn=(Button) findViewById(R.id.savex);
		final SharedPreferences sp=getSharedPreferences("xposed_blacklist", MODE_WORLD_READABLE);
		et.setText(sp.getString("main", ""));
		btn.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					Editor e=sp.edit();
					e.putString("main", et.getText().toString());
					e.commit();
					Toast.makeText(getApplicationContext(), "已保存并生效", Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Toast.makeText(getApplicationContext(), "正在生成列表，这可能需要一段时间，请稍候...", Toast.LENGTH_LONG).show();
					new Timer().schedule(new TimerTask()
						{
							public void run()
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() 
									{
										@Override
										public void run()
										{
											LayoutInflater li=LayoutInflater.from(XposedCfgActivity.this);
											View v=li.inflate(R.layout.pndialog, null);
											final EditText pnlist=(EditText) v.findViewById(R.id.pndialogEditText1);
											final EditText searche=(EditText) v.findViewById(R.id.pndialogEditText2);
											Button searchb=(Button) v.findViewById(R.id.pndialogButton1);
											Button add=(Button) v.findViewById(R.id.pndialogButton2);
											Button addAll=(Button) v.findViewById(R.id.pndialogButton3);
											AlertDialog.Builder ab=new AlertDialog.Builder(XposedCfgActivity.this);
											ab.setTitle("包名获取界面");
											ab.setView(v);
											AlertDialog d= ab.create();
											d.setOnCancelListener(new DialogInterface.OnCancelListener()
												{
													@Override
													public void onCancel(DialogInterface p1)
													{
														Toast.makeText(getApplicationContext(), "如果已经添加，记得保存。", Toast.LENGTH_SHORT).show();
													}
												});
											d.show();
											searchb.setOnClickListener(new OnClickListener(){

													@Override
													public void onClick(View p1)
													{
														String keyword=searche.getText().toString();
														int startPosition=pnlist.getText().toString().indexOf(keyword, pnlist.getSelectionEnd());
														int searchlength=keyword.length();
														if (searchlength == 0)
														{
															Toast.makeText(getApplicationContext(), "文本为空！请输入再搜索～", Toast.LENGTH_SHORT).show();
															return;
														}
														if (startPosition != -1)
														{
															pnlist.setSelection(startPosition, startPosition + searchlength);
															pnlist.requestFocus();
														}
														else
														{
															Toast.makeText(getApplicationContext(), "找不到该文本！", Toast.LENGTH_SHORT).show();
														}
													}
												});
											add.setOnClickListener(new OnClickListener()
												{
													@Override
													public void onClick(View p1)
													{
														String selectedText=pnlist.getText().subSequence(pnlist.getSelectionStart(), pnlist.getSelectionEnd()).toString();
														if (selectedText.equals(""))
														{
															Toast.makeText(getApplicationContext(), "无选中文本！", Toast.LENGTH_SHORT).show();
															return;
														}
														et.setText(et.getText().toString() + "\n" + selectedText);
													}
												});
											addAll.setOnClickListener(new OnClickListener(){

													@Override
													public void onClick(View p1)
													{
														et.setText(getAllApp(true));
													}
												});
											pnlist.setText(getAllApp(false));
										}
									});
							}
						}, 500);
				}
			});
	}
	String getAllApp(boolean isOnlyGetPackageName)
	{
		String ret = "";
		PackageManager pm = getPackageManager();
		List<PackageInfo> ls = pm.getInstalledPackages(0);
		for (int i=0;i < ls.size();i++)
		{
			PackageInfo pi=ls.get(i);
			if (!isOnlyGetPackageName)
			{
				if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)	
				{
					ret += "一般应用\n";
				}
				else
				{
					ret += "系统应用\n";
				}
			}
			if (!isOnlyGetPackageName)
			{
				ret += pi.applicationInfo.loadLabel(pm).toString() + "\n" + pi.packageName + "\n\n";
			}
			else
			{
				ret += pi.packageName + "\n";
			}
		}
		return ret;
	}
}
