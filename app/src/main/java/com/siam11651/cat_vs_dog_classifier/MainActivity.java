package com.siam11651.cat_vs_dog_classifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity
{
    private SurfaceHolder surfaceHolder;

    @SuppressLint("MissingPermission")
    public void openCamera()
    {
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try
        {
            cameraManager.openCamera("0", new DetectorCameraStateCallback(surfaceHolder.getSurface()), null);
        }
        catch(CameraAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = findViewById(R.id.surface_camera_view);
        surfaceHolder = surfaceView.getHolder();

        Detector.initialize(this, findViewById(R.id.text_detection), findViewById(R.id.text_confidence), surfaceHolder);
        surfaceHolder.addCallback(new DetectorSurfaceCallback(this));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {
                // show error
            }
        }
    }
}