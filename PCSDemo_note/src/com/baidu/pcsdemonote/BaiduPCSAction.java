package com.baidu.pcsdemonote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.http.util.EncodingUtils;
import com.baidu.oauth2.BaiduOAuth;
import com.baidu.oauth2.BaiduOAuthViaDialog;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.baidu.pcs.BaiduPCSAPI;
import com.baidu.pcs.BaiduPCSStatusListener;
import com.baidu.pcs.PCSActionInfo;
import com.baidu.pcs.PCSActionInfo.PCSFileInfoResponse;
import com.baidu.pcsdemonote.BaiduPCSAction;


public class BaiduPCSAction {
	
	public BaiduOAuth bdOauth = null;

    // Get access_token 
    public void login(final Context context){    	
    	if(null != PCSDemoInfo.access_token){ 
    		//If the access_token is not null, start ContentActivity
			Intent intent = new Intent();    				    						    				
			intent.setClass(context, ContentActivity.class); 				
			context.startActivity(intent); 
    	}else{    		
    		bdOauth = new BaiduOAuthViaDialog(PCSDemoInfo.app_key);
        	try {
        		//Start OAUTH dialog
        		bdOauth.startDialogAuth(context, new String[]{"basic", "netdisk"}, new BaiduOAuthViaDialog.DialogListener(){
        			//Login successful 
        			public void onComplete(Bundle values) {
        				//Get access_token
        				PCSDemoInfo.access_token = values.getString("access_token");
        				
        				Intent intent = new Intent();    				    						    				
        				intent.setClass(context, ContentActivity.class); 				
        				context.startActivity(intent);    				
        			}

        			// TODO: the error code need be redefined
        			@SuppressWarnings("unused")
    				public void onError(int error) {   				
        				Toast.makeText(context, R.string.fail, Toast.LENGTH_SHORT).show();
        			}

        			public void onCancel() {   				
        				Toast.makeText(context, R.string.back, Toast.LENGTH_SHORT).show();
        			}

        			public void onException(String arg0) {
        				Toast.makeText(context, arg0, Toast.LENGTH_SHORT).show();
        			}
        		});
        	}catch (Exception e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
    		
    	}
    	    	
    }
    
    //Upload files to PCS
    public void upload(final Context context){
    	
    	if(null != PCSDemoInfo.access_token){
    		Thread workThread = new Thread(new Runnable(){				
    			public void run() {
									
		    		BaiduPCSAPI api = new BaiduPCSAPI();
		    		
		    		//Set access_token for rest api
		    		api.setAccessToken(PCSDemoInfo.access_token);
		    		
		    	    //Use pcs uploadFile API to uplaod files
					final PCSActionInfo.PCSFileInfoResponse uploadResponse = api.uploadFile(PCSDemoInfo.sourceFile, PCSDemoInfo.bdRootPath+PCSDemoInfo.fileTitle+".txt", new BaiduPCSStatusListener(){
						@Override
						public void onProgress(long bytes, long total) {
							// TODO Auto-generated method stub					
						}
		    		});
		    		
					//The interface of the thread UI
					PCSDemoInfo.uiThreadHandler.post(new Runnable(){						
		    			
						public void run(){
		  
		    				if(uploadResponse.error_code == 0){
		    					
		    					Toast.makeText(context,"上传成功", Toast.LENGTH_SHORT).show();		    					
		    					//Delete temp file
		    					File file = new File(PCSDemoInfo.sourceFile);
		    					file.delete();
		    					
	    					    //Back to the content activity
		    					back(context);		    					
		    				}else{		    					
		    					Toast.makeText(context,"错误代码："+uploadResponse.error_code, Toast.LENGTH_SHORT).show(); 
		    				}		    				
		    			}
		    		});	
		    		
				}
			});
			 
    		workThread.start();
    	}
    }
    
  
    //This function to display the list of contents
    public void list(final Context context){
    	
        if (null != PCSDemoInfo.access_token){
        	        	
    		Thread workThread = new Thread(new Runnable(){
    			
				public void run() {
					
		    		BaiduPCSAPI api = new BaiduPCSAPI();
		    		api.setAccessToken(PCSDemoInfo.access_token );
		    		
		    		//The path to  file storage on the cloud
		    		String path = PCSDemoInfo.bdRootPath;
		    		
		    		//Use list api
		    		final PCSActionInfo.PCSListInfoResponse listResponse = api.list(path, "time", "desc");
		    				    		
		    		PCSDemoInfo.uiThreadHandler.post(new Runnable(){
		    			
		    			public void run(){		    				
		    			
		    				ArrayList<HashMap<String, String>> list =new ArrayList<HashMap<String,String>>();   
		    						    				

		    				if( ! listResponse.list.isEmpty()){
		    					   			    	            
			    	            for(Iterator<PCSFileInfoResponse> i = listResponse.list.iterator(); i.hasNext();){
			    	            	
			    	            	HashMap<String, String> map =new HashMap<String, String>();
			    	            				    	            	
			    	            	PCSFileInfoResponse info = i.next();
			    	            	
			    	            	//Get the file name 			    	            	
			    	         	    String path = info.path;			    	         	    
			    	         	    String fileName = path.substring(PCSDemoInfo.bdRootPath.length(),path.lastIndexOf("."));
			    	         	    			    	         	   			    	         	    
			    	         	    //Get the last modified time
			    	         	    Date date = new Date(info.mTime*1000);
			    	         	    			    	         	    			    	         	    			    	         	 			    	         	    			    	         	    
			    	         	    //Modify the format of the time
			    	         	    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
			    	         	    String dateString = formatter.format(date);
		  			 			    			    	            	
			    	            	map.put("file_name", fileName);			    	            	
			    	            	map.put("time", dateString);
			    	            	
			    	            	//Add item to content list	
			    	            	list.add(map); 	            	
			    	            	PCSDemoInfo.fileNameList.add(fileName);							    				    	             
			    	            }			    	               
			    	        }else{			    	        	
			    	        	//Clear content list
		    					list.clear();
		    					Toast.makeText(context, "您的文件夹为空！", Toast.LENGTH_SHORT).show();		    					
		    				}    
		    				
			    	         SimpleAdapter listAdapter =new SimpleAdapter(context, list, R.layout.content, new String[]{"file_name","time"}, new int[]{R.id.file_name,R.id.time});   			    	        
			    	         //Set listview to display content
			    	         ((ListActivity)context).setListAdapter(listAdapter);
		    	         
			    	         Toast.makeText(context, R.string.refresh, Toast.LENGTH_SHORT).show();
		    			}
		    		});	
		    		
				}
			});
			 
    		workThread.start();

        } 
    }
       
    //Delete the file on the pcs
    public void delete(final Context context){
    	
    	if(null != PCSDemoInfo.access_token){

    		Thread workThread = new Thread(new Runnable(){
				public void run() {

		    		BaiduPCSAPI api = new BaiduPCSAPI();
		    		//Set access_token
		    		api.setAccessToken(PCSDemoInfo.access_token);
		    		
		    		List<String> files = new ArrayList<String>();
		    		files.add(PCSDemoInfo.bdRootPath + PCSDemoInfo.fileTitle + ".txt");
		    		
		    		//Call delete api
		    		final PCSActionInfo.PCSSimplefiedResponse deleteResponse = api.deleteFiles(files);
		    		
		    		PCSDemoInfo.uiThreadHandler.post(new Runnable(){
		    			public void run(){
		    				if(0 == deleteResponse.error_code){
		    							    							
		    					if(PCSDemoInfo.status == 2){
		    						//First remove the clouds files, and then refresh content list
		    						Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show();
		    						
		    						list(context);
		    						
		    					}else{
		    						//First remove the clouds files,and then upload the file 
		    						if(PCSDemoInfo.status == 1){		    							
		    							upload(context);
		    						} 						
		    					}		    					
		    				}else{
		    					Toast.makeText(context, "删除失败！"+deleteResponse.message, Toast.LENGTH_SHORT).show();
		    				}
		    			}
		    		});	
				}
			});
			 
    		workThread.start();
    	}
    }
    
    public void save(Context context) {
    	
    	try{
    		PCSDemoInfo.sourceFile = context.getFilesDir()+"/"+PCSDemoInfo.fileTitle+".txt";
       		
       	    String saveFile = PCSDemoInfo.fileTitle+".txt";
       			        			 	 
       	    FileOutputStream outputStream= context.openFileOutput(saveFile, Context.MODE_PRIVATE);
       	 
       	    if(!PCSDemoInfo.fileContent.equals("")){
       		    //save file
           	    outputStream.write(PCSDemoInfo.fileContent.getBytes());    					           	           	 
       	    }else{
	       		byte bytes = 0;
	       		outputStream.write(bytes);
       	    }
       	          	 
       	    outputStream.close();
       	    
       	    if(PCSDemoInfo.status == 0){
       	    	//Upload the file to cloud 
       	    	upload(context);       	    	
       	    }else{
       	    	//If it is edited file save, the first remove the clouds existing file, and then upload
       	    	if(PCSDemoInfo.status == 1){       	    		
       	    		delete(context);       	    		
       	    	}
       	    }  	                 		       	 		    
       	  }catch (Exception e) {   
               Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();  
          }    	    		 
    }
    
    //Back to the content activity
    public void back(Context context){    	  		
    	Intent content = new Intent();
  	    content.setClass(context, ContentActivity.class);	
  	    context.startActivity(content);  	
    }
 
    //Finish the program
    public void  exit(final Context context){
    	
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(context);
        exitAlert.setIcon(R.drawable.alert_dark).setTitle("提示...").setMessage("你确定要离开客户端吗？");
        exitAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {               
               public void onClick(DialogInterface dialog, int which) {
                    	PCSDemoInfo.flag= 1;
                        Intent intent = new Intent(); 
                        intent.setClass(context, PCSDemoNoteActivity.class);//jump to PCSDemoNoteActivity
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
                        context.startActivity(intent);
                    }
                });
        
        exitAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             
                public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        
        exitAlert.show();
    }
    		
}
