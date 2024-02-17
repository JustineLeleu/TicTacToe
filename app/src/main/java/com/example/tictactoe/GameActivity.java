package com.example.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.constraintlayout.widget.Group;

import java.util.*;

public class GameActivity extends Activity implements View.OnClickListener {

    // Variables for game state
    public int player;     // Current player (1 or 2)
    public String playerName;       // Display name of the current player
    public int humanPlayer;     // Player number for the human player
    public int iaPlayer;        // Player number for the AI player
    private boolean solo;        // Flag indicating if the game is solo or not
    private TextView playerText;        // TextView to display the current player's name
    private List<ImageButton> tiles;        // List of ImageButtons representing the game tiles
    private final int[] board = new int[9];       // Array representing the game board
    private int turnCount = 9;      // Counter for the number of turns remaining
    private long mLastClickTime = 0;    // Timestamp of the last click event
    private final int delay = 300;      // Delay in milliseconds between turns
    private int maxIncursion;     // Maximum depth for the minimax algorithm (difficulty level)
    Handler handler;        // Handler for scheduling delayed tasks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        handler = new Handler(Looper.getMainLooper());

        // Initialize game state
        player = 1;
        playerText = findViewById(R.id.playertext);

        // Initialize game tiles
        tiles = new ArrayList<ImageButton>(){{
            add(findViewById(R.id.imageButton1));
            add(findViewById(R.id.imageButton2));
            add(findViewById(R.id.imageButton3));
            add(findViewById(R.id.imageButton4));
            add(findViewById(R.id.imageButton5));
            add(findViewById(R.id.imageButton6));
            add(findViewById(R.id.imageButton7));
            add(findViewById(R.id.imageButton8));
            add(findViewById(R.id.imageButton9));
        }};

        // Set up game tiles
        tiles.forEach(imageButton -> {
            imageButton.setTag("empty");
            imageButton.setOnClickListener(this);
            imageButton.setClickable(false);
        });

        // Check if the game is solo and set up accordingly
        solo = getIntent().getExtras().getBoolean("solo");
        if (solo){
            maxIncursion = getIntent().getExtras().getInt("difficulty");

            // Let the player decide who starts when playing against the AI
            View orderLayout = findViewById(R.id.orderChoice_layout);
            orderLayout.setVisibility(View.VISIBLE);
            Button orderButton1 = findViewById(R.id.button_order1);
            Button orderButton2 = findViewById(R.id.button_order2);

            // Set up the order buttons
            orderButton1.setOnClickListener(v -> {
                humanPlayer = 1;
                iaPlayer = 2;
                playerName = "You";
                playerText.setText(playerName);
                orderLayout.setVisibility(View.INVISIBLE);
                startTurn();
            });

            orderButton2.setOnClickListener(v -> {
                humanPlayer = 2;
                iaPlayer = 1;
                playerName = "IA";
                playerText.setText(playerName);
                orderLayout.setVisibility(View.INVISIBLE);
                handler.postDelayed(this::startTurn, delay);
            });
        }
        else {
            playerName = "Player 1";
            playerText.setText(playerName);
            startTurn();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        // Prevent multiple clicks within a short time frame
        if (SystemClock.elapsedRealtime() - mLastClickTime < delay) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        pressedOnClick(v);
    }

    public void pressedOnClick(View v){
        // Update the clicked tile and the game board
        ImageButton button = findViewById(v.getId());
        Log.v("TAG", String.valueOf(button.isClickable()));
        button.setImageResource((player == 1) ? R.drawable.cross : R.drawable.circle);
        button.setTag("fill");
        board[tiles.indexOf(button)] = player;
        endTurn();
    }

    public void startTurn(){
        // Enable the tiles for the current player's turn
        if (!solo || player == humanPlayer){
            tiles.forEach(imageButton -> {
                if (imageButton.getTag() == "empty") imageButton.setClickable(true);
            });
        }else {
            // If it's the AI's turn, use the minimax algorithm to decide the move
            int[] move = minimax(board, player, 0);
            tiles.get(move[0]).performClick();
        }
    }

