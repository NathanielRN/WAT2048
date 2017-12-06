package uwaterloo.ca.lab4_205_03;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;

public class Lab4_205_03 extends AppCompatActivity {

    // Access to screen layout
    RelativeLayout screenLayoutAccess;

    // Direction Label
    TextView gestureDirectionLabel;

    // Create SensorManager
    SensorManager sensorManagerAccess;

    // Declare sensors
    Sensor accelerometerSensor;

    // Declare Event Listeners
    SensorEventListener accelerometerHandler;

    //Declare Gameboard Size
    static final int GAMEBOARD_SIZE = ScalingConstantsClass.GAME_GRIDBOARD_SIDE_LENGTH;

    // Declare varaible for access to game loop
    GameLoopTask myGameLoopTask;

    //registerListener(listenToEvenet, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup Layout
        setContentView(R.layout.activity_lab4_205_03);
        screenLayoutAccess = (RelativeLayout) findViewById(uwaterloo.ca.lab4_205_03.R.id.customRelativeLayoutID);

        screenLayoutAccess.getLayoutParams().height = GAMEBOARD_SIZE;
        screenLayoutAccess.getLayoutParams().width = GAMEBOARD_SIZE;


        screenLayoutAccess.setBackgroundResource(uwaterloo.ca.lab4_205_03.R.drawable.gameboard);

        myGameLoopTask = new GameLoopTask(this,screenLayoutAccess,this.getApplicationContext());

        // Locate the IDs from xml
        locateTextViewReferences();

        // Get reference to system sensors
        obtainSensorReferences();

        // Establish different handler types
        configureHandlers();

        //Create instance of GameLoopTask

        Timer myGameLoopTimer = new Timer();
        myGameLoopTimer.schedule(myGameLoopTask,50,50);
    }

    //MARK: Make data persist functions - Found here http://stackoverflow.com/questions/151777/saving-android-activity-state-using-save-instance-state

    // MARK: Setup Functions

    protected void locateTextViewReferences() {
        gestureDirectionLabel = (TextView) findViewById(uwaterloo.ca.lab4_205_03.R.id.gestureDirectionID);
    }

    protected void obtainSensorReferences() {
        sensorManagerAccess = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManagerAccess.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    protected void configureHandlers() {
        accelerometerHandler = new AccelerometerSensorHandler(gestureDirectionLabel, myGameLoopTask);
    }

    // MARK: Register/Unregister Handlers correctly

    @Override
    protected void onPause() {
        super.onPause();
        sensorManagerAccess.unregisterListener(accelerometerHandler, accelerometerSensor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Use SENSOR_DELAY_GAME to record readings for context of a game
        sensorManagerAccess.registerListener(accelerometerHandler, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME); //Use SENSOR_DELAY_GAME to record readings for context of a game
    }
}
