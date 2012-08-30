package com.baidu.pcsdemonote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import com.baidu.mobstat.StatService;
import com.baidu.pcs.BaiduPCSAPI;
import com.baidu.pcs.BaiduPCSStatusListener;
import com.baidu.pcs.PCSActionInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Author: ganxun(ganxun@baidu.com)
 * Time:   2012.7.10
 * 
 */

@SuppressWarnings("unused")
public class EditActivity extends Activity {
    /** Called when the activity is first created. */
	
	private TextView title = null;	
	private EditText content = null;	
	private ImageButton editBack = null;
	private ImageButton save = null;
	
	private String output_content = null;		
	private int save_Flag = 0;	
	BaiduPCSAction editNote = new BaiduPCSAction(); 
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        
//        editNote.download(EditActivity.this);
        download();
        
        title = (TextView)findViewById(R.id.edit_title);
        content = (EditText)findViewById(R.id.edit_content);        
        editBack = (ImageButton)findViewById(R.id.btneditback);
        save = (ImageButton)findViewById(R.id.btneditsave);
        
        PCSDemoInfo.status = 1;        
        PCSDemoInfo.uiThreadHandler = new Handler();
               
        title.setText(PCSDemoInfo.fileTitle);	               
    
        editBack.setOnClickListener(new Button.OnClickListener(){
        	
        	public void onClick(View v){        		
        		editNote.back(EditActivity.this);       
        	}
        });
        
        save.setOnClickListener(new Button.OnClickListener(){
        	
        	public void onClick(View v){
        		
        		PCSDemoInfo.fileContent = content.getText().toString();        		
        		editNote.save(EditActivity.this);
        	}
        });       
    }
    
    
    public void download(){
    	
    	if(null != PCSDemoInfo.access_token){

    		Thread workThread = new Thread(new Runnable(){
				public void run() {

		    		BaiduPCSAPI api = new BaiduPCSAPI();
		    		api.setAccessToken(PCSDemoInfo.access_token);
		    		
		    		//Get the download file storage path on cloud
		    		PCSDemoInfo.sourceFile = PCSDemoInfo.bdRootPath + PCSDemoInfo.fileTitle+".txt";
		    		
		    		//Set the download file storage path
		    		PCSDemoInfo.target = getApplicationContext().getFilesDir()+"/"+PCSDemoInfo.fileTitle+".txt";
		    		
		    		//Call PCS downloadFile API
		    		final PCSActionInfo.PCSSimplefiedResponse downloadResponse = api.downloadFile(PCSDemoInfo.sourceFile, PCSDemoInfo.target,  new BaiduPCSStatusListener(){

						@Override
						public void onProgress(long bytes, long total) {
							// TODO Auto-generated method stub								
						}		    			
		    		});
		    		
		    		PCSDemoInfo.uiThreadHandler.post(new Runnable(){
		    			public void run(){
		    				
		    				if(downloadResponse.error_code == 0){
			    				try{
			    					//The local store download files
				    				File file = new File(PCSDemoInfo.target);			    				
				    				FileInputStream inStream = new FileInputStream(file);
				    				
				    				int length = inStream.available();				    				
				    				byte [] buffer = new byte[length];				    				
				    				inStream.read(buffer);
				    				
				    				PCSDemoInfo.fileContent = EncodingUtils.getString(buffer, "UTF-8");				    				
				    		        content.setText(PCSDemoInfo.fileContent);
				    		        
				    				inStream.close();
				    								    				
			    				}catch (Exception e) {
									// TODO: handle exception
			    					
			    					Toast.makeText(getApplicationContext(), "读取文件失败！", Toast.LENGTH_SHORT).show();
								}
		    				}else{
		    					
		    					Toast.makeText(getApplicationContext(), "下载失败！", Toast.LENGTH_SHORT).show();
		    				}	
		    			}
		    		});	
				}
			});
			 
    		workThread.start();
    	}
    }
    
	public void onResume() {
		
		super.onResume();
		StatService.onResume(this);
	}

	public void onPause() {
		
		super.onPause();
		StatService.onPause(this);
		finish();
	}
    
    // Back to the show content activity
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
	    menu.add(0, PCSDemoInfo.ITEM0, 0,"退出");
	    menu.add(0, PCSDemoInfo.ITEM1, 0, "关于我们");
	    
	    return true;
	}  
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		
		 switch (item.getItemId()) {
		     case PCSDemoInfo.ITEM0:
		    	 editNote.exit(EditActivity.this);
		         break;
		     case PCSDemoInfo.ITEM1:		    	 
		    	 Toast.makeText(getApplicationContext(), "自由开发者，呵呵！", Toast.LENGTH_SHORT).show();
		         break;
		 }
		 
		return true;
	}

}
