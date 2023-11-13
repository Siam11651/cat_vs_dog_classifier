package com.siam11651.cat_vs_dog_classifier;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import androidx.annotation.NonNull;

public class DetectorCaptureSessionCallback extends CameraCaptureSession.StateCallback
{
    final Surface surface;

    public DetectorCaptureSessionCallback(Surface surface)
    {
        this.surface = surface;
    }

    @Override
    public void onConfigured(@NonNull CameraCaptureSession session)
    {
        CaptureRequest.Builder captureRequestBuilder;

        try
        {
            captureRequestBuilder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        }
        catch(CameraAccessException e)
        {
            throw new RuntimeException(e);
        }

        captureRequestBuilder.addTarget(surface);

        try
        {
            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        }
        catch(CameraAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session)
    {

    }
}
