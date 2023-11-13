package com.siam11651.cat_vs_dog_classifier;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

public class DetectorSurfaceCallback implements SurfaceHolder.Callback
{
    final private MainActivity activity;

    public DetectorSurfaceCallback(MainActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder)
    {
        Detector.getDetector().start();
        String[] permissions = {Manifest.permission.CAMERA};

        if(activity.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            activity.requestPermissions(permissions, 0);
        }
        else
        {
            activity.openCamera();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder)
    {

    }
}
