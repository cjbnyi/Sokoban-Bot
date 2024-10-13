package solver;
import java.util.*;

public class SokoBot {
  ArrayList<int[]> goalList = new ArrayList<>();
  ArrayList<int[]> wallList = new ArrayList<>();
  char mapData[][];
  int width, height;

  public void initMap(char[][] mapData) {
    int i, j;

    for (i = 0; i < height; i++) {
      for (j = 0; j < width; j++) {
        if (mapData[i][j] == '#') {
          wallList.add(new int[] {i, j});
        }
        if (mapData[i][j] == '.' || mapData[i][j] == '*') {
          goalList.add(new int[] {i, j});
        }
      }
    }
  }

  public int getManhattan(int[] source) {
    ArrayList<Integer> distances = new ArrayList<>();
    for (int[] goal : this.goalList) {
      int distance = Math.abs(goal[0] - source[0]) + Math.abs(goal[1] - source[1]);
      distances.add(distance);
    }
    return Collections.min(distances);
  }

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

  public boolean isItem(int x, int y, char[][] itemsData, char[][] mapData) {
    return itemsData[x][y] == '$' || mapData[x][y] == '#';
  }

  public boolean isBlocked(int x, int y, char[][] itemsData, char[][] mapData) {
    return (isItem(x, y + 1, itemsData, mapData) && isItem(x + 1, y, itemsData, mapData)) ||
           (isItem(x, y + 1, itemsData, mapData) && isItem(x - 1, y, itemsData, mapData)) ||
           (isItem(x, y - 1, itemsData, mapData) && isItem(x + 1, y, itemsData, mapData)) ||
           (isItem(x, y - 1, itemsData, mapData) && isItem(x - 1, y, itemsData, mapData));
  }

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

  public int checkState(int playerPosition[], char[][] itemsData, char action) {
    int x = playerPosition[0];
    int y = playerPosition[1];
    
    switch (action) {
        case 'r':
            if (y >= this.width - 1 || mapData[x][y + 1] == '#') {
              return -1;
            }
        
            if (y < this.width - 1 && itemsData[x][y + 1] == ' ') {
                moveOne(playerPosition, itemsData, action);
                return getHeuristic(itemsData);
            } else if (y < this.width - 2 && itemsData[x][y + 1] == '$' && 
                       mapData[x][y + 2] != '#' && itemsData[x][y + 2] != '$') {
                moveTwo(playerPosition, itemsData, action);
                
                return getHeuristic(itemsData);
                
            }
            break;
        case 'l':

        if (y <= 0 || mapData[x][y - 1] == '#') {
          return -1;
        }

            if (y > 0 && itemsData[x][y - 1] == ' ') {
                moveOne(playerPosition, itemsData, action);
                return getHeuristic(itemsData);
            } else if (y > 1 && itemsData[x][y - 1] == '$' && 
                       mapData[x][y - 2] != '#' && itemsData[x][y - 2] != '$') {
                moveTwo(playerPosition, itemsData, action);
                
                return getHeuristic(itemsData);
            }
            break;
        case 'd':

        if (x >= this.height - 1 || mapData[x + 1][y] == '#') {
          return -1;
        }

            if (x < height - 1 && itemsData[x + 1][y] == ' ') {
                moveOne(playerPosition, itemsData, action);
                return getHeuristic(itemsData);
            } else if (x < height - 2 && itemsData[x + 1][y] == '$' && 
                       mapData[x + 2][y] != '#' && itemsData[x + 2][y] != '$') {
                moveTwo(playerPosition, itemsData, action);
                
                return getHeuristic(itemsData);
            
            }
            break;
        case 'u':

        if (x <= 0 || mapData[x - 1][y] == '#') {
          return -1;
        }

            if (x > 0 && itemsData[x - 1][y] == ' ') {
                moveOne(playerPosition, itemsData, action);
                return getHeuristic(itemsData);
            } else if (x > 1 && itemsData[x - 1][y] == '$' && 
                       mapData[x - 2][y] != '#' && itemsData[x - 2][y] != '$') {
                moveTwo(playerPosition, itemsData, action);
       
                return getHeuristic(itemsData);
              
            }
            break;
    }

    return -1; // Invalid move
}

public boolean canAdd(SokoState sokoState, HashMap<String, Integer> explored) {
    String stateHash = getStateHash(sokoState.getItemsData());
    
    if (explored.containsKey(stateHash) && explored.get(stateHash) <= sokoState.getComparator()) {
        return false;
    }

    explored.put(stateHash, sokoState.getComparator());
    return true;
}

public String getStateHash(char[][] itemsData) {
    StringBuilder hashBuilder = new StringBuilder();
    for (int i = 0; i < height; i++) {
        hashBuilder.append(itemsData[i]);
    }
    return hashBuilder.toString();
}

public void enqueueState(PriorityQueue<SokoState> pq, HashMap<String, Integer> explored, SokoState sokoState) {
    char[] actions = {'r', 'd', 'l', 'u'}; 
    int heuristic;
    int tempPosition[];
    SokoState newState;

    for (char action : actions) {
        tempPosition = Arrays.copyOf(sokoState.getPlayerPosition(height, width), 2);
        char[][] tempItems = deepCopyItems(sokoState.getItemsData());

        heuristic = checkState(tempPosition, tempItems, action);
        if (heuristic == -1) continue;

        newState = new SokoState(tempItems, sokoState.getCost() + 1, heuristic, action, sokoState);
        
        if (canAdd(newState, explored)) {
            pq.add(newState);
        }
    }
}

public char[][] deepCopyItems(char[][] itemsData) {
    char[][] copy = new char[height][width];
    for (int i = 0; i < height; i++) {
        System.arraycopy(itemsData[i], 0, copy[i], 0, width);
    }
    return copy;
}

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


public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    StringBuilder finalMoves = new StringBuilder();
    Comparator<SokoState> comparator = Comparator.comparingInt(SokoState::getComparator);
    PriorityQueue<SokoState> pq = new PriorityQueue<>(comparator);
    HashMap<String, Integer> exploredStates = new HashMap<>();

    this.width = width;
    this.height = height; 
    this.mapData = mapData;

    initMap(mapData);

    int heuristic = getHeuristic(itemsData);
    SokoState currState = new SokoState(itemsData, 0, heuristic, 's', null);
    pq.add(currState);

    while (heuristic > 0 && !pq.isEmpty()) {
        currState = pq.poll();
        enqueueState(pq, exploredStates, currState);
        
        heuristic = getHeuristic(currState.getItemsData()); 
    }


    return getPath(finalMoves, currState);
}
}
