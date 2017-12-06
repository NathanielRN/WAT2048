package uwaterloo.ca.lab4_205_03;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by nathanielruiz98 on 2017-06-23. This file is the mother block, its movements are animated and it stores its necessary values
 */

public class GameBlock extends GameBlockTemplate {

    // Assign two coordinate properties that track the position of the GameBlock
    private int myCoordX;
    private int myCoordY;
    TextView numberTextView;

    // Create a direction property for the Gameblock that tracks that direction the FSM says it should go in
    public GameLoopTask.BlockDirection myDirection = GameLoopTask.BlockDirection.NO_MOVEMENT;

    // Create variables that represent the target coordinate of where the block wishes to move to and from which it will compare its current position
    private int targetXCord;
    private int targetYCord;

    // Declare a variable for change in distance that simulates velocity which increases linearly as well as initial default starting velocity
    private float velocity;
    private final float initialVelocity = ScalingConstantsClass.BLOCK_INITIAL_VELOCITY;

    public int myNumber = 0;

    private RelativeLayout superRL;

    public boolean scheduleForRemoval = false;
    public boolean scheduleForDoubling = false;
    public boolean movingOnThisGesture = false;

    // This constructor is provided to silence a warning that says ImageView must provide a constructor with this signature
    GameBlock (Context myContext) {
        super(myContext);
    }

    // Constructor that initializes all required values
    GameBlock(Context myContext, RelativeLayout superRL, int coordX, int coordY) {
        // Call the super value for appropriate context
        super(myContext);

        this.superRL = superRL;

        // Assign the coordinates the block gets initialized at to the block's tracking variables for its coordinate
        this.myCoordX = coordX;
        this.myCoordY = coordY;

        // Initialize the target coordinates to some value so that they are not 0 at any point!
        this.targetYCord = coordY;
        this.targetXCord = coordX;

        // MARK: The Block Image

        // Assign the image that this class will use from the project's resource folder
        this.setImageResource(uwaterloo.ca.lab4_205_03.R.drawable.gameblock);

        // Set the image's initial x and y coordinates based on what the coordinates at the beginning are
        this.setX(myCoordX);
        this.setY(myCoordY);

        // Declare a scale factor and scale our image appropriately
        float IMAGE_SCALE = ScalingConstantsClass.BLOCK_IMAGE_SCALING;
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);

        superRL.addView(this);

        // MARK: The Number Image

        Random randomNumberGenerator = new Random();

        myNumber = randomNumberGenerator.nextInt(2);
        ++myNumber;
        myNumber *= 2;

        numberTextView = new TextView(myContext);
        updateMyNumberTextView();
        numberTextView.setTextColor(Color.BLACK);
        superRL.addView(numberTextView);
        numberTextView.setX(myCoordX + ScalingConstantsClass.NUMBER_TEXTVIEW_X_OFFSET);
        numberTextView.setY(myCoordY + ScalingConstantsClass.NUMBER_TEXTVIEW_Y_OFFSET);

        numberTextView.bringToFront();

        numberTextView.setTextSize(ScalingConstantsClass.NUMBER_TEXTVIEW_SIZE);

