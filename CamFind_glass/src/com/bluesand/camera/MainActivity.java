package com.bluesand.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


public class MainActivity extends Activity {

    private static final String TAG = "camera";

    private SurfaceView mPreview;
    private Camera mCamera;

    private final PictureCallback mPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Picture taken!");
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            
            mCamera.release();

            String result = "";

            try {
                HttpResponse<JsonNode> request = Unirest
                        .post("https://camfind.p.mashape.com/image_requests")
                        .header("X-Mashape-Key",
                                "xxxxxxxxxxxxxxxxxxxxxxx")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .field("image_request[locale]", "en_US")
                        .field("image_request[image]", pictureFile)
                        .asJson();
            } catch (UnirestException e) {
                Log.d(TAG, "Request error");
            }
        
            String req_body =  request.getBody().toString();
                
            String token = "";

            try {
                JSONObject json = new JSONObject(req_body);
                token = json.getString("token");
            } catch (JSONException e) {
                Log.d(TAG, "JSON exceptions");
            }
                
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted Exception");
            }

            String res_path = "https://camfind.p.mashape.com/image_responses/" + token;
            
            try {
                HttpResponse<JsonNode> response = Unirest
                        .get(res_path)
                        .header("X-Mashape-Key",
                                "xxxxxxxxxxxxxxxxxxxxxxx")
                        .asJson();           
            } catch (UnirestException e) {
                Log.d(TAG, "Response error");
            }

            result =  response.getBody().toString();     

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    };

    /** Create a File for saving an image */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_DCIM), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        String path = "/storage/emulated/0/DCIM/IMG_"+ timeStamp + ".jpg";
        mediaFile = new File(path);
        Log.d(TAG, "PATH: " + path);

        return mediaFile;
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			Log.d(TAG, "DPAD_CENTER");
			mCamera.takePicture(null, null, mPictureCallback);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                // mCamera.takePicture(null, null, mPictureCallback);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Nothing to do here.
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Nothing to do here.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPreview = (SurfaceView) findViewById(R.id.preview);
        mPreview.getHolder().addCallback(mSurfaceHolderCallback);

        mCamera = getCameraInstance();
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
            // Work around for Camera preview issues.
            Camera.Parameters params = camera.getParameters();
            params.setPreviewFpsRange(30000, 30000);
            camera.setParameters(params);
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }
}
