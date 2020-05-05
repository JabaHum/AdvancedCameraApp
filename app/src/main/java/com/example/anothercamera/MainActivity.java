package com.example.anothercamera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE_REQUEST_B = 100;
    private static final String APP_TAG = MainActivity.class.getSimpleName();

    private ImageView mCameraImageView;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton;
    private Disposable disposable;

    private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startImageCapture();
        }
    };

    private OnClickListener mSaveImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            File saveFile = openFileForImage();
            if (saveFile != null) {
                saveImageToFile(saveFile);
            } else {
                Toast.makeText(MainActivity.this, "Unable to open file for saving image.",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions.setLogging(true);
        setContentView(R.layout.activity_main);

        mCameraImageView = (ImageView) findViewById(R.id.camera_image_view);


        disposable = RxView.clicks(this.<View>findViewById(R.id.capture_image_button))
                .compose(rxPermissions.ensureEach(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(new Consumer<Permission>() {

                               @Override
                               public void accept(Permission permission) throws Exception {
                                   Log.i(APP_TAG, "Permission result " + permission);
                                   if (permission.granted) {
                                       findViewById(R.id.capture_image_button).setOnClickListener(mCaptureImageButtonClickListener);

                                       mSaveImageButton = (Button) findViewById(R.id.save_image_button);
                                       mSaveImageButton.setOnClickListener(mSaveImageButtonClickListener);
                                       mSaveImageButton.setEnabled(false);
                                   } else if (permission.shouldShowRequestPermissionRationale) {
                                       // Denied permission without ask never again
                                       Toast.makeText(MainActivity.this,
                                               "Denied permission without ask never again",
                                               Toast.LENGTH_SHORT).show();
                                   } else {
                                       // Denied permission with ask never again
                                       // Need to go to the settings
                                       Toast.makeText(MainActivity.this,
                                               "Permission denied, can't enable the camera",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable t) {
                                   Log.e(APP_TAG, "onError", t);
                               }
                           },
                        new Action() {
                            @Override
                            public void run() {
                                Log.i(APP_TAG, "OnComplete");
                            }
                        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == RESULT_OK) {
                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap.recycle();
                    mCameraBitmap = null;
                }
                Bundle extras = data.getExtras();
                mCameraBitmap = (Bitmap) extras.get("data");
                byte[] cameraData = extras.getByteArray(CameraActivity.EXTRA_CAMERA_DATA);
                if (cameraData != null) {
                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);
                    mCameraImageView.setImageBitmap(mCameraBitmap);
                    mSaveImageButton.setEnabled(true);
                }
            } else {
                mCameraBitmap = null;
                mSaveImageButton.setEnabled(false);
            }
        }
    }

    private void startImageCapture() {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B);
        startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), TAKE_PICTURE_REQUEST_B);
    }

    private File openFileForImage() {
        File imageDirectory = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "com.oreillyschool.android2.camera");
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
                        Locale.getDefault());

                return new File(imageDirectory.getPath() +
                        File.separator + "image_" +
                        dateFormat.format(new Date()) + ".png");
            }
        }
        return null;
    }

    private void saveImageToFile(File file) {
        if (mCameraBitmap != null) {
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
                if (!mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                    Toast.makeText(MainActivity.this, "Unable to save image to file.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Saved image to: " + file.getPath(),
                            Toast.LENGTH_LONG).show();
                }
                outStream.close();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Unable to save image to file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}