        // Assign velocity it's default initial value of initialVelocity
        this.velocity = initialVelocity;
    }

    private void updateMyNumberTextView() {
        numberTextView.setText(String.format(Locale.US, "%d",myNumber));
    }

    // Methods from Game Block Template

    public void move() {

        // Define a variable that represents our acceleration as the block moves a distance in pixels
        float ACCELERATION = ScalingConstantsClass.BLOCK_CONSTANT_ACCELERATION;

        // A switch statement that updates the block's current position based on it's desired direction if it has reached its targeted destination yet or not
        // This is where our animation logic works where the myCoord variables get gradually updated along with every call from the timer to simulate a smooth transition
        switch (myDirection) {
            case LEFT:
                if (myCoordX - velocity > targetXCord){
                    myCoordX -= velocity;
                    this.setX(myCoordX);
                    numberTextView.setX(myCoordX + ScalingConstantsClass.NUMBER_TEXTVIEW_X_OFFSET);
                    velocity += ACCELERATION;
                }else{
                    this.setX(targetXCord);
                    myCoordX = targetXCord;
                    numberTextView.setX(myCoordX + ScalingConstantsClass.NUMBER_TEXTVIEW_X_OFFSET);
                    this.myDirection = GameLoopTask.BlockDirection.NO_MOVEMENT;
                    velocity = initialVelocity;
                }
                break;
            case RIGHT:
                if (myCoordX + velocity < targetXCord){
                    myCoordX += velocity;
                    this.setX(myCoordX);
                    numberTextView.setX(myCoordX + ScalingConstantsClass.NUMBER_TEXTVIEW_X_OFFSET);
                    velocity += ACCELERATION;
                }else{
                    this.setX(targetXCord);
                    myCoordX = targetXCord;
                    numberTextView.setX(myCoordX + ScalingConstantsClass.NUMBER_TEXTVIEW_X_OFFSET);
                    this.myDirection = GameLoopTask.BlockDirection.NO_MOVEMENT;
                    velocity = initialVelocity;
                }
                break;
            case UP:
                if (myCoordY - velocity > targetYCord){
                    myCoordY -= velocity;
                    this.setY(myCoordY);
                    numberTextView.setY(myCoordY + ScalingConstantsClass.NUMBER_TEXTVIEW_Y_OFFSET);
                    velocity += ACCELERATION;
                }else{
                    this.setY(targetYCord);
                    myCoordY = targetYCord;
                    numberTextView.setY(myCoordY + ScalingConstantsClass.NUMBER_TEXTVIEW_Y_OFFSET);
                    this.myDirection = GameLoopTask.BlockDirection.NO_MOVEMENT;
                    velocity = initialVelocity;
                }
                break;
            case DOWN:
                if (myCoordY + velocity < targetYCord){
                    myCoordY += velocity;
                    this.setY(myCoordY);
                    numberTextView.setY(myCoordY + ScalingConstantsClass.NUMBER_TEXTVIEW_Y_OFFSET);
                    velocity += ACCELERATION;
                }else{
                    this.setY(targetYCord);
                    myCoordY = targetYCord;
                    numberTextView.setY(myCoordY + ScalingConstantsClass.NUMBER_TEXTVIEW_Y_OFFSET);
                    this.myDirection = GameLoopTask.BlockDirection.NO_MOVEMENT;
                    velocity = initialVelocity;
                }
                break;
            case NO_MOVEMENT:
                break;
            default:
                this.setX(0);
                this.setY(0);
        }
    }

    // We also need to look into how to get the block to generate AFTER movement is finished
    public void setDestination() {
        this.myDirection = GameLoopTask.currentGameDirection;
        int furthestXCord;
        int furthestYCord;
        int slotCount = 0;
        int freeSpace;
        int numberOfBlocksInTheWay = 0;

        int coordstoCheck[] = new int[2];

        switch(GameLoopTask.currentGameDirection){
            case LEFT:
                furthestXCord = GameLoopTask.LEFT_BOUNDARY; //The furthest left bound
                coordstoCheck[1] = myCoordY;
                List <Integer> leftDirectionBlockNumbers = new ArrayList<>();
                boolean potentialForLeftMerge = true;
                GameBlock mightMergeWithThisLeftBlock = null;
                for (int x = myCoordX - GameLoopTask.SLOT_ISOLATION; x >= furthestXCord; x -= GameLoopTask.SLOT_ISOLATION) { // loop to iterate through all the slots
                    ++slotCount;
                    coordstoCheck[0] = x; //only x position is changing...y remains constant
                    GameBlock oneAwayBlock = GameLoopTask.isOccupied(coordstoCheck); //check if the block to the left is occupied
                    if (oneAwayBlock != null) { // if occupied
                        if (oneAwayBlock.myNumber == myNumber && potentialForLeftMerge) { //if the blocks can merge and the numbers are the same
                            potentialForLeftMerge = false;
                            mightMergeWithThisLeftBlock = oneAwayBlock;
                        }
                        Collections.reverse(leftDirectionBlockNumbers);
                        leftDirectionBlockNumbers.add(oneAwayBlock.myNumber); // make a collection of all blocks in the way to the left that can't be merged
                        Collections.reverse(leftDirectionBlockNumbers);
                        ++numberOfBlocksInTheWay;
                    }
                }

                int previousLeftNumber = -1;
                boolean mergeWithMyLeftNumberAlreadyOccured = false;
                for(int blockNumber: leftDirectionBlockNumbers) {
                    if (blockNumber == previousLeftNumber) {
                        --numberOfBlocksInTheWay; // since block numbers match...they have potential to merge
                        if (blockNumber != myNumber) {
                            previousLeftNumber = -1;
                        }
                        if (blockNumber == myNumber) {
                            mergeWithMyLeftNumberAlreadyOccured = !mergeWithMyLeftNumberAlreadyOccured; // if number matches our block number, then it can merge
                        }
                    } else {
                        previousLeftNumber = blockNumber; // change the previous left number to the block number otherwise and repeat the process
                    }
                }

                if(leftDirectionBlockNumbers.size() > 0 && myNumber == leftDirectionBlockNumbers.get(leftDirectionBlockNumbers.size() -1) && !mergeWithMyLeftNumberAlreadyOccured) {
                    if (mightMergeWithThisLeftBlock != null) {
                        merge(mightMergeWithThisLeftBlock); // call the merge method to merge all the possible merge candidates found earlier
                        --numberOfBlocksInTheWay; // This is hiding behind the SECOND in a double merge!
                    }
                }

                freeSpace = slotCount - numberOfBlocksInTheWay; //calculate free slots
                movingOnThisGesture = freeSpace != 0;
                targetXCord = myCoordX - freeSpace*GameLoopTask.SLOT_ISOLATION;
                break;
            case RIGHT:

                furthestXCord = GameLoopTask.LEFT_BOUNDARY + GameLoopTask.SLOT_ISOLATION * 3; //The furthest right bound
                coordstoCheck[1] = myCoordY;
                List <Integer> rightDirectionBlockNumbers = new ArrayList<>();
                boolean potentialForRightMerge = true;
                GameBlock mightMergeWithThisRightBlock = null;

                for (int x = myCoordX + GameLoopTask.SLOT_ISOLATION; x <= furthestXCord; x += GameLoopTask.SLOT_ISOLATION) { // loop to iterate through all the slots
                    ++slotCount;
                    coordstoCheck[0] = x; //only x position is changing...y remains constant
                    GameBlock oneAwayBlock = GameLoopTask.isOccupied(coordstoCheck); //check if the block to the right is occupied
                    if (oneAwayBlock != null) {
                        if (oneAwayBlock.myNumber == myNumber && potentialForRightMerge) { //if the blocks can merge and the numbers are the same
                            potentialForRightMerge = false;
                            mightMergeWithThisRightBlock = oneAwayBlock;
                        }
                        Collections.reverse(rightDirectionBlockNumbers);
                        rightDirectionBlockNumbers.add(oneAwayBlock.myNumber); // make a collection of all blocks in the way to the right that can't be merged
                        Collections.reverse(rightDirectionBlockNumbers);
                        ++numberOfBlocksInTheWay;
                    }
                }

                int previousRightNumber = -1;
                boolean mergeWithMyRightNumberAlreadyOccured = false;
                for(int blockNumber: rightDirectionBlockNumbers) {
                    if (blockNumber == previousRightNumber) {
                        --numberOfBlocksInTheWay; // since block numbers match...they have potential to merge
                        if (blockNumber != myNumber) {
                            previousRightNumber = -1;
                        }
                        if (blockNumber == myNumber) {
                            mergeWithMyRightNumberAlreadyOccured = !mergeWithMyRightNumberAlreadyOccured; // if number matches our block number, then it can merge
                        }
                    } else {
                        previousRightNumber = blockNumber; // change the previous right number to the block number otherwise and repeat the process
                    }
                }

                if(rightDirectionBlockNumbers.size() > 0 && myNumber == rightDirectionBlockNumbers.get(rightDirectionBlockNumbers.size() -1) && !mergeWithMyRightNumberAlreadyOccured) {
                    if (mightMergeWithThisRightBlock != null) {
                        merge(mightMergeWithThisRightBlock); // call the merge method to merge all the possible merge candidates found earlier
                        --numberOfBlocksInTheWay;
                    }
                }

                freeSpace = slotCount - numberOfBlocksInTheWay; // calculate the free slots
                movingOnThisGesture = freeSpace != 0;
                targetXCord = myCoordX + freeSpace*GameLoopTask.SLOT_ISOLATION;
                break;
            case UP:
                furthestYCord = GameLoopTask.TOP_BOUNDARY; //The furthest top bound
                coordstoCheck[0] = myCoordX;
                List <Integer> upDirectionBlockNumbers = new ArrayList<>();
                boolean potentialForUpMerge = true;
                GameBlock mightMergeWithThisUpBlock = null;

                for (int y = myCoordY - GameLoopTask.SLOT_ISOLATION; y >= furthestYCord; y -= GameLoopTask.SLOT_ISOLATION) { // loop to iterate through all the slots
                    ++slotCount;
                    coordstoCheck[1] = y; //only y position is changing...x remains constant
                    GameBlock oneAwayBlock = GameLoopTask.isOccupied(coordstoCheck);//check if the block above is occupied
                    if (oneAwayBlock != null) {
                        if (oneAwayBlock.myNumber == myNumber && potentialForUpMerge) { //if the blocks can merge and the numbers are the same
                            potentialForUpMerge = false;
                            mightMergeWithThisUpBlock = oneAwayBlock;
                        }
                        Collections.reverse(upDirectionBlockNumbers);
                        upDirectionBlockNumbers.add(oneAwayBlock.myNumber); // make a collection of all blocks in the way to above that can't be merged
                        Collections.reverse(upDirectionBlockNumbers);
                        ++numberOfBlocksInTheWay;
                    }
                }

                int previousUpNumber = -1;
                boolean mergeWithMyUpNumberAlreadyOccured = false;
                for(int blockNumber: upDirectionBlockNumbers) {
                    if (blockNumber == previousUpNumber) {
                        --numberOfBlocksInTheWay; // since block numbers match...they have potential to merge
                        if (blockNumber != myNumber) {
                            previousUpNumber = -1;
                        }
                        if (blockNumber == myNumber) {
                            mergeWithMyUpNumberAlreadyOccured = !mergeWithMyUpNumberAlreadyOccured; // if number matches our block number, then it can merge
                        }
                    } else {
                        previousUpNumber = blockNumber; // change the previous up number to the block number otherwise and repeat the process
                    }
                }

                if(upDirectionBlockNumbers.size() > 0 && myNumber == upDirectionBlockNumbers.get(upDirectionBlockNumbers.size() -1) && !mergeWithMyUpNumberAlreadyOccured) {
                    if (mightMergeWithThisUpBlock != null) {
                        merge(mightMergeWithThisUpBlock); // call the merge method to merge all the possible merge candidates found earlier
                        --numberOfBlocksInTheWay;
                    }
                }

                freeSpace = slotCount - numberOfBlocksInTheWay; //calculate the free slots
                movingOnThisGesture = freeSpace != 0;
                targetYCord = myCoordY - freeSpace*GameLoopTask.SLOT_ISOLATION;
                break;
            case DOWN:
                furthestYCord = GameLoopTask.TOP_BOUNDARY + GameLoopTask.SLOT_ISOLATION * 3; //The furthest bottom bound
                coordstoCheck[0] = myCoordX;
                List <Integer> downDirectionBlockNumbers = new ArrayList<>();
                boolean potentialForDownMerge = true;
                GameBlock mightMergeWithThisDownBlock = null;

                for (int y = myCoordY + GameLoopTask.SLOT_ISOLATION; y <= furthestYCord; y += GameLoopTask.SLOT_ISOLATION) { // loop to iterate through all the slots
                    ++slotCount;
                    coordstoCheck[1] = y; //only y position is changing...x remains constant
                    GameBlock oneAwayBlock = GameLoopTask.isOccupied(coordstoCheck); //check if the block below is occupied
                    if (oneAwayBlock != null) {
                        if (oneAwayBlock.myNumber == myNumber && potentialForDownMerge) { //if the blocks can merge and the numbers are the same
                            potentialForDownMerge = false;
                            mightMergeWithThisDownBlock = oneAwayBlock;
                        }
                        Collections.reverse(downDirectionBlockNumbers);
                        downDirectionBlockNumbers.add(oneAwayBlock.myNumber); // make a collection of all blocks in the way to below that can't be merged
                        Collections.reverse(downDirectionBlockNumbers);
                        ++numberOfBlocksInTheWay;
                    }
                }

                int previousDownNumber = -1;
                boolean mergeWithMyDownNumberAlreadyOccured = false;
                for(int blockNumber: downDirectionBlockNumbers) {
                    if (blockNumber == previousDownNumber) {
                        --numberOfBlocksInTheWay; // since block numbers match...they have potential to merge
                        if (blockNumber != myNumber) {
                            previousDownNumber = -1;
                        }
                        if (blockNumber == myNumber) {
                            mergeWithMyDownNumberAlreadyOccured = !mergeWithMyDownNumberAlreadyOccured; // if number matches our block number, then it can merge
                        }
                    } else {
                        previousDownNumber = blockNumber; // change the previous down number to the block number otherwise and repeat the process
                    }
                }

                if(downDirectionBlockNumbers.size() > 0 && myNumber == downDirectionBlockNumbers.get(downDirectionBlockNumbers.size() -1) && !mergeWithMyDownNumberAlreadyOccured) {
                    if (mightMergeWithThisDownBlock != null) {
                        merge(mightMergeWithThisDownBlock); // call the merge method to merge all the possible merge candidates found earlier
                        --numberOfBlocksInTheWay;
                    }
                }

                freeSpace = slotCount - numberOfBlocksInTheWay; //calculate the free slots
                movingOnThisGesture = freeSpace != 0;
                targetYCord = myCoordY + freeSpace*GameLoopTask.SLOT_ISOLATION;
                break;
            case NO_MOVEMENT: // don't do anything if there is no movement
                break;
            default:
                break;

        }
    }

    private void merge(GameBlock targetBlock){
        targetBlock.scheduleForDoubling = true;
        this.scheduleForRemoval = true;
    }

    public void doubleMyNumber(){
        this.myNumber *= 2; //double the number
        this.updateMyNumberTextView(); //updates the block with the new number
    }

    public int[] getCoords() { //function to get the coordinates
        int arrayOfBlockCoordinates[] = new int[2];
        arrayOfBlockCoordinates[0] = myCoordX;
        arrayOfBlockCoordinates[1] = myCoordY;
        return arrayOfBlockCoordinates;
    }

    public int[] getTargetCoords() { //function to get the target or final position coordinates
        int arrayOfBlockCoordinates[] = new int[2];
        arrayOfBlockCoordinates[0] = targetXCord;
        arrayOfBlockCoordinates[1] = targetYCord;
        return arrayOfBlockCoordinates;
    }

    public void selfDestruct() { // function to destroy the gameblock text view
        superRL.removeView(numberTextView);
        superRL.removeView(this);
    }

    public boolean willIEverFindMerge() {
        int[] aboveMe = {this.myCoordX, this.myCoordY - GameLoopTask.SLOT_ISOLATION};
        int[] belowMe = {this.myCoordX, this.myCoordY + GameLoopTask.SLOT_ISOLATION};
        int[] rightOfMe = {this.myCoordX + GameLoopTask.SLOT_ISOLATION, this.myCoordY};
        int[] leftOfMe = {this.myCoordX - GameLoopTask.SLOT_ISOLATION, this.myCoordY};

        GameBlock theBlockAboveMe = GameLoopTask.isOccupied(aboveMe);
        GameBlock theBlockBelowMe = GameLoopTask.isOccupied(belowMe);
        GameBlock theBlockRightOfMe = GameLoopTask.isOccupied(rightOfMe);
        GameBlock theBlockLeftOfMe = GameLoopTask.isOccupied(leftOfMe);

        return (myCoordY > GameLoopTask.TOP_BOUNDARY && (theBlockAboveMe == null  || theBlockAboveMe.myNumber == myNumber)) ||
                (myCoordY < GameLoopTask.TOP_BOUNDARY + 3 * GameLoopTask.SLOT_ISOLATION && (theBlockBelowMe == null  || theBlockBelowMe.myNumber == myNumber)) ||
                (myCoordX > GameLoopTask.LEFT_BOUNDARY && (theBlockLeftOfMe == null  || theBlockLeftOfMe.myNumber == myNumber)) ||
                (myCoordX < GameLoopTask.LEFT_BOUNDARY + 3 * GameLoopTask.SLOT_ISOLATION && (theBlockRightOfMe == null  || theBlockRightOfMe.myNumber == myNumber));
    }
}
