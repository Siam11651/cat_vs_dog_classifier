package com.siam11651.cat_vs_dog_classifier;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

public class DetectorCameraStateCallback extends CameraDevice.StateCallback
{
    final Surface surface;
    boolean surfaceAttached;

    public DetectorCameraStateCallback(Surface surface)
    {
        this.surface = surface;
        surfaceAttached = false;
    }

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice)
    {
        if(surfaceAttached)
        {
            return;
        }

        OutputConfiguration outputConfiguration = new OutputConfiguration(surface);
        ArrayList<OutputConfiguration> outputs = new ArrayList<>(Collections.singletonList(outputConfiguration));
        SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputs, Runnable::run, new DetectorCaptureSessionCallback(surface));

        try
        {
            cameraDevice.createCaptureSession(sessionConfiguration);
        }
        catch(CameraAccessException e)
        {
            throw new RuntimeException(e);
        }

        surfaceAttached = true;
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice)
    {

    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int i)
    {

    }
}
