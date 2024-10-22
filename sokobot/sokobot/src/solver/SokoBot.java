package solver;
import java.util.*;

public class SokoBot {
    ArrayList<int[]> goalList = new ArrayList<>();
    ArrayList<int[]> wallList = new ArrayList<>();
    char mapData[][];
    int width, height;

    // needed to order states in a PQueue
    Comparator<SokoState> comparator = Comparator.comparingInt(SokoState::getComparator);
    // stores unexplored states in order of comparator value
    PriorityQueue<SokoState> pq = new PriorityQueue<>(comparator);
    // stores explored states as String and their corresponding heuristic value as Integer
    HashMap<String, Integer> exploredStates = new HashMap<>();

    // constants
    private final char[] actions = {'r', 'd', 'l', 'u'};
    private final int INVALID_MOVE = -1;
    private final int NORMAL_MOVE = 1;
    private final int PUSH_MOVE = 2;

    /*
        Extracts goal and wall coordinates from char mapData[][]
    */
    public void initMap(char[][] mapData) {
        int i, j;

        for (i = 0; i < height; i++) {
          for (j = 0; j < width; j++) {
            if (mapData[i][j] == '#') {
              wallList.add(new int[] {i, j});
            }
            else if (mapData[i][j] == '.' || mapData[i][j] == '*') {
              goalList.add(new int[] {i, j});
            }
          }
        }
    }

    /*
        Computes the Manhattan distance of a box (given its coordinates) and its closest goal.
    */
    public int getManhattan(int[] source) {
        ArrayList<Integer> distances = new ArrayList<>();
        for (int[] goal : this.goalList) {
          int distance = Math.abs(goal[0] - source[0]) + Math.abs(goal[1] - source[1]); 
          distances.add(distance);
        }
        return Collections.min(distances);
    }

    /*
        Computes the sum of each box and its Manhattan distance to the closest goal.
    */
    public int getHeuristic(char itemsData[][]) {
        int[][] boxes = new int[goalList.size()][2];
        int i, j, count = 0;
        int totalHeuristic = 0;

        // Listing the coordinates of boxes and the player
        for (i = 0; i < height; i++) {
          for (j = 0; j < width; j++) {
            if (itemsData[i][j] == '$') {
              boxes[count][0] = i;
              boxes[count][1] = j;
              count++;
            }
          }
        }

        // Summing up the minimum Manhattan distances
        for (i = 0; i < count; i++) {
          totalHeuristic += getManhattan(boxes[i]);
        }

        return totalHeuristic;
    }

    /*
        Simulates a valid player move into an empty space
    */
    public void moveOne(int playerPosition[], char[][] itemsData, char action) {

        int x = playerPosition[0];
        int y = playerPosition[1];

        switch (action) {
          case 'r':
            itemsData[x][y + 1] = '@';
            itemsData[x][y] = ' ';
            playerPosition[1] = y + 1;
            break;
          case 'l':
            itemsData[x][y - 1] = '@';
            itemsData[x][y] = ' ';
            playerPosition[1] = y - 1;
            break;
          case 'u':
            itemsData[x - 1][y] = '@';
            itemsData[x][y] = ' ';
            playerPosition[0] = x - 1;
            break;
          case 'd':
            itemsData[x + 1][y] = '@';
            itemsData[x][y] = ' ';
            playerPosition[0] = x + 1;
            break;
        }
    }

    /*
        Simulates a valid player move into a pushable box
    */
    public void moveTwo(int playerPosition[], char[][] itemsData, char action) {

        int x = playerPosition[0];
        int y = playerPosition[1];

        switch (action) {
          case 'r':
            itemsData[x][y + 2] = '$';
            itemsData[x][y + 1] = '@';
            itemsData[x][y] = ' ';
            playerPosition[1] = y + 1;
            break;
          case 'l':
            itemsData[x][y - 2] = '$';
            itemsData[x][y - 1] = '@';
            itemsData[x][y] = ' ';
            playerPosition[1] = y - 1;
            break;
          case 'u':
            itemsData[x - 2][y] = '$';
            itemsData[x - 1][y] = '@';
            itemsData[x][y] = ' ';
            playerPosition[0] = x - 1;
            break;
          case 'd':
            itemsData[x + 2][y] = '$';
            itemsData[x + 1][y] = '@';
            itemsData[x][y] = ' ';
            playerPosition[0] = x + 1;
            break;
        }
    }