    public void endTurn(){
        // Check for win or tie and end the game if necessary
        if (checkForWin(board, player)){
            endGame(true);
            return;
        }

        --turnCount;
        if (turnCount == 0){
            endGame(false);
            return;
        }

        // Disable all tiles and switch turns
        tiles.forEach(imageButton -> imageButton.setClickable(false));

        // Add a delay between turns
        handler.postDelayed(() -> {
            player = (player == 1) ? 2 : 1;
            if(!solo) playerName = (player == 1) ? "Player 1" : "Player 2";
            else playerName = (player == humanPlayer) ? "You" : "IA";
            playerText.setText(playerName);
            startTurn();
        }, delay);
    }

    // Minimax algorithm for AI decision making
    public int[] minimax(int[] newBoard, int currentPlayer, int recursion){
        Integer[] availSpots = findAvailableTiles(newBoard);

        // Check for terminal states and return a score
        if (checkForWin(newBoard, humanPlayer)) return new int[]{0,-10};
        else if (checkForWin(newBoard, iaPlayer)) return new int[]{0,10};
        else if (availSpots.length == 0) return new int[]{0,0};
        else if (recursion == maxIncursion) return new int[]{0,0};

        List<int[]> moves = new ArrayList<>();
        int opponentWin = 0;        // Count of potential wins for the opponent in the next turn

        // Loop through all possible moves and evaluate them
        for (int spot : availSpots){
            int[] move = new int[2];
            move[0] = spot;
            newBoard[spot] = currentPlayer;
            if (currentPlayer == humanPlayer){
                int[] result = minimax(newBoard, iaPlayer, recursion + 1);
                if (result[1] == -10){
                    ++opponentWin;
                    result[1] *= opponentWin;
                }
                move[1] = result[1];
            }
            if (currentPlayer == iaPlayer){
                int[] result = minimax(newBoard, humanPlayer, recursion + 1);
                move[1] = result[1];
            }
            newBoard[spot] = 0;
            moves.add(move);
        }

        // Choose the best move based on the evaluations
        int[] bestMove = new int[2];
        if (currentPlayer == humanPlayer){
            int bestScore = 10000;
            for (int[] move : moves) {
                if (move[1] < bestScore) {
                    bestScore = move[1];
                    bestMove = move;
                }
            }
        }
        if (currentPlayer == iaPlayer){
            int bestScore = -10000;
            for (int[] move : moves) {
                if (move[1] > bestScore) {
                    bestScore = move[1];
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    // Find the available tiles on the board
    public Integer[] findAvailableTiles(int[] board){
        List<Integer> avail = new ArrayList<>();
        for (int i = 0; i < board.length; i++){
            if (board[i] == 0) avail.add(i);
        }
        return avail.toArray(new Integer[0]);
    }

    // Check for a win condition
    public boolean checkForWin(int[] board, int player){
        // Check all possible winning combinations
        if (board[0] == player && board[1] == player && board[2] == player) return true;
        if (board[3] == player && board[4] == player && board[5] == player) return true;
        if (board[6] == player && board[7] == player && board[8] == player) return true;
        if (board[0] == player && board[3] == player && board[6] == player) return true;
        if (board[1] == player && board[4] == player && board[7] == player) return true;
        if (board[2] == player && board[5] == player && board[8] == player) return true;
        if (board[0] == player && board[4] == player && board[8] == player) return true;
        if (board[2] == player && board[4] == player && board[6] == player) return true;

        return false;
    }

    public void endGame(boolean winner){

        // Display the end game screen
        Group winGroup = findViewById(R.id.winGroup);
        TextView winText = findViewById(R.id.winText);
        winText.setText(winner ? playerName + " win !" : "Draw !");
        winGroup.setVisibility(View.VISIBLE);

        // Set up the reset and quit buttons
        Button resetButton = findViewById(R.id.resetButton);
        Button quitButton = findViewById(R.id.quitButton);

        resetButton.setOnClickListener(v -> recreate());

        quitButton.setOnClickListener(v -> startActivity(new Intent(GameActivity.this, MainActivity.class)));

        // Disable all tiles
        tiles.forEach(imageButton-> imageButton.setClickable(false));
    }
}