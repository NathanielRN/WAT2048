package uwaterloo.ca.lab4_205_03;

/**
 * This is a Finite State Machine designed to detect signatures in signal pulses, its primary method,
 * supplyReading, is called by the accelerometer SensorHandler, many times per second.
 */

class GestureFSM {

    // Enum that creates a type so we can have the different states for our gesture recognizer
    private enum FSMStates { WAIT, FALL_B, RISE_A, STABLE_B, STABLE_A, DETERMINED}

    private FSMStates myState;

    // Signature Constants
    enum mySig {negativeDirection, positiveDirection, UNDETERMINED}
    private mySig mySignature;

    //Series of Constants
    private final float[] THRESHOLD_A = {2.0f, 0.4f}; // UP & RIGHT (RISING) The first element is the magnitude of the peak that the gesture must pass to be determined as a gesture
    private final float[] THRESHOLD_B = {-2.0f, -0.4f}; // DOWN & LEFT (FALLING) The second is the slope of the change that must occur to begin detecting a possible gesture

    //Tracking my own history reading so we can calculate slope
    private float previousReading; // Used to calculate the slope for max determination

    //Constructor
    GestureFSM() {
        myState = FSMStates.WAIT;
        mySignature = mySig.UNDETERMINED;
        previousReading = 0.0f;
    }

    //Reset to a default state
    private void resetFSM() {
        this.myState = FSMStates.WAIT;
        this.mySignature = mySig.UNDETERMINED;
        previousReading = 0.0f;
    }

    //When the accelerometerSensor handler receives a reading the sensor, it calls the finite state machine to recognize signatures.
    boolean supplyReading(float input) {
        //First, calculate the slope
        float slope = input - previousReading;

        //Begin FSM switch through state code
        switch (myState){
            // The wait state is the base state for the FSM, if a no significant change occurs (slope < slope threshold, the state machine doesnt change
            case WAIT:
                if (slope > THRESHOLD_A[1]) {
                    // if a change occurs whose slope passes the threshold and is positive, the FSM goes into the Rise A state.
                    myState = FSMStates.RISE_A;
                } else if(slope < THRESHOLD_B[1]) {
                    // Similarly if the slope is negative and exceeded the threshold the FSM goes into Fall B state
                    myState = FSMStates.FALL_B;
                }
                break;
            //The fall B state waits until the slope of the signal crosses over zero, since the slope is already negative
            //the fall B state listens for a slope which is postive or equal to zero, once the slope is positive
            //or equal to zero, that means we have reached the local min.
            case FALL_B:
                //slope crossing 0 into positive values
                //..MINIMUM VALUE APPEARED
                //Note that at the current time, the slope is now just barely positive or close to zero, since this is the first positive slope
                //since we went negative, that means that the current value of input is very close to if not actually at the
                //minimum value of the signal, for this reason we use input as the magnitude of the pulse and do our comparisons with that.
                if (slope >= 0) { //Oh look we hit the min point.
                    if (input < THRESHOLD_B[0]) { //comparing magnitude of the signal to the magnitude threshold, was the gesture hard enough?
                        myState = FSMStates.STABLE_B; // yes it was hard enough
                    } else {
                        // if the amplitude is never reaching the threshold...
                        myState = FSMStates.DETERMINED; // no it was not hard enough
                        mySignature = mySig.UNDETERMINED;
                        // probably set your maximum amplitude too high
                    }
                }
                break;
            //Same idea as the state Fall_B but with positives and negatives flipped around, the value of input is still the magnitude
            //and it will be compared to the threshold as in Fall_B
            case RISE_A:
                //slope crossing 0 into negative values
                //..MAXIMUM VALUE APPEARED
                if (slope <= 0) { //O look we hit the max point
                    if (input > THRESHOLD_A[0]) { // was the gesture hard enough?
                        myState = FSMStates.STABLE_A; // yes it was hard enough
                    } else {
                        // if the amplitude is never reaching the threshold...
                        myState = FSMStates.DETERMINED; // no it was not hard enough.
                        mySignature = mySig.UNDETERMINED;
                        // probably set your maximum amplitude too high
                    }
                }
                break;
            case STABLE_A:
                // Time to return a determinate result. In this case Mysig.positiveDirection,
                // which translates to right or up depending on the which axis the FSM is being used for.
                myState = FSMStates.DETERMINED;
                mySignature = mySig.positiveDirection;
                return true; //exit
            case STABLE_B:
                // Time to return a determinate result. In this case Mysig.negativeDirection,
                // which translates to left or down depending on the which axis the FSM is being used for.
                myState = FSMStates.DETERMINED;
                mySignature = mySig.negativeDirection;
                return true;

            case DETERMINED:
                //Yay! determined result, we Log.d for debugging purposes and reset the FSM to its base state
                // (including setting the state to the WAIT state
                resetFSM();
                return false;
            default:
                //if by some supernatural tomfoolery we end up in a state that is not one of the regular states, return to WAIT
                resetFSM();
                break;
        }
        // Replace the history record with the new input
        previousReading = input;
        return false;
    }
    //simple function to acquire the signature of a GestureFSM at any given time in the code.
    mySig getFSMSignature(){
        return this.mySignature;
    }
}