    /*
        Gets the dead spots of a map.
     */
    public boolean getDeadSpots(char[][] mapData, int i, int j) {
        //top, left, right, bot
        int[] rows = {1, 0, 0, -1};
        int[] cols = {0, -1, 1, 0};
        int top, left, right, bot;
        if(mapData[i][j] != '.'){
            //checks
            top=0;
            left=0;
            right=0;
            bot=0;

            // if including crates: || mapData[i+rows[2]][j+cols[2]] == '$'
            if(i+1 < this.width && mapData[i+rows[0]][j+cols[0]] == '#' ) bot=1;
            if(j-1 >=0 && mapData[i+rows[1]][j+cols[1]] == '#' ) left=1;
            if(j+1 < this.height && mapData[i+rows[2]][j+cols[2]] == '#' ) right=1;
            if(i-1 >= 0 && mapData[i+rows[3]][j+cols[3]] == '#' ) top=1;
            
            if(top+left == 2 || top+right == 2|| bot+left == 2|| bot+right == 2){
                return true;
            }
        }
        return false;
    }

    /*
        Checks whether an action can be performed given a game state

        For each move type, we have the following possible return values:
        -1: Player's move sends them out of bounds
        1: Player's move sends them to an empty space
        2: Player's move runs into a crate
    */
    public int checkState(int playerPosition[], char[][] itemsData, char action) {
        int row = playerPosition[0];
        int col= playerPosition[1];

        switch (action) {
            case 'r':
                if (col>= this.width - 1 || mapData[row][col+ 1] == '#') {
                    return INVALID_MOVE;
                }
                if (col< this.width - 1 && itemsData[row][col+ 1] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (col< this.width - 2 && itemsData[row][col+ 1] == '$' &&
                           mapData[row][col + 2] != '#' && itemsData[row][col+ 2] != '$' &&
                           mapData[row][col + 2] != 'X' &&
                           getDeadSpots(mapData, row, col+2) == false) {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'l':
                if (col<= 0 || mapData[row][col- 1] == '#') {
                    return INVALID_MOVE;
                }
                if (col> 0 && itemsData[row][col- 1] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (col> 1 && itemsData[row][col- 1] == '$' &&
                           mapData[row][col- 2] != '#' && itemsData[row][col- 2] != '$'&&
                           getDeadSpots(mapData, row, col-2)== false) {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'd':
                if (row >= this.height - 1 || mapData[row + 1][col] == '#') {
                    return INVALID_MOVE;
                }
                if (row < height - 1 && itemsData[row + 1][col] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (row < height - 2 && itemsData[row + 1][col] == '$' &&
                           mapData[row + 2][col] != '#' && itemsData[row + 2][col] != '$'&&
                           getDeadSpots(mapData, row+2, col)== false) {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'u':
                if (row <= 0 || mapData[row - 1][col] == '#') {
                    return INVALID_MOVE;
                }
                if (row > 0 && itemsData[row - 1][col] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (row > 1 && itemsData[row - 1][col] == '$' &&
                           mapData[row - 2][col] != '#' && itemsData[row - 2][col] != '$'&&
                           getDeadSpots(mapData, row-2, col)== false) {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            default: // An error has occurred
                System.out.println("Move error!");
        }

        return INVALID_MOVE; // Invalid move
    }

    /*
        Checks whether a SokoState can be added given the current explored states
     */
    public boolean canAddState(SokoState sokoState) {

        String stateHash = getStateHash(sokoState.getItemsData());

        if (exploredStates.containsKey(stateHash) && exploredStates.get(stateHash) <= sokoState.getComparator()) {
            return false;
        }
        return true;
    }

    /*
        Converts char[][] itemsData (information about a state) into a String for the HashMap of explored states
     */
    public String getStateHash(char[][] itemsData) {
        StringBuilder hashBuilder = new StringBuilder();
        for (int i = 0; i < height; i++) {
            hashBuilder.append(itemsData[i]);
        }
        return hashBuilder.toString();
    }

    /*
        Gets all possible box pushes from a given state.
     */
    public void getPossiblePushStates(ArrayList<SokoState> possiblePushStates, SokoState sokoState, char action) {

        int[] tempPlayerPosition = Arrays.copyOf(sokoState.getPlayerPosition(height, width), 2);
        char[][] tempItemsData = deepCopyItems(sokoState.getItemsData());

        int moveType = checkState(tempPlayerPosition, tempItemsData, action); // first two arguments mutated by action
        String stateHash = getStateHash(tempItemsData);

        if (moveType == INVALID_MOVE || exploredStates.containsKey(stateHash)) { // invalid move or move already explored, therefore, we terminate expansion
            // System.out.println("Invalid move!"); // DEBUG
            return;
        }

        int newHeuristic = getHeuristic(tempItemsData);
        SokoState newSokoState = new SokoState(tempItemsData, newHeuristic, action, sokoState);

        exploredStates.put(stateHash, sokoState.getComparator()); // prevents infinite recursion

        if (moveType == PUSH_MOVE) { // this is a push move, therefore, we add to push states and terminate expansion
            // System.out.println("Push move!"); // DEBUG
            possiblePushStates.add(newSokoState); // add to possible push states
            return;
        }

        // this is a normal move, therefore, we continue expansion
        for (char a : actions) {
            // System.out.println("Normal move!"); // DEBUG
            getPossiblePushStates(possiblePushStates, newSokoState, a);
        }
    }

    /*
        Checks all possible actions from a given SokoState and adds to PQueue pq if valid, given HashMap explored
     */
    public void enqueuePossiblePushes(SokoState sokoState) {

        ArrayList<SokoState> possiblePushStates = new ArrayList<SokoState>();

        for (char action : actions) {
            getPossiblePushStates(possiblePushStates, sokoState, action);
        }

        // System.out.println("Possible pushes: " + possiblePushStates.size()); // DEBUG

        for (SokoState pushState : possiblePushStates) {
            pq.add(pushState);
        }
    }

    /*
        Creates a deep copy of a char[][] itemsData
     */
    public char[][] deepCopyItems(char[][] itemsData) {
        char[][] copy = new char[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(itemsData[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    /*
        Gets the String representation of the set of moves of a solution
     */
    public String getPath(StringBuilder finalMoves, SokoState currState) {
      Stack<Character> path = new Stack<>();
      SokoState tempState = currState;

      // Backtrack through the solution path
      while (tempState != null && tempState.getAction() != 's') {
          path.push(tempState.getAction());
          tempState = tempState.getPrevState();
      }

      while (!path.isEmpty()) {
          finalMoves.append(path.pop());
      }

      return finalMoves.toString();
    }
    
    /*
        Main method that solves a Sokoban puzzle given its width, height, and data about its states
     */
    public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {

        StringBuilder finalMoves = new StringBuilder();

        this.width = width;
        this.height = height;
        this.mapData = mapData;

        initMap(mapData);

        int heuristic = getHeuristic(itemsData);
        SokoState currState = new SokoState(itemsData, heuristic, 's', null);
        pq.add(currState);

        while (heuristic > 0 && !pq.isEmpty()) {
            currState = pq.poll();
            enqueuePossiblePushes(currState);
            heuristic = getHeuristic(currState.getItemsData());
        }

        return getPath(finalMoves, currState);
    }
}
