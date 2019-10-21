package com.arke.sdk.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.arke.sdk.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengf on 2017/12/21.
 */

public class VideoListActivity extends BaseActivity implements AdapterView.OnItemClickListener {


    ListView lv_video ;
    private Cursor cursor;
    private List<Map<String, Object>> videolist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_layout);

        lv_video = (ListView) findViewById(R.id.lv_video);
        videolist = new ArrayList<Map<String, Object>>();

    /*    String[] mediaColumns = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DISPLAY_NAME };*/


        cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);

/*        if(cursor==null){
          //  Toast.makeText(this, R.string.No_video_files, Toast.LENGTH_LONG).show();
         //   return;
            Log.d("zf", "cursor is null");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("displayname", R.string.No_video_files);
            videolist.add(map);

        }*/


        if (cursor.moveToFirst()) {
            do {

                String video_title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("displayname", video_title);
                map.put("filepath", filePath);
                videolist.add(map);


            } while (cursor.moveToNext());
        }else{
            Toast.makeText(this, R.string.No_video_files, Toast.LENGTH_LONG).show();
        }



        SimpleAdapter simple_adapter = new SimpleAdapter(this, videolist,
                R.layout.video_list_item, new String[] { "displayname","filepath" },
                new int[] {R.id.videoName,R.id.pathname});

        lv_video.setAdapter(simple_adapter);
        lv_video.setOnItemClickListener(this);
/*        lv_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("testvideo", "zenmele" + position);
               // String videofilename = lv_video.getItemAtPosition(position);
                TextView textView = (TextView)view.findViewById(R.id.videoName);
                String videofilename = (String) textView.getText();
                Intent intent = new Intent(this,VideoPlayerActivity.class);
                intent.putExtra("filename", videofilename);
                startActivity(intent);

                Log.d("testvideo", videofilename);
            }
        });*/

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView = (TextView)view.findViewById(R.id.pathname);
                String videofilename = (String) textView.getText();
                Intent intent = new Intent(this,VideoPlayerActivity.class);
                intent.putExtra("videofilename", videofilename);

                startActivity(intent);
    }
}
