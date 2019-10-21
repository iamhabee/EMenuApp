package com.arke.sdk.view;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.arke.sdk.R;

/**
 * Created by zhengf on 2017/12/20.
 */




public class VideoPlayerActivity extends BaseActivity {

    private static final String TAG = "VideoPlayerActivity";

    private VideoView vvView;
    private MediaController mcController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String filename = intent.getStringExtra("videofilename");

        //String path = SDCARD_PATH + FILE_NAME;
        // String path =  Environment.getExternalStorageDirectory().getPath()+"/"+ filename;
        // Log.d(TAG, path);
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(filename);
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
        String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // 视频旋转方向
        Log.d(TAG, height + width + rotation);

        if("0".equals(rotation)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
            Log.d(TAG, rotation);
        }

        setContentView(R.layout.activity_video_player);

        vvView = (VideoView) findViewById(R.id.videoView);


/*       try {
            copyAssetsFileToSdcard(FILE_NAME);
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(e.getMessage());
        }*/
        // String path =  Environment.getExternalStorageDirectory().getPath()+"/ECR-A8.mp4";

        vvView.setVideoPath(filename);

        mcController = new MediaController(this);

        vvView.setMediaController(mcController);

        mcController.setMediaPlayer(vvView);

        vvView.start();

        vvView.requestFocus();
    }

            /**
             * Copy assets file.
             */
/*        private void copyAssetsFileToSdcard (String fileName) throws RemoteException {
            int byteRead;
            ;
            InputStream input = null;

            String dstPath = SDCARD_PATH + fileName;
            try {
                input = this.getResources().getAssets().open(FILE_NAME);
                FileOutputStream fs = new FileOutputStream(dstPath);
                byte[] buffer = new byte[input.available()];
                Log.d(TAG, "Copy assets file" + input.available());
                while ((byteRead = input.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                fs.flush();//刷新缓冲区
                input.close();
                fs.close();
            } catch (IOException e) {
                throw new RemoteException(e.getMessage());
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        showToast(e.getLocalizedMessage());
                    }
                }
            }
        }*/
}