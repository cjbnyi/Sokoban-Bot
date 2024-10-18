package solver;

public class SokoState {
    private char itemsData[][];
    // private int cost;
    private int heuristic;
    private char action;
    private SokoState prevState;

    SokoState(char itemsData[][], int heuristic, char action, SokoState prevState) {
        this.itemsData = new char[itemsData.length][itemsData[0].length];
        for (int i = 0; i < itemsData.length; i++)
            for (int j = 0; j < itemsData[i].length; j++)
                this.itemsData[i][j] = itemsData[i][j];

        // this.cost = cost;
        this.heuristic = heuristic;
        this.action = action;
        this.prevState = prevState;
    }

    public char[][] getItemsData() {
        return itemsData;
    }

    public int[] getPlayerPosition(int height, int width) {
        int playerPosition[] = new int[2];
        int i, j;

        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {
              if (itemsData[i][j] == '@') {
                playerPosition[0] = i;
                playerPosition[1] = j;
              }
            }
        }

        return playerPosition;
    }

    /*
    public int getCost() {
        return cost;
    }

     */

    public int getHeuristic() {
        return heuristic;
    }

    /**
     * A* implementation
     * f(n) = g(n) + h(n)
     * @return
     */
    public int getComparator() {
        return heuristic;
    }

    public char getAction() {
        return action;
    }

    public SokoState getPrevState() {
        return prevState;
    }
}
