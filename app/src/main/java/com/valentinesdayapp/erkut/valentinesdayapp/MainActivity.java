package com.valentinesdayapp.erkut.valentinesdayapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String AUDIO_FILE         = "sound/sound.mp3";
    private static final String IMAGE_FOLDER       = "image";
    private static final long  IMAGE_UPDATE_PERIOD = 4 * 1000;
    private static final String IMAGE_KEY          = "image_key";


    private Toolbar              mToolbar;
    private SquareImageView      mImageView;
    private FloatingActionButton mButton;
    private boolean              mIsPressed;
    private MediaPlayer          mPlayer;
    private String[]             mImageFileList;
    private int                  mImageFileListIndex;
    private UpdateImageThread    mThread;

    private Handler mUpdateImageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageFileList != null && mImageFileList.length > 0) {
                loadImageView(mImageFileList[mImageFileListIndex]);
                mImageFileListIndex = (mImageFileListIndex + 1) % mImageFileList.length;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsPressed = false;
        mToolbar   = (Toolbar) findViewById(R.id.main_toolbar);
        if(mToolbar != null) {
            mToolbar.setTitle(getString(R.string.app_name));
            setSupportActionBar(mToolbar);
        }
        mImageView = (SquareImageView) findViewById(R.id.main_image);
        mButton    = (FloatingActionButton) findViewById(R.id.main_actionbutton);
        mButton.setImageResource(R.drawable.ic_play_white);
        mButton.setOnClickListener(this);
        mPlayer    = new MediaPlayer();
        try {
            AssetFileDescriptor fds = getAssets().openFd(AUDIO_FILE);
            mPlayer.setDataSource(fds.getFileDescriptor(), fds.getStartOffset(), fds.getLength());
            mPlayer.setLooping(true);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageFileList      = getAssetImageList();
        mImageFileListIndex = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_heart:
                new AlertDialog.Builder(this)
                        .setTitle("Message")
                        .setMessage(getString(R.string.message))
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        if(mIsPressed) {
            mButton.setImageResource(R.drawable.ic_play_white);
            mPlayer.pause();
            mThread.interrupt();
        } else {
            mButton.setImageResource(R.drawable.ic_pause_white);
            mPlayer.start();
            mThread = new UpdateImageThread();
            mThread.start();
        }
        mIsPressed = !mIsPressed;
    }

    private class UpdateImageThread extends Thread {
        public void run() {
            while(true) {
                try {
                    mUpdateImageHandler.sendEmptyMessage(0);
                    this.sleep(IMAGE_UPDATE_PERIOD);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private String[] getAssetImageList() {
        String[] imageList = null;
        AssetManager assetManager = getAssets();
        try {
            imageList = assetManager.list(IMAGE_FOLDER);
            Log.d("MainActivity", "number of images= "+imageList.length);
            for(String image:imageList) {
                Log.d("MainActivity", "image name= "+image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageList;
    }

    private void loadImageView(String fileName) {
        try {
            InputStream ims = getAssets().open(IMAGE_FOLDER + "/" + fileName);
            Drawable d = Drawable.createFromStream(ims, null);
            mImageView.setImageDrawable(d);
            ims.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
