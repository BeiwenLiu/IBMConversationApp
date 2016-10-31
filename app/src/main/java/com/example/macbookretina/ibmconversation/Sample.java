package com.example.macbookretina.ibmconversation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Bundle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class Sample extends Activity {
    public static final String TAG = "TrulyHandsfreeSDK";
    private String items[] = { " - BuildList", " - BuildGrammar", " - BuildIncremental",
			       " - RecogList", " - RecogGrammar", " - Phrasespot", " - RecogPipe", 
			       " - RecogSeq", " - SpeakerVerification", " - SpeakerIdentification", 
			       " - UDT / SID", " - RecogEnroll" };
    protected static Sample app = null;
    private ListView list = null;
    private ArrayAdapter<String> adapter = null;
    public static int sample = 0;
    protected static Record audioInstance = null;
    protected static Thread athread = null;
    protected static Context context = null;
    public static String appDirFull = null;

    // Copy resource data files to local storage
    void copyData(int res, String filename) {
        InputStream ins;
        int size;
        byte[] buffer;
        try {
            ins = getResources().openRawResource(res);
            size = ins.available();
            buffer = new byte[size];
            ins.read(buffer);
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to open resource");
            return;
        }
        if (!writeStorage(filename, buffer)) {
            Log.e(TAG, "COPYDATA FAILED: file=" + filename);
        } else {
            Log.i(TAG, "COPIED DATA: file=" + filename);
        }
    }

    public static Boolean writeStorage(String filename, byte[] content) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "writeStorage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        appDirFull = getFilesDir().toString() + "/";
        // We're not taking full advantage of the AssetCache class at this
        // point,
        // these files could be copied on-demand instead.
        try {
            for (String file : new String[] { "nn_en_us_mfcc_16k_15_250_v5.1.1.raw",
                    "lts_en_us_9.5.2b.raw", "names.txt", "john_smith_16khz.wav",
                    "twelve_oclock_16khz.audio", "hbg_antiData_v6_0.raw", "hbg_genderModel.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_am.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_3.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_5.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_7.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_9.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_11.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_13.raw",
                    "sensory_demo_hbg_en_us_sfs_delivery04_pruned_search_15.raw",
		    "phonemeSearch_1_5.raw", "svsid_1_1.raw",
                    "HBG_enUS_taskData_v2_2.raw", "UDT_enUS_taskData_v12_0_5.raw"
		}) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sample = position;
                Toast.makeText(getApplicationContext(), ((TextView) view).getText() + " - ",
                        Toast.LENGTH_SHORT).show();
                Intent myIntent = new Intent(view.getContext(), Console.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Start Audio thread
        audioInstance = new Record();
        athread = new Thread(audioInstance);
        athread.start();
    }
}
