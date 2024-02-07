package Program1;

import java.util.*;

public class StateTree {
    private char[][] gameState;
    private int[] move;
    private char[][] childGameState;
    private int[][] heuristicValue;
    private boolean[][] legalMoves;
    private LinkedList<StateTree> children;
    private StateTree parent;
    private int min;
    private int max;
    private boolean isLeaf;
    private final int maxDepth = 3;
    private int depth;
    private int playerNum;
    public StateTree(char[][] state, StateTree parent, boolean leaf, int num, int depth, int[] move) {
        this.gameState = state;
        this.parent = parent;
        min = Integer.MIN_VALUE; max = Integer.MAX_VALUE;
        isLeaf = leaf;
        children = new LinkedList<StateTree>();
        playerNum = num;
        this.depth = depth;
        this.move = new int[2];
        this.move = move;
    }

    /* Getters */
    public int getMin() {
        return min;
    }
    public int getMax() {
        return max;
    }

    public int[] getMove() {
        return move;
    }

    /*Main Tree Logic*/
    public void populate() {
        
        legality(); // establish legality
        boolean childLeaves = false;
        if (!isLeaf) { // if I am not a leaf I create children
            if (depth == maxDepth-1) { // if I am one above max depth my children are leaves
                childLeaves = true;
            }
            int[] nextMove = new int[2];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (legalMoves[i][j]) {
                        childGameState = gameState.clone();
                        if (playerNum == 1) {
                            childGameState[i][j] = 'x';
                            nextMove = new int[]{i,j};
                            // the child has reverse player num as it is the other player's move
                            children.add(new StateTree(childGameState, this, childLeaves, 2, depth+1, nextMove)); 
                        } else {
                            childGameState[i][j] = 'o';
                            nextMove = new int[]{i,j};
                            children.add(new StateTree(childGameState, this, childLeaves, 1, depth+1, nextMove));
                        }
                    }
                }
            }
        }
        // At this point children are established and the heuristic function is allowed to be called if not a leaf
        
        evaluate();
    }
    private void evaluate() {
        if (isLeaf) {
            // Terminal case
            leafHeuristic();
        } else {
            // Recursive case
            // ensure that all children have evaluated themselves
            for (StateTree child : children) {
                child.populate();
            }
            // Now evaluate yourself
            heuristic();
        }
        minMax();
    }
    private void minMax() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (legalMoves[i][j]) {
                    max = Integer.max(max, heuristicValue[i][j]);
                    min = Integer.min(min, heuristicValue[i][j]);
                    if (parent == null) {
                        if (playerNum == 1) {
                            if (max == heuristicValue[i][j]) {
                                move = new int[] {i,j};
                            }
                        }
                        if (playerNum == 2) {
                            if (min == heuristicValue[i][j]) {
                                move = new int[] {i,j};
                            }
                        }
                    }
                }
            }
        }
    }

    //Heuristic Expression
    private void leafHeuristic() {
        // establish the value of the board
        // set min and max to the value
        int boardValue = 0;
        // Our expression v = 1 or -1 if terminal state, v = 2X_2 + X_1 - (2O_2 + O_1)
        boardValue = checkTerminal();
        if (boardValue==0) {
            boardValue+=checkRows();
            boardValue+=checkCols();
            boardValue+=checkDiag();
        }
        min = boardValue;
        max = boardValue;
    }
    // Bubble up
    private void heuristic() {
        int[] childPos;
        // if player one heuristic for max
        if (playerNum == 1) {
            for (StateTree child : children) {
                childPos = child.getMove();
                heuristicValue[childPos[0]][childPos[1]] = child.max;
            }
        }
        // if player two heuristic for min
        else {
            for (StateTree child : children) {
                childPos = child.getMove();
                heuristicValue[childPos[0]][childPos[1]] = child.min;
            }
        }
    }

    private void legality() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameState[i][j] == ' ') {
                    legalMoves[i][j] = true;
                } else {
                    legalMoves[i][j] = false;
                }
            }
        }
    }

    // Helper Function returns the value of each row, column or diag
    private int checkRows() {
        int returnVal = 0; // value of all rows
        int holder; // value of current row
        char pChar = ' '; // char holder (player char)
        int j; // internal loop needs a prior declaration
        for (int i = 0; i < 3; i++) {
            holder = 0;
            j = 0;
            for (;j < 3; j++ ) { // find the first instance of a char in the row
                if (gameState[i][j]!=' ') {
                    pChar = gameState[i][j]; // if we find something other than empty set our char to it
                    break;
                }
            }
            if (pChar!=' ') { // if we found a char
                for (;j < 3; j++) { // iterate through the rest of the row
                    if (gameState[i][j] == pChar) {
                        holder++; // add a point for each char we see
                    } else if (gameState[i][j] != ' ' && gameState[i][j] != pChar) { // if we see the opposite char
                        holder = 0; // this row is worth nothing
                        j = 4; // no more loop
                    }
                }
            }
            if (pChar == 'o') {
                holder*=-1; // if the char was o reverse the points
            }
            pChar = ' '; // reset char holder
            returnVal+=holder; // add the holder to our rows value
        }
        return returnVal; // return the vlaue of all rows
    }
    // j and i in gamestate are switched from previous function --- make note if this really is a solution
    private int checkCols() {
        int returnVal = 0; // value of all cols
        int holder; // value of current col
        char pChar = ' '; // char holder (player char)
        int j; // internal loop needs a prior declaration
        for (int i = 0; i < 3; i++) {
            holder = 0;
            j = 0;
            for (;j < 3; j++ ) { // find the first instance of a char in the col
                if (gameState[j][i]!=' ') {
                    pChar = gameState[j][i]; // if we find something other than empty set our char to it
                    break;
                }
            }
            if (pChar!=' ') { // if we found a char
                for (;j < 3; j++) { // iterate through the rest of the col
                    if (gameState[j][i] == pChar) {
                        holder++; // add a point for each char we see
                    } else if (gameState[j][i] != ' ' && gameState[j][i] != pChar) { // if we see the opposite char
                        holder = 0; // this col is worth nothing
                        j = 4; // no more loop
                    }
                }
            }
            if (pChar == 'o') {
                holder*=-1; // if the char was o reverse the points
            }
            pChar = ' '; // reset char holder
            returnVal+=holder; // add the holder to our cols value
        }
        return returnVal; // return the vlaue of all cols
    }
    private int checkDiag() {
        int returnVal = 0;
        return returnVal;
    }
    private int checkTerminal() {
        int returnVal = 0;
        // return 1 if x, -1 if o, 0 if nonterminal
        return returnVal;
    }
}