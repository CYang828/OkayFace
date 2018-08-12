package com.okay.face;

import android.Manifest;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.okay.face.activity.OkayFaceGrayActivity;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_HANDLE_CAMERA_PERM_RGB = 1;
    private static final int RC_HANDLE_CAMERA_PERM_GRAY = 2;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        Button btnCameraRGB = (Button) findViewById(R.id.btnRGB);
//        btnCameraRGB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
//                if (rc == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(mContext, FaceDetectRGBActivity.class);
//                    startActivity(intent);
//                } else {
//                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_RGB);
//                }
//            }
//        });

        Button btnCameraGray = (Button) findViewById(R.id.btnGray);
        btnCameraGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, OkayFaceGrayActivity.class);
                    startActivity(intent);
                } else {
                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_GRAY);
                }
            }
        });

//        Button btnPhoto = (Button) findViewById(R.id.btnImage);
//        btnPhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, PhotoDetectActivity.class);
//                startActivity(intent);
//            }
//        });
    }


    private void requestCameraPermission(final int RC_HANDLE_CAMERA_PERM) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == RC_HANDLE_CAMERA_PERM_RGB) {
//            Intent intent = new Intent(mContext, FaceDetectRGBActivity.class);
//            startActivity(intent);
//            return;
//        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == RC_HANDLE_CAMERA_PERM_GRAY) {
            Intent intent = new Intent(mContext, OkayFaceGrayActivity.class);
            startActivity(intent);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

}
