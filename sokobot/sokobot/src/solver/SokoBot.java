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
        for (i = 0; i < boxes.length; i++) {
          totalHeuristic += getManhattan(boxes[i]);
        }

        return totalHeuristic;
    }

    /*
        Determines whether row x, column y has an item
    */
    public boolean isItem(int x, int y, char[][] itemsData, char[][] mapData) {
        return itemsData[x][y] == '$' || mapData[x][y] == '#';
    }

    /*
        Determines whether a box
        NOTE: This method is unnecessary, as we can code a general preprocessing method that determines
              all possible 'dead' positions and not just specific corner cases.
    */
    public boolean isBlocked(int x, int y, char[][] itemsData, char[][] mapData) {
        return (isItem(x, y + 1, itemsData, mapData) && isItem(x + 1, y, itemsData, mapData)) ||
               (isItem(x, y + 1, itemsData, mapData) && isItem(x - 1, y, itemsData, mapData)) ||
               (isItem(x, y - 1, itemsData, mapData) && isItem(x + 1, y, itemsData, mapData)) ||
               (isItem(x, y - 1, itemsData, mapData) && isItem(x - 1, y, itemsData, mapData));
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
        Unnecessary, same comments as isBlocked()
    */
    public boolean blockedBox(char[][] itemsData, char[][] mapData) {

        int i, j;

        for (i = 0; i < this.height; i++) {
          for (j = 0; j < this.width; j++) {
            if (itemsData[i][j] == '$' && isBlocked(i, j, itemsData, mapData)) {
              return true;
            }
          }
        }

        return false;
    }

    /*
        Checks whether an action can be performed given a game state

        NOTE: You might have to revise check state because it does not return info about the move done.

        For each move type, we have the following possible return values:
        -1: Player's move sends them out of bounds
        1: Player's move sends them to an empty space
        2: Player's move runs into a crate
    */
    public int checkState(int playerPosition[], char[][] itemsData, char action) {

        int x = playerPosition[0]; // can be confusing since x is associated with the horizontal axis
        int y = playerPosition[1]; // better labeled row and column, correspondingly

        switch (action) {
            case 'r':
                if (y >= this.width - 1 || mapData[x][y + 1] == '#') {
                    return INVALID_MOVE;
                }
                if (y < this.width - 1 && itemsData[x][y + 1] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (y < this.width - 2 && itemsData[x][y + 1] == '$' &&
                           mapData[x][y + 2] != '#' && itemsData[x][y + 2] != '$') {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'l':
                if (y <= 0 || mapData[x][y - 1] == '#') {
                    return INVALID_MOVE;
                }
                if (y > 0 && itemsData[x][y - 1] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (y > 1 && itemsData[x][y - 1] == '$' &&
                           mapData[x][y - 2] != '#' && itemsData[x][y - 2] != '$') {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'd':
                if (x >= this.height - 1 || mapData[x + 1][y] == '#') {
                    return INVALID_MOVE;
                }
                if (x < height - 1 && itemsData[x + 1][y] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (x < height - 2 && itemsData[x + 1][y] == '$' &&
                           mapData[x + 2][y] != '#' && itemsData[x + 2][y] != '$') {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            case 'u':
                if (x <= 0 || mapData[x - 1][y] == '#') {
                    return INVALID_MOVE;
                }
                if (x > 0 && itemsData[x - 1][y] == ' ') {
                    moveOne(playerPosition, itemsData, action);
                    return NORMAL_MOVE;
                }
                if (x > 1 && itemsData[x - 1][y] == '$' &&
                           mapData[x - 2][y] != '#' && itemsData[x - 2][y] != '$') {
                    moveTwo(playerPosition, itemsData, action);
                    return PUSH_MOVE;
                }
                break;

            default:
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

        // can also be confusing because method just checks if it can be added but it gets added too
        // can be transferred to enqueuePossiblePushes()
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
        TODO: Add base case where state has been explored.
        Status: DONE!! Verify.
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

        // int newCost = sokoState.getCost() + 1;
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
        Status: DONE!! Verify
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
            // System.out.println("Test! " + "Heuristic: " + heuristic + " | PQueue size: " + pq.size()); // DEBUG
        }

        return getPath(finalMoves, currState);
    }
}
