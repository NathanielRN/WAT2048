package uwaterloo.ca.lab4_205_03;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

/**
 * Created by nathanielruiz98 on 2017-05-26. The Handler used for the accelerometer and its function in conjunction with its need to update the graph, change the labels, update a datasource,
 * and detect gestures
 */

class AccelerometerSensorHandler implements SensorEventListener {

    // Gesture direction Label
    private TextView directionLabel;

    // Create instance variables for separate x and y finite state machines
    private GestureFSM accessGestureFSMx;
    private GestureFSM accessGestureFSMy;

    // Create necessary signature variables from finite state machines which will be attached to x and y respectively
    private GestureFSM.mySig lastXSignature;
    private GestureFSM.mySig lastYSignature;

    private GameLoopTask gameLoopAccess;

    // Create a discrete valued data type for the type of movements that the state machine can detect
    private enum Directions {UP, DOWN, LEFT, RIGHT}

    // Constructor for initialization of the accelerometer sensor and access to the game loop timer
    AccelerometerSensorHandler(TextView accessToDirecitonLabel, GameLoopTask accessToGameLoop) {
        directionLabel = accessToDirecitonLabel;
        accessGestureFSMx = new GestureFSM();
        accessGestureFSMy = new GestureFSM();
        lastXSignature = GestureFSM.mySig.UNDETERMINED;
        lastYSignature = GestureFSM.mySig.UNDETERMINED;
        this.gameLoopAccess = accessToGameLoop;
    }

    public void onAccuracyChanged(Sensor s, int i) { /* Not important to us */ }

    public void onSensorChanged(SensorEvent sensorEvent) {
        // Function to smooth value, assign, and further manipulate/work with the raw value however our app may need before its stored
        smoothValueAndAssign(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
    }

    private void smoothValueAndAssign(float xRawReading, float yRawReading, float zRawReading) {

        // This should NOT be required, we should be able to just pass this into the 99th (newest) element
        float[][] filteredReadingsArray = new float[2][3];

        filteredReadingsArray[0] = filteredReadingsArray[1];
        // Apply a low pass filter for the new adjusted appropriate value
        double FILTER_CONSTANT = 9.0f;
        filteredReadingsArray[1][0] = (float)(filteredReadingsArray[0][0] + (xRawReading - filteredReadingsArray[0][0]) / FILTER_CONSTANT);
        filteredReadingsArray[1][1] = (float)(filteredReadingsArray[0][1] + (yRawReading - filteredReadingsArray[0][1]) / FILTER_CONSTANT);
        filteredReadingsArray[1][2] = (float)(filteredReadingsArray[0][2] + (zRawReading - filteredReadingsArray[0][2]) / FILTER_CONSTANT);

        // MARK: New filtered variable is ready to be used!

        // Our function that conforms to the finite state machine logic
        checkForValidGesture(filteredReadingsArray[1]);

    }

    private void checkForValidGesture(float receivedReadingValues[]) {

        // Get a boolean back to determine if the signature has been determined yet for x and y
        boolean signatureIsDeterminedX = accessGestureFSMx.supplyReading(receivedReadingValues[0]);
        boolean signatureIsDeterminedY = accessGestureFSMy.supplyReading(receivedReadingValues[1]);

        // Upon determining assign the X signature to our signature variable at the top
        if (signatureIsDeterminedX) {
            lastXSignature = accessGestureFSMx.getFSMSignature();
        }

        // Upon determining assign the Y signature to our signature variable at the top
        if (signatureIsDeterminedY) {
            lastYSignature = accessGestureFSMy.getFSMSignature();
        }

        // If one of the states is determined then we will check if we can conclusively say we got a gesture
        if (signatureIsDeterminedX || signatureIsDeterminedY) {
            // First we will check if the x direction is undetermined so as to ensure we are not determined for BOTH y and x implying diagonal movement
            if (lastXSignature == GestureFSM.mySig.UNDETERMINED) {
                // If from our last check we have determined that the y direction has signature positive or negative we will go into the appropriate block
                if (lastYSignature == GestureFSM.mySig.positiveDirection) {
                    // If we are not moving in the x and we are conclusively moving in the positive y then we will switch the label to the UP direction
                    directionLabel.setText(Directions.UP.toString());
                    // Through the game loop access, we set the direction we want to block to move to and the loop and the block handles the rest
                    gameLoopAccess.setDirection(GameLoopTask.BlockDirection.UP);
                } else if (lastYSignature == GestureFSM.mySig.negativeDirection) {
                    // Similarly, if we are not moving in the x and we are conclusively moving in the negative y then we will switch the label to the DOWN direction
                    directionLabel.setText(Directions.DOWN.toString());
                    // Through the game loop access, we set the direction we want to block to move to and the loop and the block handles the rest
                    gameLoopAccess.setDirection(GameLoopTask.BlockDirection.DOWN);
                } else {
                    // We have not conclusively determine if the y direction is ready! We will return because we can't say anything about both direction being undetermined
                    return;
                }
                // Once we determine a successful movement we reset our state machine in anticipation of the next movement
                lastXSignature = GestureFSM.mySig.UNDETERMINED;
                lastYSignature = GestureFSM.mySig.UNDETERMINED;
                // Alternatively we will check if the y direction is undetermined so as to ensure we are not determined for BOTH y and x implying diagonal movement
            } else if (lastYSignature == GestureFSM.mySig.UNDETERMINED) {
                // If from our last check we have determined that the x direction has signature positive or negative we will go into the appropriate block
                if (lastXSignature == GestureFSM.mySig.positiveDirection) {
                    // If we are not moving in the y and we are conclusively moving in the positive x then we will switch the label to the RIGHT direction
                    directionLabel.setText(Directions.RIGHT.toString());
                    // Through the game loop access, we set the direction we want to block to move to and the loop and the block handles the rest
                    gameLoopAccess.setDirection(GameLoopTask.BlockDirection.RIGHT);
                } else if (lastXSignature == GestureFSM.mySig.negativeDirection) {
                    // Similarly, if we are not moving in the y and we are conclusively moving in the negative x then we will switch the label to the LEFT direction
                    directionLabel.setText(Directions.LEFT.toString());
                    // Through the game loop access, we set the direction we want to block to move to and the loop and the block handles the rest
                    gameLoopAccess.setDirection(GameLoopTask.BlockDirection.LEFT);
                }  else {
                    // We have not conclusively determine if the x direction is ready! We will return because we can't say anything about both direction being undetermined
                    return;
                }
                // Once we determine a successful movement we reset our state machine in anticipation of the next movement
                lastXSignature = GestureFSM.mySig.UNDETERMINED;
                lastYSignature = GestureFSM.mySig.UNDETERMINED;
            } else {
                // They cannot be both determined!! This is an edge case so we reset our states and do not change the label
                lastXSignature = GestureFSM.mySig.UNDETERMINED;
                lastYSignature = GestureFSM.mySig.UNDETERMINED;
            }
        }
    }
}