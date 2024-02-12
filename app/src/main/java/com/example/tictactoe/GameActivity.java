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

    public int player;     // player 1 or 2
    public String playerName;       // name of the player to display
    public int humanPlayer;     // number for human player
    public int iaPlayer;        // number for IA player
    private boolean solo;        // is game solo or not
    private TextView playerText;        // player display text
    private List<ImageButton> tiles;        // list of buttons for tiles
    private final int[] board = new int[9];       // array of 9 for board
    private int turnCount = 9;      // count of turns to check for tie
    private long mLastClickTime = 0;    // variable to track event time
    private final int delay = 300;      // delay between turns
    private int maxIncursion;     // maximum possible recursion to adapt difficulty
    Handler handler;        // handler to deal with delay


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        handler = new Handler(Looper.getMainLooper());

        // setting all the variables and the board
        player = 1;
        playerText = findViewById(R.id.playertext);

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

        tiles.forEach(imageButton -> {
            imageButton.setTag("empty");
            imageButton.setOnClickListener(this);
            imageButton.setClickable(false);
        });

        solo = getIntent().getExtras().getBoolean("solo");
        if (solo){
            maxIncursion = getIntent().getExtras().getInt("difficulty");

            // let the player decide who start when versus IA
            View orderLayout = findViewById(R.id.orderChoice_layout);
            orderLayout.setVisibility(View.VISIBLE);
            Button orderButton1 = findViewById(R.id.button_order1);
            Button orderButton2 = findViewById(R.id.button_order2);

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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTurn();
                    }
                }, delay);
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
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < delay) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        pressedOnClick(v);
    }

    public void pressedOnClick(View v){
        // set tile images and board depending on player
        ImageButton button = findViewById(v.getId());
        Log.v("TAG", String.valueOf(button.isClickable()));
        button.setImageResource((player == 1) ? R.drawable.cross : R.drawable.circle);
        button.setTag("fill");
        board[tiles.indexOf(button)] = player;
        endTurn();
    }

    public void startTurn(){
        if (!solo || player == humanPlayer){
            // reset tiles if not activated yet
            tiles.forEach(imageButton -> {
                if (imageButton.getTag() == "empty") imageButton.setClickable(true);
            });
        }else {
            int[] move = minimax(board, player, 0);
            tiles.get(move[0]).performClick();
        }
    }

    public void endTurn(){
        // check for win to end game
        if (checkForWin(board, player)){
            endGame(true);
            return;
        }

        // check for tie to end game
        --turnCount;
        if (turnCount == 0){
            endGame(false);
            return;
        }

        // disable all tiles to change turn
        tiles.forEach(imageButton -> imageButton.setClickable(false));

        // add a delay between turns
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                player = (player == 1) ? 2 : 1;
                if(!solo) playerName = (player == 1) ? "Player 1" : "Player 2";
                else playerName = (player == humanPlayer) ? "You" : "IA";
                playerText.setText(playerName);
                startTurn();
            }
        }, delay);
    }

    // Minimax algorithm for IA
    public int[] minimax(int[] newBoard, int currentPlayer, int recursion){
        Integer[] availSpots = findAvailableTiles(newBoard);

        // check for a terminal state and return score
        if (checkForWin(newBoard, humanPlayer)) return new int[]{0,-10};
        else if (checkForWin(newBoard, iaPlayer)) return new int[]{0,10};
        else if (availSpots.length == 0) return new int[]{0,0};
        else if (recursion == maxIncursion) return new int[]{0,0};

        List<int[]> moves = new ArrayList<>();
        int opponentWin = 0;        // count of opponent win next turn in case of multiple defeat possible

        // loop through all possible spots and stock possible moves
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

        // test all possible moves and find the best one to return it
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

    // find the available tiles in board
    public Integer[] findAvailableTiles(int[] board){
        //return Arrays.stream(board).filter(x -> x == 0).toArray();
        List<Integer> avail = new ArrayList<>();
        for (int i = 0; i < board.length; i++){
            if (board[i] == 0) avail.add(i);
        }
        return avail.toArray(new Integer[0]);
    }

    // check all lines for a possible win
    public boolean checkForWin(int[] board, int player){
        // Horizontals checks
        if (board[0] == player && board[1] == player && board[2] == player) return true;
        if (board[3] == player && board[4] == player && board[5] == player) return true;
        if (board[6] == player && board[7] == player && board[8] == player) return true;
        // Vertical checks
        if (board[0] == player && board[3] == player && board[6] == player) return true;
        if (board[1] == player && board[4] == player && board[7] == player) return true;
        if (board[2] == player && board[5] == player && board[8] == player) return true;
        // Diagonal checks
        if (board[0] == player && board[4] == player && board[8] == player) return true;
        if (board[2] == player && board[4] == player && board[6] == player) return true;

        return false;
    }

    public void endGame(boolean winner){

        // set endgame screen
        Group winGroup = findViewById(R.id.winGroup);
        TextView winText = findViewById(R.id.winText);
        winText.setText(winner ? playerName + " win !" : "Draw !");
        winGroup.setVisibility(View.VISIBLE);

        // deal with reset and quit buttons
        Button resetButton = findViewById(R.id.resetButton);
        Button quitButton = findViewById(R.id.quitButton);

        resetButton.setOnClickListener(v -> {
            recreate();
        });

        quitButton.setOnClickListener(v -> {
            startActivity(new Intent(GameActivity.this, MainActivity.class));
        });

        tiles.forEach(imageButton-> imageButton.setClickable(false));
    }
}
