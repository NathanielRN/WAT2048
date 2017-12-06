package uwaterloo.ca.lab4_205_03;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

/**
 * Created by nathanielruiz98 on 2017-06-23. The running game loop task
 */

class GameLoopTask extends TimerTask {

    // Enum that describes all the possible directions a block can take on the game board including not moving at all
    enum BlockDirection {UP, DOWN, LEFT, RIGHT, NO_MOVEMENT}

    // Game outcomes as an enum
    private enum GameOutcomes {YOUWIN, GAMEOVER}

    // Need access to these variables because the game loop will be updating the UI by adding blocks on the screen for example
    private Activity myActivity;
    private Context myContext;
    private RelativeLayout MyRL;

    // Track the current game's direction
    static BlockDirection currentGameDirection = BlockDirection.NO_MOVEMENT;
    private boolean finishedMovingAndReadyToAcceptNewDestination;

    // Flag to help us create a block after all blocks are finished moving
    private boolean scheduleACreation = false;

    // You can only win once!
    private boolean gameWonOnce = false;

    // Linked List of all our GameBlocks
    static private LinkedList <GameBlock> myGBList;

    // These boundaries were determined through trial and error to be the respective variables appropriate for our game grid and our block's position relative to it
    final static int LEFT_BOUNDARY = ScalingConstantsClass.LEFTMOST_POINT;
    final static int TOP_BOUNDARY = ScalingConstantsClass.TOP_POINT;
    final static int SLOT_ISOLATION = ScalingConstantsClass.GRIDBOARD_SLOT_WIDTH;

    // Create a random Number Generator that will be used in block creation
    private Random randomNumberGenerator;

    // Access to the main textView
    private TextView userTextView;

    // Our constructor for the game loop task that initializes the necessary graphic tools and other properties
    GameLoopTask(Activity myActivity, RelativeLayout MyRL, Context myContext) {

        // Assign our graphic variables values so that they maybe used
        this.myActivity = myActivity;
        this.myContext = myContext;
        this.MyRL = MyRL;

        // Initialize our random number generator
        this.randomNumberGenerator = new Random();

        // Initialize our GameBlock linked list to be empty
        myGBList = new LinkedList<>();

        // Create a block upon creating a game loop task because you need to start with one!
        createBlock();

        userTextView = (TextView) MyRL.findViewById(uwaterloo.ca.lab4_205_03.R.id.gestureDirectionID);
    }

