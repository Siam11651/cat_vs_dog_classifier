package com.siam11651.cat_vs_dog_classifier;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.TextView;

import org.tensorflow.lite.InterpreterApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Detector extends Thread
{
    private static Detector singleton;
    final private TextView detectionResult;
    final private TextView confidence;
    final private Activity activity;
    final private SurfaceHolder surfaceHolder;
    final private InterpreterApi interpreter;
    final private ReentrantLock lock;

    private Detector(Activity activity, TextView detectionResult, TextView confidence, SurfaceHolder surfaceHolder)
    {
        this.activity = activity;
        this.detectionResult = detectionResult;
        this.confidence = confidence;
        this.surfaceHolder = surfaceHolder;

        File tempModelFile = new File(activity.getFilesDir(), "model.tflite");

        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(tempModelFile);
            InputStream modelInputStream = activity.getResources().openRawResource(R.raw.model);

            while(modelInputStream.available() > 0)
            {
                byte[] byteArray = new byte[modelInputStream.available()];

                modelInputStream.read(byteArray);
                fileOutputStream.write(byteArray);
            }

            modelInputStream.close();
            fileOutputStream.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        interpreter = InterpreterApi.create(tempModelFile, new InterpreterApi.Options());
        lock = new ReentrantLock();
    }

    public static void initialize(Activity activity, TextView detectionResult, TextView confidence, SurfaceHolder surfaceHolder)
    {
        if(singleton != null)
        {
            return;
        }

        singleton = new Detector(activity, detectionResult, confidence, surfaceHolder);
    }

    public static Detector getDetector()
    {
        return singleton;
    }

    public void setImage()
    {
        if(!lock.tryLock())
        {
            return;
        }

        lock.unlock();
    }

    @Override
    public void run()
    {
        AtomicBoolean inferring = new AtomicBoolean(false);
        Surface surface = surfaceHolder.getSurface();

        while(true)
        {
            if(inferring.get())
            {
                return;
            }
            else
            {
                inferring.set(true);
            }

            Semaphore semaphore = new Semaphore(0);
            AtomicReference<Looper> looper = new AtomicReference<>();

            new Thread(() ->
            {
                Looper.prepare();
                looper.set(Looper.myLooper());
                semaphore.release();
                Looper.loop();
            }).start();

            try
            {
                semaphore.acquire();
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);

            PixelCopy.request(surface, bitmap, (int copyResult) ->
            {
                float[][][] input = new float[bitmap.getWidth()][bitmap.getHeight()][3];
                float[][] output = new float[1][1];

                for(int i = 0; i < bitmap.getHeight(); ++i)
                {
                    for(int j = 0; j < bitmap.getWidth(); ++j)
                    {
                        int color = bitmap.getPixel(i, j);
                        float r = ((color >> 16) & 255) / 255.0f;
                        float g = ((color >> 8) & 255) / 255.0f;
                        float b = (color & 255) / 255.0f;
                        input[j][i][0] = r;
                        input[j][i][1] = g;
                        input[j][i][2] = b;
                    }
                }

                interpreter.run(new float[][][][]{input}, output);

                activity.runOnUiThread(() ->
                {
                    if(output[0][0] <= 0.5f)
                    {
                        detectionResult.setText(R.string.cat);
                        confidence.setText(activity.getString(R.string.confidence, (int)((1f - output[0][0]) * 100f)));
                    }
                    else
                    {
                        detectionResult.setText(R.string.dog);
                        confidence.setText(activity.getString(R.string.confidence, (int)(output[0][0] * 100f)));
                    }
                });

                looper.get().quit();
                inferring.set(false);
                semaphore.release();
            }, new Handler(Objects.requireNonNull(looper.get())));

            try
            {
                semaphore.acquire();
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
