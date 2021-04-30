package com.blautic.pikkuacademyfull;

import android.content.Context;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;

import timber.log.Timber;

public class MovementDetector {
    public interface MovementListener {
        void onStep(int steps, float distance);

        void onJump(int jumps);

        void onStand();

        void onRest();
    }

    private boolean isMoving;
    private boolean isRest;

    private boolean isOverThreshold = false;
    private long lastTimeMovementDetected = System.currentTimeMillis();
    private final MovementListener movementListener;
    private final float thresholdStep;
    private  float thresholdTime = 300f;
    private final float thresholdJump;
    private final long timeBeforeDeclaringStationary;
    private final long timeBeforeDeclaringRest;
    private float EWMA = 0.1f;
    private int steps = 0;
    private int jumps = 0;
    private final float averageStepDistance = 0.70f;
    private final float[] gravity = new float[]{1, 1, 1};
    private final float[] linear_acceleration = new float[3];
    private int calibratedCountData = 0;
    private String csvData = "";
    private static final int TIME_BEFORE_DECLARING_STATIONARY = 2000;
    private static final int TIME_BEFORE_DECLARING_REST= 2000;


    public MovementDetector(MovementListener movementListener) {
        this(0.15f, 0.5f, TIME_BEFORE_DECLARING_STATIONARY, TIME_BEFORE_DECLARING_REST, movementListener);
    }

    public MovementDetector(float thresholdStep, float thresholdJump, long timeBeforeDeclaringStationary,
                            long timeBeforeDeclaringRest, MovementListener movementListener) {
        this.movementListener = movementListener;
        this.thresholdStep = thresholdStep;
        this.thresholdJump = thresholdJump;
        this.timeBeforeDeclaringStationary = timeBeforeDeclaringStationary;
        this.timeBeforeDeclaringRest = timeBeforeDeclaringRest;
    }

    public void setDataAccelerometer(float x, float y, float z) {
        setLinearAcceleration(x, y, z);

        float accelCurrent = (float) Math.sqrt(Math.pow(linear_acceleration[0], 2)
                + Math.pow(linear_acceleration[1], 2) + Math.pow(linear_acceleration[2], 2));

        float beta = 0.2f;
        EWMA = (1 - beta) * EWMA + beta * accelCurrent;

        if (calibratedCountData < 20) {
            calibratedCountData++;
            return;
        }

        float delta = accelCurrent - EWMA;
        String data = accelCurrent + "," + EWMA + "," + delta +" \n";
        Timber.d(data);
        csvData = csvData + x +","+ y +","+ z + ","+ accelCurrent + ","+ EWMA+ " \n";
       // csvData = csvData + accelCurrent + "," + EWMA + "," + delta +" \n";
        if (delta > thresholdStep && !isOverThreshold && (System.currentTimeMillis() - lastTimeMovementDetected) > thresholdTime) {
            isOverThreshold = true;
            lastTimeMovementDetected = System.currentTimeMillis();
            isMoving = true;
            if (delta > thresholdJump) {
                thresholdTime = 1000f;
                jumps++;
                movementListener.onJump(jumps);
            } else {
                thresholdTime = 300f;
                steps++;
                movementListener.onStep(steps, steps * averageStepDistance);
            }
        } else if(accelCurrent < EWMA){
            isOverThreshold = false;
            long timeDelta = (System.currentTimeMillis() - lastTimeMovementDetected);
            if (timeDelta > timeBeforeDeclaringStationary && !isRest) {
                isMoving = false;
                movementListener.onStand();

            }
        }
    }

    public void setDataAngles(float xy, float zy, float xz) {
        if (Math.abs(xy) > 30 || Math.abs(zy) > 30 ) {
            long timeDelta = (System.currentTimeMillis() - lastTimeMovementDetected);
            if (timeDelta > timeBeforeDeclaringRest) {
                isRest = true;
                movementListener.onRest();
            }
        } else {
          //  movementListener.onStand();
            isRest = false;
        }
    }

    private void setLinearAcceleration(float x, float y, float z) {
        final float alpha = 0.8f;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z;
        linear_acceleration[0] = x - gravity[0];
        linear_acceleration[1] = y - gravity[1];
        linear_acceleration[2] = z - gravity[2];
    }

    public void writeDataToFile(Context context) {

        File dir = new File(context.getFilesDir(), "data");
        if (!dir.exists()) {
            dir.mkdir();
        }
        String name = "data-" + System.currentTimeMillis() + ".cvs";
        try {
            File gpxfile = new File(dir, name);

            FileWriter writer = new FileWriter(gpxfile);
            writer.append(csvData);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