    // Override the TimerTask run method to specify what block will be executed during every interval of the timer
    @Override
    public void run() {
        // We will run on UI thread because we are updating the UI
        myActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        //Bring the text to front even if new blocks are created to come over them
                        userTextView.bringToFront();

                        // Assume that we do have to end the game until we find that there IS a block that can move
                        boolean endOfGame = true;

                        // Assume there will be no movement until a block tells us there is
                        boolean thereWasMovement = false;

                        // We are finished moving and can NOW accept the creating of a new block
                        finishedMovingAndReadyToAcceptNewDestination = true;

                        for (GameBlock block: myGBList){
                            finishedMovingAndReadyToAcceptNewDestination = finishedMovingAndReadyToAcceptNewDestination && (block.myDirection == BlockDirection.NO_MOVEMENT);

                            // If our function finds that any of the blocks CAN move (not that they did) then don't end the game just yet
                            if (block.willIEverFindMerge()) {
                                endOfGame = false;
                            }

                            // If we find that any of the blocks do move on the current gesture, then we will allow a block to be created
                            if (block.movingOnThisGesture) {
                                thereWasMovement = true;
                            }
                        }

                        // If we get to this point and find that no block has any more moves, spam the logs with a game over. Return and don't continue.
                        if (endOfGame) {
                            Log.wtf("you failed the game", "GAME OVER");
                            userTextView.setText(GameOutcomes.GAMEOVER.toString());
                            return;
                        }

                        // Our logic for the run function that will update the game's current situation
                        if (finishedMovingAndReadyToAcceptNewDestination){

                            // If we are allowed to create now and we've confirmed that there is movement, then allow the creation of a block and reset the schedule
                            if (scheduleACreation && thereWasMovement){
                                createBlock();
                                scheduleACreation = false;
                            }

                            // Declare a second linked list that contains all blocks ready to be removed. It's scope is here because no one else needs access to it.
                            // Done in this way because updating a linked list while iterating through it causes an exception in Java
                            LinkedList <GameBlock> gameBlocksToBeRemoved = new LinkedList<>();

                            // Iterate through the blocks and double and/or move those to be removed
                            // We update the block's number here to make it easier to evaluate the numbers in our merging algorithm
                            for (GameBlock block: myGBList){

                                // We will call the blocks own function so it can update its own numbers
                                if (block.scheduleForDoubling){
                                    block.doubleMyNumber();
                                    block.scheduleForDoubling = false;
                                    if (block.myNumber == 64 && !gameWonOnce) {
                                        Log.wtf("won", "YOU WINNNN!!!!!");
                                        userTextView.setText(GameOutcomes.YOUWIN.toString());
                                        gameWonOnce = true;
                                    }
                                    if (block.myNumber > 99) {
                                        block.numberTextView.setTextSize(18);
                                    }
                                }

                                // We will schedule the block for removal by adding it to the "to be remove" linked list
                                if (block.scheduleForRemoval){
                                    gameBlocksToBeRemoved.add(block);
                                }
                            }

                            // Iterate through the list of to be removed blocks and remove them from the main linked list
                            for (GameBlock blockToBeRemoved : gameBlocksToBeRemoved){
                                myGBList.remove(blockToBeRemoved);
                                blockToBeRemoved.selfDestruct();
                            }
                        }

                        // We call each block's move function so they can move to the target we calculated
                        for (GameBlock block : myGBList){
                            block.move();
                        }
                    }
                }
        );
    }

    // A function to create an instance of a GameBlock and add it to the view
    private void createBlock() {

        // Declare a 2D array of booleans that will store the status of each potential spot on the board
        Boolean[][] statusOfCoordinates = new Boolean[4][4];

        // Iterate through the 2D array and initialize all of the spots to be false, as in empty
        for(Boolean[] row: statusOfCoordinates) {
            Arrays.fill(row, false);
        }

        // Now, go through our main GameBlock list and through some algerba manipulations assign their coordinates logical positions in our 2D array
        // that respect their row and column position behaviour
        for (GameBlock block: myGBList) {
            int blockXandY[] = block.getTargetCoords();
            statusOfCoordinates[((blockXandY[0] - LEFT_BOUNDARY) / SLOT_ISOLATION)][((blockXandY[1] - TOP_BOUNDARY) / SLOT_ISOLATION)] = true;
        }

        // Start off by assuming that there are no empty spots
        int numberOfEmptySpots = 0;

        // Iterate through the 2D array and if we find that the position is false as in it IS empty, then add to our counter of empty spots
        for (int indexOfGridRow = 0; indexOfGridRow < 4; indexOfGridRow++) {
            for (int indexOfGridColumn = 0; indexOfGridColumn < 4; indexOfGridColumn++) {
                if (!statusOfCoordinates[indexOfGridRow][indexOfGridColumn]) {
                    numberOfEmptySpots++;
                }
            }
        }

        // No empty spots is a possible case, but not one we can deal with it terms of creating a new block, so leave the prospect of adding a block
        if (numberOfEmptySpots == 0) {
            Log.wtf("bad move", "Moving in this direction produced no empty spots! Bad move player! (Game's not over yet though!)");
            return;
        }

        // Find a random slot from 0 to the number of empty slots exclusive
        int randomEmptySlot = randomNumberGenerator.nextInt(numberOfEmptySpots);

        // Here we define a for loop that goes through all the empty slots until we hit the index of the empty slot equivalent to that determined from our random number generator
        rowLoop:
        for (int indexOfGridRow = 0; indexOfGridRow < 4; indexOfGridRow++) {
            for (int indexOfGridColumn = 0; indexOfGridColumn < 4; indexOfGridColumn++) {
                if (!statusOfCoordinates[indexOfGridRow][indexOfGridColumn] && randomEmptySlot > 0) {
                    randomEmptySlot--;
                } else if (!statusOfCoordinates[indexOfGridRow][indexOfGridColumn] && randomEmptySlot == 0) {

                    // We have found the block, create it with the initial points found by our for loop, and add it to our linked list
                    GameBlock newBlock = new GameBlock(myContext, MyRL, LEFT_BOUNDARY + SLOT_ISOLATION * indexOfGridRow, TOP_BOUNDARY + SLOT_ISOLATION * indexOfGridColumn);
                    myGBList.add(newBlock);
                    break rowLoop;
                }
            }
        }
    }

    // Function to the set the direction of the block we created by access its setter method and passing in the appropriate direction
    void setDirection(BlockDirection directionOfBlock) {

        // Boolean to determine if we are finished moving, will be manipulated
        finishedMovingAndReadyToAcceptNewDestination = true;

        // For loop that will finilize our statement of no movement with the blocks
        for (GameBlock block: myGBList){
            finishedMovingAndReadyToAcceptNewDestination = finishedMovingAndReadyToAcceptNewDestination && (block.myDirection == BlockDirection.NO_MOVEMENT);
        }

        // Here we continue based on if we are done moving
        if (finishedMovingAndReadyToAcceptNewDestination) {

            // We set the current direction that all blocks will follow!
            currentGameDirection = directionOfBlock;

            // As long as the block is not getting ready to be removed, we call the setting destination block
            for (GameBlock block : myGBList) {
                if (!block.scheduleForRemoval) {
                    block.setDestination();
                }
            }

            // Now that we have officially set everything to move, we allow a block to be created so that animation isn't strange
            scheduleACreation = true;
        }
    }

    // Our isOccupied function will take in any coordinates as an array and using the main GameBlock list will determine if there actually is a block there to return
    // Otherwise, this will return null indicating there is no block at that point, this can be out of the board or usually means that slot is empty
    static GameBlock isOccupied(int[] coordsToCheck){
        for (GameBlock block: myGBList){

            // Using the block's getter function we get the coordinates safely
            if ((coordsToCheck[0] == block.getCoords()[0]) && (coordsToCheck[1] == block.getCoords()[1])){
                return block;
            }
        }

        // We need to be careful because this function can return null and cause an exception.
        return null;
    }
}