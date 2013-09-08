package com.sunyata.kindmind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import com.sunyata.kindmind.BuildConfig;
import android.content.Context;
import android.util.Log;

public class JsonSerializerM {

	private Context mContext;
	private String mFilename;
	
	JsonSerializerM(Context inContext, String inFileName){
		mContext = inContext;
		mFilename = inFileName;
	}
	
	ArrayList<ListDataItemM> loadData() throws JSONException, IOException {
		ArrayList<ListDataItemM> retDataList = new ArrayList<ListDataItemM>();
		BufferedReader tmpBufferedReader = null;
		try{
			//Establishing an input stream to be read from by opening the file
			InputStream tmpInputStream = mContext.openFileInput(mFilename);

			tmpBufferedReader = new BufferedReader(new InputStreamReader(tmpInputStream));
			StringBuilder tmpJsonString = new StringBuilder();
			String line = null;
			while((line = tmpBufferedReader.readLine()) != null){
				tmpJsonString.append(line);
			}

			JSONArray tmpJsonArray = (JSONArray) new JSONTokener(tmpJsonString.toString()).nextValue();
			//-only the first value
			for (int i = 0; i < tmpJsonArray.length(); i++){
				retDataList.add(new ListDataItemM(tmpJsonArray.getJSONObject(i)));
			}
		}catch(FileNotFoundException e){
			//Every time we start fresh, we will get FileNotFoundException (since it assumes we have the file already)
		}finally{
			if(tmpBufferedReader != null){
				tmpBufferedReader.close();
			}
		}
		return retDataList;
	}
	
	void saveData(ArrayList<ListDataItemM> inListData, boolean inSaveCurrentList)
			throws JSONException, IOException{
		JSONArray tmpJsonArray = new JSONArray();
		for (ListDataItemM ld : inListData){
			/* Check removed since we now use id instead of name (so there could be multiple datalistitem with
			 * the same name (in this case no name)
			if(ld.mName == ListDataItemM.NO_NAME_SET){
				continue;
			}
			*/
			Object addJsonObject = null;
			try {
				addJsonObject = ld.toJson(inSaveCurrentList);
			} catch (JSONException e) {
				Log.e(Utils.getClassName(), "JSONException in method saveData: " + e.getMessage());
				if(BuildConfig.DEBUG){e.printStackTrace();}//[Refactor]
			}
			tmpJsonArray.put(addJsonObject);
		}

		Writer tmpWriter = null;
		try{
			if(inSaveCurrentList){
				//Removing previous version of the file
				File tmpFile = new File(mFilename);
				tmpFile.delete();
			}
			
			OutputStream tmpOutputStream = mContext.openFileOutput(mFilename, Context.MODE_WORLD_READABLE);
			//-Private means that only this application will be able to write
			//-Refactor: Instead using a content provider
			tmpWriter = new OutputStreamWriter(tmpOutputStream);
			tmpWriter.write(tmpJsonArray.toString());
		}finally{
			if(tmpWriter != null){
				tmpWriter.close();
			}
		}
	}
}
