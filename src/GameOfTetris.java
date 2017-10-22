/**
 * CS 6366.001 - Computer Graphics - F17
 * Instructor: Kang Zhang
 * Student: Binhan Wang (bxw161330)
 * <p>
 * Assignment 3
 *
 * This program fulfills all the requirements in the Assignment3 including bonus requirement:
 *
 * 1. Introducing Game Setting Dialog before each game, so use can adjust factors as needed.
 *
 * 2. In the Setting Dialog, user can change following gaming factors:
 *
 *      M – scoring factor (range: 1-10).
 *      N – number of rows required for each Level of difficulty (range: 20-50).
 *      S – speed factor (range: 0.1-1.0).
 *      Rows - number of row in main area.
 *      Columns - number of columns in main area.
 *      Block Size - adjust the size of block in the game.
 *                  (The block size adjustment is essentially changing canvas size, so during the game user can also drag the window to adjust)
 *
 * 3. Move mouse into falling shape will trigger a change shape action, which will replace the falling shape with a new one other that itself nor
 *    the next shape. However, this action won't be performed if the space in main area is not allowed for such replacement.
 *    Score will be deducted if change shape action takes place.
 *
 * 4. (Bonus) In the Setting Dialog, user can choose from 6 extra shapes to be added into random pool, so that user will encounter
 *    the selected shape during the game.
 */

import sun.security.krb5.internal.PAData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * GameOfTetris served as application logic class
 * Implementing singleton pattern for easy access to application data
 */

public class GameOfTetris {

    /**
     * Static Fields
     */

    // Main area
    final static int DEFAULT_MAINAREA_WIDTH = 10;
    final static int DEFAULT_MAINAREA_HEIGHT = 20;
    final static int MAX_MAINAREA_WIDTH = 20;
    final static int MAX_MAINAREA_HEIGHT = 30;

    static int MAINAREA_WIDTH = DEFAULT_MAINAREA_WIDTH;
    static int MAINAREA_HEIGHT = DEFAULT_MAINAREA_HEIGHT;

    // UI properties

    final static int DEFAULT_BLOCKSIZE = 30;
    final static int MAX_BLOCKSIZE = 40;
    static int BLOCKSIZE = DEFAULT_BLOCKSIZE;
    final static int MARGIN = 10;


    static int frameWidth() {
        return BLOCKSIZE * (MAINAREA_WIDTH + 5) + 3 * MARGIN;
    }

    static int frameHeight() {
        return BLOCKSIZE * MAINAREA_HEIGHT + MARGIN * 2;
    }

    // Gaming
    final static int BASE_TIMER_INTERVAL = 500;

    static int LEVEL = 1;
    static int LINE = 0;
    static int SCORE = 0;

    // Factors
    final static int M_MIN = 1;
    final static int M_MAX = 10;
    static int M = M_MIN; // M – scoring factor (range: 1-10).

    final static int N_MIN = 20;
    final static int N_MAX = 50;
    static int N = N_MIN; // N – number of rows required for each Level of difficulty (range: 20-50).

    final static float S_MIN = 0.1f;
    final static float S_MAX = 1.0f;
    final static float S_SCALE = 10f;
    static float S = S_MIN; // S – speed factor (range: 0.1-1.0).


    /**
     * Main function as application entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        GameOfTetris.getInstance().showNewGameDialog();
    }

    /**
     * Some sub class for the application logic
     */
    // class for timer task
    static class GameTimerTask extends TimerTask {
        GameTimerTask() {

        }

        @Override
        public void run() {
            System.out.println("Timer tick!");
            if (GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.MoveDown))
                GameOfTetris.getInstance().canvas.repaint();
        }
    }

    // class for Tetromino

    static class Tetromino {

        public enum Action_Type {
            MoveLeft,
            MoveRight,
            MoveDown,
            RotateLeft,
            RotateRight,
            Change;
        }

        private enum Tetromino_Type {
            Tetromino_S(new Color(255, 250, 0), new boolean[][]{
                    {false, true, true},
                    {true, true, false},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_Z(new Color(91, 47, 139), new boolean[][]{
                    {true, true, false},
                    {false, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_J(new Color(0, 93, 175), new boolean[][]{
                    {true, false, false},
                    {true, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_L(new Color(255, 33, 0), new boolean[][]{
                    {false, false, true},
                    {true, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_O(new Color(0, 153, 62), new boolean[][]{
                    {false, true, true, false},
                    {false, true, true, false},
                    {false, false, false, false},
                    {false, false, false, false}
            }, new int[]{3, -2}),
            Tetromino_T(new Color(255, 174, 0), new boolean[][]{
                    {false, true, false},
                    {true, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_I(new Color(0, 159, 236), new boolean[][]{
                    {false, false, false, false},
                    {true, true, true, true},
                    {false, false, false, false},
                    {false, false, false, false}
            }, new int[]{3, -2}),
            // Extra Shapes
            Tetromino_E1(new Color(155, 155, 155), new boolean[][]{
                    {false, true, false},
                    {true, true, false},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_E2(new Color(135, 201, 71), new boolean[][]{
                    {true, false, false},
                    {false, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_E3(new Color(212, 138, 137), new boolean[][]{
                    {false, false, false},
                    {true, true, true},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_E4(new Color(223, 96, 13), new boolean[][]{
                    {false, true, false},
                    {true, false, false},
                    {false, false, false}
            }, new int[]{3, -2}),
            Tetromino_E5(new Color(43, 121, 144), new boolean[][]{
                    {false, false, true},
                    {false, true, false},
                    {true, false, false}
            }, new int[]{3, -2}),
            Tetromino_E6(new Color(136, 126, 73), new boolean[][]{
                {false, true, false},
                {true, false, true},
                {false, false, false}
            }, new int[]{3, -2});

            private Color color;
            private boolean[][] initRelativePos;
            private int[] initAnchor;

            Tetromino_Type(Color color, boolean[][] initRelativePos, int[] initAnchor) {
                this.color = color;
                this.initRelativePos = initRelativePos;
                this.initAnchor = initAnchor;
            }

            public Color getColor() {
                return color;
            }

            /**
             * Pick a random value of the Tetromino_Type enum.
             *
             * @return a random Tetromino_Type.
             */
            public static Tetromino_Type getRandomType() {
                Random random = new Random();
                return values()[random.nextInt(values().length)];
            }

            /**
             * Get a deep copy of initRelativePos
             *
             * @return
             */
            public boolean[][] getInitRelativePos() {
                boolean[][] initPosCopy = new boolean[initRelativePos.length][];
                for (int i = 0; i < initRelativePos.length; i++)
                    initPosCopy[i] = Arrays.copyOf(initRelativePos[i], initRelativePos[i].length);
                return initPosCopy;
            }

            /**
             * Get a deep copy of InitAnchor
             *
             * @return
             */
            public int[] getInitAnchor() {
                return Arrays.copyOf(initAnchor, initAnchor.length);
            }
        }

        private Tetromino_Type type;
        private int[] anchor;
        private boolean[][] relativePos;

        static Set<Tetromino_Type> blockedTypes = new HashSet<>(Arrays.asList(
                Tetromino_Type.Tetromino_E1,
                Tetromino_Type.Tetromino_E2,
                Tetromino_Type.Tetromino_E3,
                Tetromino_Type.Tetromino_E4,
                Tetromino_Type.Tetromino_E5,
                Tetromino_Type.Tetromino_E6));

        Color color;

        Tetromino() {
            do {
                type = Tetromino_Type.getRandomType();
            }while (blockedTypes.contains(type));
            relativePos = type.getInitRelativePos();
            anchor = type.getInitAnchor();
            color = type.getColor();
        }

        public static int[][] generateBlockPos(int[] anchor, boolean[][] relativePos) {
            int[][] blockPos = new int[4][2]; // at most 4 blocks
            int idx = 0;
            for (int i = 0; i < relativePos.length; i++) {
                for (int j = 0; j < relativePos[i].length; j++)
                    if (relativePos[i][j]) {
                        blockPos[idx][0] = j + anchor[0];
                        blockPos[idx++][1] = i + anchor[1];
                    }
            }

            int[][]result = new int[idx][2];
            for(int i=0;i<idx;i++)
                result[i] = blockPos[i];
            return result;
        }

        public void setAnchor(int[] anchor) {
            this.anchor = anchor;
        }

        public int[] getAnchor() {
            return Arrays.copyOf(anchor, anchor.length);
        }

        public void setRelativePos(boolean[][] relativePos) {
            this.relativePos = relativePos;
        }

        public boolean[][] getRelativePos() {
            boolean[][] relativePosCopy = new boolean[relativePos.length][];
            for (int i = 0; i < relativePos.length; i++)
                relativePosCopy[i] = Arrays.copyOf(relativePos[i], relativePos[i].length);
            return relativePosCopy;
        }

        public int[][] getBlocksPos() {
            return generateBlockPos(anchor, relativePos);
        }
    }

    // Application logic singleton object
    private static final GameOfTetris instance = new GameOfTetris();

    // static UI properties
    CvGameOfTetris canvas;
    GameOfTetrisFrame frame;
    JDialog dialog;

    // application properties
    private boolean pause;
    private Timer timer;
    private Color[][] mainAreaData;
    private Tetromino fallingTetr;
    private Tetromino nextTetr;
    private boolean isPlaying;
    private boolean isGameOver;

    private GameOfTetris() {
        // Init UI element
        isPlaying = false;
        isGameOver = false;

        mainAreaData = new Color[MAINAREA_WIDTH][MAINAREA_HEIGHT];
        canvas = new CvGameOfTetris();
        frame = new GameOfTetrisFrame(canvas);
    }

    public static synchronized GameOfTetris getInstance() {
        return instance;
    }

    /**
     * Game Controls
     */
    void startGame() {
        System.out.println("Game Start!");
        // trigger timer
        pause = true;
        setPause(false);

        LEVEL = 1;
        SCORE = 0;
        LINE = 0;

        mainAreaData = new Color[MAINAREA_WIDTH][MAINAREA_HEIGHT];

        fallingTetr = new Tetromino();
        nextTetr = new Tetromino();
        isPlaying = true;
        isGameOver = false;

        frame.setSize(frameWidth(), frameHeight());
        canvas.repaint();
    }

    void endGame() {
        System.out.println("End Game!");
        isPlaying = false;
        setPause(true);
        fallingTetr = null;

        // show new game
        canvas.repaint();
    }

    /**
     * Getters & Setters
     */

    // property setter which also help to trigger timer
    public void setPause(boolean pause) {
        if (pause != this.pause) {
            this.pause = pause;
            if (pause) {
                System.out.println("Timer stop!");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            } else {
                System.out.println("Timer start!");
                timer = new Timer();
                //FS = FS x (1 + Level  x S).
                float interval = (float) BASE_TIMER_INTERVAL / (1f + S * (float) LEVEL);
                timer.scheduleAtFixedRate(new GameTimerTask(), (int) interval, (int) interval);
            }
        }
    }

    public Color[][] getMainAreaData() {
        return mainAreaData;
    }

    public Tetromino getFallingTetr() {
        return fallingTetr;
    }

    public Tetromino getNextTetr() {
        return nextTetr;
    }

    public boolean hitTest(int[][] blocks) {
        for (int i = 0; i < blocks.length; i++) {
            int x = blocks[i][0];
            int y = blocks[i][1];
            if (!(x >= 0 && x < MAINAREA_WIDTH && y < MAINAREA_HEIGHT)) {
                return false;// hit main area boundaries
            } else if (insideMainArea(x, y) && mainAreaData[x][y] != null) {
                return false;// hit existing block
            }
        }
        return true;
    }

    /**
     * Helper function - test whether a point is inside main area
     *
     * @param x
     * @param y
     * @return
     */
    public boolean insideMainArea(int x, int y) {
        return x >= 0 && x < MAINAREA_WIDTH && y >= 0 && y < MAINAREA_HEIGHT;
    }

    /**
     * Helper function - Rotate an boolean matrix in-place
     *
     * @param mat
     * @param clockwise
     */
    public static void rotateMatrix(boolean mat[][], boolean clockwise) {
        int N = mat.length;
        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++) {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N - x - 1; y++) {
                if (clockwise) {
                    // store current cell in temp variable
                    boolean temp = mat[x][y];

                    // move values from left to top
                    mat[x][y] = mat[N - 1 - y][x];

                    // move values from bottom to left
                    mat[N - 1 - y][x] = mat[N - 1 - x][N - 1 - y];

                    // move values from right to bottom
                    mat[N - 1 - x][N - 1 - y] = mat[y][N - 1 - x];

                    // assign temp to right
                    mat[y][N - 1 - x] = temp;
                } else {
                    // store current cell in temp variable
                    boolean temp = mat[x][y];

                    // move values from right to top
                    mat[x][y] = mat[y][N - 1 - x];

                    // move values from bottom to right
                    mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y];

                    // move values from left to bottom
                    mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x];

                    // assign temp to left
                    mat[N - 1 - y][x] = temp;
                }
            }
        }
    }

    /**
     * Action Handling
     *
     * @param action
     * @return whether should repaint
     */
    public boolean moveAction(Tetromino.Action_Type action) {
        if (fallingTetr == null)
            return false;

        boolean[][] newRelativePos = fallingTetr.getRelativePos();
        int[] newAnchor = fallingTetr.getAnchor();

        // for change action
        Tetromino t = null;

        // set new metrics
        switch (action) {
            case MoveDown:
                newAnchor[1] += 1;
                break;
            case MoveLeft:
                newAnchor[0] -= 1;
                break;
            case MoveRight:
                newAnchor[0] += 1;
                break;
            case RotateLeft:
                if (fallingTetr.type != Tetromino.Tetromino_Type.Tetromino_O)
                    rotateMatrix(newRelativePos, true);
                break;
            case RotateRight:
                if (fallingTetr.type != Tetromino.Tetromino_Type.Tetromino_O)
                    rotateMatrix(newRelativePos, false);
                break;
            case Change:
                t = new Tetromino();
                while (t.type == fallingTetr.type || t.type == nextTetr.type)
                    t = new Tetromino();
                t.anchor = fallingTetr.anchor;
                newRelativePos = t.getRelativePos();
                break;

            default:
                break;
        }

        int[][] newBlocksPos = Tetromino.generateBlockPos(newAnchor, newRelativePos);

        if (hitTest(newBlocksPos)) {
            if (action == Tetromino.Action_Type.Change) {
                fallingTetr = t;
                SCORE -= LEVEL * M;
            } else {
                fallingTetr.setAnchor(newAnchor);
                fallingTetr.setRelativePos(newRelativePos);
            }
            return true;
        } else {
            if (action == Tetromino.Action_Type.MoveDown) {
                // merge fallingTetr into main area data
                boolean endOfGame = false;
                int[][] blocksPos = fallingTetr.getBlocksPos();
                PriorityQueue<Integer> rowsToRemove = new PriorityQueue<>(MAINAREA_HEIGHT);
                for (int i = 0; i < blocksPos.length; i++) {
                    int x = blocksPos[i][0];
                    int y = blocksPos[i][1];
                    if (!insideMainArea(x, y))
                        endOfGame = true;
                    else {
                        // merge color block to maiArea
                        mainAreaData[x][y] = fallingTetr.color;

                        // add rows to remove
                        boolean hasHole = false;
                        for (int j = 0; j < MAINAREA_WIDTH; j++) {
                            if (mainAreaData[j][y] == null) {
                                hasHole = true;
                                break;
                            }
                        }
                        if (!hasHole)
                            rowsToRemove.add(y);
                    }
                }
                if (!endOfGame) {
                    fallingTetr = nextTetr;
                    nextTetr = new Tetromino();
                    // remove rows if necessary
                    if (rowsToRemove.size() > 0) {
                        removeRows(rowsToRemove);
                    }
                    // need to repaint for row removal
                    return true;
                } else {
                    //Game Over
                    System.out.println("Game Over!");
                    isGameOver = true;
                    endGame();
                }
            }

            return false;
        }
    }

    private void removeRows(PriorityQueue<Integer> rows) {

        // Already in descending order
        for (Integer row : rows) {
            for (int r = row - 1; r >= 0; r--) {
                for (int c = 0; c < MAINAREA_WIDTH; c++)
                    mainAreaData[c][r + 1] = mainAreaData[c][r];
            }
            for (int c = 0; c < MAINAREA_WIDTH; c++)
                mainAreaData[c][0] = null;
        }

        SCORE += rows.size() * (LEVEL * M);
        LINE += rows.size();

        if (LINE >= N) {
            LEVEL++;
            LINE -= N;
            setPause(true);
            setPause(false);
        }

    }

    /**
     * Dialogs
     */
    private void repaintDialog(){
        if(SwingUtilities.isEventDispatchThread()){
            dialog.revalidate();
            dialog.repaint();
            dialog.pack();
        }
        else {
            try{
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        repaintDialog();
                    }
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void showNewGameDialog() {

        if (SwingUtilities.isEventDispatchThread()) {
            // Create a modal dialog
            dialog = new JDialog(frame, "New Game", true);

            // Use a flow layout
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            dialog.add(panel);

            {
                JPanel row = new JPanel();
                row.setLayout(new FlowLayout(FlowLayout.LEADING));
                row.add(new JLabel("Game Setting:"));
                panel.add(row);
            }

            {
                JPanel topPanel = new JPanel();
                topPanel.setLayout(new GridBagLayout());

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridy = 0;
                c.insets = new Insets(10, 10, 10, 10);

                // Factors
                {
                    JPanel factorPanel = new JPanel();
                    factorPanel.setLayout(new BoxLayout(factorPanel, BoxLayout.Y_AXIS));
                    factorPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.lightGray));

                    factorPanel.add(new JLabel("Factors:"));

                    // Scoring Factor M
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.LEADING));

                        row.add(new JLabel("M:"));

                        JLabel v = new JLabel(String.format("%02d", M));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, M_MIN, M_MAX, M);
                        slider.setMajorTickSpacing(3);
                        slider.setMinorTickSpacing(1);
                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                M = slider.getValue();
                                v.setText(String.format("%02d", M));
                            }
                        });
                        row.add(slider);
                        row.add(v);
                        factorPanel.add(row);
                    }

                    // Difficulty Factor N
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.LEADING));

                        row.add(new JLabel("N:"));

                        JLabel v = new JLabel(String.format("%02d", N));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, N_MIN, N_MAX, N);
                        slider.setMajorTickSpacing(5);
                        slider.setMinorTickSpacing(1);
                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                N = slider.getValue();
                                v.setText(String.format("%02d", N));
                            }
                        });
                        row.add(slider);
                        row.add(v);

                        factorPanel.add(row);
                    }

                    // Speed Factor S
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.LEADING));

                        row.add(new JLabel("S:"));

                        JLabel v = new JLabel(String.format("%.1f", S));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, (int) (S_MIN * S_SCALE), (int) (S_MAX * S_SCALE), (int) (S * S_SCALE));
                        slider.setMajorTickSpacing(3);
                        slider.setMinorTickSpacing(1);

                        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
                        labelTable.put((int) (S_MIN * S_SCALE), new JLabel(String.format("%.1f", S_MIN)));
                        labelTable.put((int) (S_MAX * S_SCALE / 2), new JLabel(String.format("%.1f", S_MAX / 2)));
                        labelTable.put((int) (S_MAX * S_SCALE), new JLabel(String.format("%.1f", S_MAX)));
                        slider.setLabelTable(labelTable);

                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                S = (float) slider.getValue() / S_SCALE;
                                v.setText(String.format("%.1f", S));
                            }
                        });
                        row.add(slider);
                        row.add(v);
                        factorPanel.add(row);
                    }
                    c.gridx = 0;
                    topPanel.add(factorPanel, c);
                }

                // Main Area Settings
                {
                    JPanel mainAreaPanel = new JPanel();
                    mainAreaPanel.setLayout(new BoxLayout(mainAreaPanel, BoxLayout.Y_AXIS));
                    mainAreaPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.lightGray));

                    mainAreaPanel.add(new JLabel("Main Area:"));

                    // Number of Row
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.TRAILING));

                        row.add(new JLabel("Rows:"));

                        JLabel v = new JLabel(String.format("%02d", MAINAREA_HEIGHT));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, DEFAULT_MAINAREA_HEIGHT, MAX_MAINAREA_HEIGHT, MAINAREA_HEIGHT);
                        slider.setMajorTickSpacing(5);
                        slider.setMinorTickSpacing(1);
                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                MAINAREA_HEIGHT = slider.getValue();
                                v.setText(String.format("%02d", MAINAREA_HEIGHT));
                            }
                        });
                        row.add(slider);
                        row.add(v);
                        mainAreaPanel.add(row);
                    }


                    // Number of Columns
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.TRAILING));

                        row.add(new JLabel("Columns:"));

                        JLabel v = new JLabel(String.format("%02d", MAINAREA_WIDTH));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, DEFAULT_MAINAREA_WIDTH, MAX_MAINAREA_WIDTH, MAINAREA_WIDTH);
                        slider.setMajorTickSpacing(5);
                        slider.setMinorTickSpacing(1);
                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                MAINAREA_WIDTH = slider.getValue();
                                v.setText(String.format("%02d", MAINAREA_WIDTH));
                            }
                        });
                        row.add(slider);
                        row.add(v);
                        mainAreaPanel.add(row);
                    }

                    // Block Size
                    {
                        JPanel row = new JPanel();
                        row.setLayout(new FlowLayout(FlowLayout.TRAILING));

                        row.add(new JLabel("Block Size:"));

                        JLabel v = new JLabel(String.format("%02d", BLOCKSIZE));

                        JSlider slider = new JSlider(JSlider.HORIZONTAL, DEFAULT_BLOCKSIZE, MAX_BLOCKSIZE, BLOCKSIZE);
                        slider.setMajorTickSpacing(5);
                        slider.setMinorTickSpacing(1);
                        slider.setPaintTicks(true);
                        slider.setPaintLabels(true);
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                BLOCKSIZE = slider.getValue();
                                v.setText(String.format("%02d", BLOCKSIZE));
                                repaintDialog();
                            }
                        });
                        row.add(slider);
                        row.add(v);

                        c.gridx = 1;
                        mainAreaPanel.add(row);
                    }
                    topPanel.add(mainAreaPanel, c);
                }
                panel.add(topPanel);
            }

            // Custom Shape
            {
                JPanel customShapePanel = new JPanel();
                customShapePanel.setLayout(new GridBagLayout());

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(5, 5, 5, 5);

                {
                    {
                        c.gridx = 0; c.gridy = 0;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E1;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }

                    {
                        c.gridx = 1; c.gridy = 0;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E2;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }

                    {
                        c.gridx = 0; c.gridy = 1;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E3;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }

                    {
                        c.gridx = 1; c.gridy = 1;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E4;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }

                    {
                        c.gridx = 0; c.gridy = 2;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E5;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }

                    {
                        c.gridx = 1; c.gridy = 2;
                        Tetromino.Tetromino_Type type = Tetromino.Tetromino_Type.Tetromino_E6;
                        JPanel element = new JPanel();
                        element.setLayout(new FlowLayout());
                        element.add(new ShapePanel(type));
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected(!Tetromino.blockedTypes.contains(type));
                        cb.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!Tetromino.blockedTypes.contains(type))
                                    Tetromino.blockedTypes.add(type);
                                else
                                    Tetromino.blockedTypes.remove(type);
                            }
                        });
                        element.add(cb);
                        customShapePanel.add(element, c);
                    }
                }
                panel.add(customShapePanel);
            }

            // Buttons
            {
                JPanel btnPanel = new JPanel();
                btnPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

                // Create an Start button
                Button start = new Button("Start");
                start.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        startGame();
                        dialog.setVisible(false);
                    }
                });

                btnPanel.add(start);

                // Create an Quit button
                Button quit = new Button("Cancel");
                quit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (pause && !isGameOver && isPlaying) {
                            setPause(false);
                        }
                        dialog.setVisible(false);
                    }
                });

                btnPanel.add(quit);
                panel.add(btnPanel);
            }

            // Show dialog
            dialog.pack();
            dialog.setVisible(true);
        } else {

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        showNewGameDialog();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ShapePanel extends JPanel {

        private Tetromino.Tetromino_Type type;

        public ShapePanel(Tetromino.Tetromino_Type type) {
            this.type = type;
        }

        public Dimension getPreferredSize() {
            return new Dimension(BLOCKSIZE * 3+1, BLOCKSIZE * 3+1);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int[][] pos = Tetromino.generateBlockPos(new int[]{0, 0}, type.getInitRelativePos());
            for (int i = 0; i < pos.length; i++) {
                int[] xy = pos[i];
                int x = xy[0], y = xy[1];
                g.setColor(type.color);
                g.fillRect(x * BLOCKSIZE, y * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
                g.setColor(Color.black);
                g.drawRect(x * BLOCKSIZE, y * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
            }
        }
    }

    /**
     * UI Classes
     */

    static class GameOfTetrisFrame extends Frame {
        GameOfTetrisFrame(CvGameOfTetris canvas) {
            super("Game Of Tetris");
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            setSize(frameWidth(), frameHeight());
            add("Center", canvas);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setVisible(true);
        }
    }

    static class CvGameOfTetris extends Canvas {

        // internal class for ui component
        class RectComponent {
            float x, y, w, h;
            Color borderColor = Color.black;
            Color fillColor = null;
            Color textColor = Color.black;
            String text;
            boolean hidden = false;
            List<RectComponent> subComponents = new ArrayList<>();

            public RectComponent() {
            }

            public RectComponent(float x, float y, float w, float h) {
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
            }

            public void addSubComponent(RectComponent component) {
                subComponents.add(component);
            }

            public boolean inside(float px, float py) {
                return px >= x && px <= x + w && py >= y && py <= y + h;
            }

            public float relativeX(float x) {
                return x + this.x;
            }

            public float relativeY(float y) {
                return y + this.y;
            }

            public float centerX() {
                return x + w / 2;
            }

            public float centerY() {
                return y + h / 2;
            }

            public void setCenter(float cx, float cy) {
                x = cx - w / 2;
                y = cy - h / 2;
            }
        }

        // Properties
        int originX = 0, originY = 0; // top-left left-to-right top-to-down coordination
        float pixelSize, rWidth, rHeight, blockSize, margin;
        Font textFont;
        // Components
        RectComponent mainArea, pauseLabel, gameOverLabel, nextShape, scorePanel, quitBtn, newGameBtn;
        List<RectComponent> fallingBlockComponents = new ArrayList<>();

        // Change flag
        boolean changeFlag = false;

        CvGameOfTetris() {
            mainArea = new RectComponent();
            pauseLabel = new RectComponent();
            gameOverLabel = new RectComponent();
            nextShape = new RectComponent();
            scorePanel = new RectComponent();
            quitBtn = new RectComponent();
            newGameBtn = new RectComponent();

            pauseLabel.hidden = true;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent evt) {

                    super.mousePressed(evt);

                    int btn = evt.getButton();
                    float xA = fx(evt.getX()), yA = fy(evt.getY());
                    if (btn == MouseEvent.BUTTON1) {
                        if (quitBtn != null && quitBtn.inside(xA, yA)) {
                            System.exit(0);
                        } else if (newGameBtn != null && newGameBtn.inside(xA, yA)) {
                            GameOfTetris.getInstance().setPause(true);
                            GameOfTetris.getInstance().showNewGameDialog();
                        } else if (!GameOfTetris.getInstance().pause && GameOfTetris.getInstance().isPlaying) {
                            GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.MoveLeft);
                            repaint();
                        }
                    } else if (btn == MouseEvent.BUTTON3) {
                        if (!GameOfTetris.getInstance().pause && GameOfTetris.getInstance().isPlaying) {
                            GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.MoveRight);
                            repaint();
                        }
                    }
                }
            });

            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent evt) {
                    super.mouseWheelMoved(evt);

                    // do not response to any event when game is not playing
                    if (!GameOfTetris.getInstance().isPlaying)
                        return;

                    if (GameOfTetris.getInstance().pause)
                        return;
                    if (evt.getWheelRotation() < 0) {
                        System.out.println("Rotated Up... " + evt.getWheelRotation());
                        if (GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.RotateLeft))
                            repaint();

                    } else {
                        System.out.println("Rotated Down... " + evt.getWheelRotation());
                        if (GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.RotateRight))
                            repaint();
                    }
                    System.out.println("ScrollAmount: " + evt.getScrollAmount());
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent evt) {
                    super.mouseMoved(evt);

                    // do not response to any event when game is not playing
                    if (!GameOfTetris.getInstance().isPlaying)
                        return;

                    float xA = fx(evt.getX()), yA = fy(evt.getY());
                    if (mainArea != null && pauseLabel != null) {
                        boolean shouldHidePause = !mainArea.inside(xA, yA);
                        if (shouldHidePause != pauseLabel.hidden) {
                            System.out.println("set pause hidden: " + shouldHidePause);
                            pauseLabel.hidden = shouldHidePause;
                            GameOfTetris.getInstance().setPause(!shouldHidePause);
                            repaint();
                        }
                    }

                    boolean setFlag = true;
                    for (RectComponent block : fallingBlockComponents) {
                        if (block.inside(xA, yA)) {
                            setFlag = false;
                            if (!changeFlag) {
                                if (GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.Change))
                                    repaint();
                                changeFlag = true;
                                break;
                            }
                        }
                    }
                    if (setFlag)
                        changeFlag = false;

                }
            });
        }

        void initgr() {
            Dimension d = getSize();
            int maxX = d.width - 1, maxY = d.height - 1;

            rWidth = (float) frameWidth();
            rHeight = (float) frameHeight();
            margin = fl(MARGIN); // Margin for layout component
            blockSize = fl(BLOCKSIZE);

            pixelSize = Math.max(rWidth / maxX, rHeight / maxY);

            textFont = new Font("Dialog", Font.BOLD, iL(16 / pixelSize));
        }

        int iX(float x) {
            return Math.round(originX + x / pixelSize);
        }

        int iY(float y) {
            return Math.round(originY + y / pixelSize);
        }

        int iL(float l) {
            return Math.round(l / pixelSize);
        }

        float fx(int x) {
            return (x - originX) * pixelSize;
        }

        float fy(int y) {
            return (y - originY) * pixelSize;
        }

        float fl(int L) {
            return (float) L;
        }

        void drawComponent(Graphics g, RectComponent c) {
            // draw component if it is not hidden
            if (!c.hidden) {
                int X = iX(c.x), Y = iY(c.y), W = iL(c.w), H = iL(c.h);

                // fill color if needed
                if (c.fillColor != null) {
                    g.setColor(c.fillColor);
                    g.fillRect(X, Y, W, H);
                }

                // draw border
                if (c.borderColor != null) {
                    g.setColor(c.borderColor);
                    g.drawRect(X, Y, W, H);
                }

                // draw string if needed
                if (c.text != null) {
                    // Get the FontMetrics
                    FontMetrics metrics = g.getFontMetrics(g.getFont());
                    // Determine the X coordinate for the text
                    float x = c.x + (c.w - metrics.stringWidth(c.text)) / 2;
                    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
                    float y = c.y + ((c.h - metrics.getHeight()) / 2) + metrics.getAscent();
                    // Draw the String
                    g.drawString(c.text, iX(x), iY(y));
                }
            }
            // Draw sub-components recursively
            for (RectComponent rc : c.subComponents)
                drawComponent(g, rc);
        }

        void drawMainArea(Graphics g) {

            // config maiArea
            {
                float width = MAINAREA_WIDTH * blockSize;
                float height = MAINAREA_HEIGHT * blockSize;
                float x = margin, y = margin;
                mainArea.x = x;
                mainArea.y = y;
                mainArea.w = width;
                mainArea.h = height;
                mainArea.subComponents.clear();
            }

            // add falling blocks to mainArea
            Tetromino fallingTer = GameOfTetris.getInstance().getFallingTetr();
            fallingBlockComponents.clear();
            if (fallingTer != null) {
                int[][] fallingBlocks = fallingTer.getBlocksPos();
                for (int i = 0; i < fallingBlocks.length; i++) {
                    int[] xy = fallingBlocks[i];
                    int x = xy[0];
                    int y = xy[1];
                    if (GameOfTetris.getInstance().insideMainArea(x, y)) {
                        RectComponent block = new RectComponent(mainArea.relativeX((float) x * blockSize), mainArea.relativeY((float) y * blockSize), blockSize, blockSize);
                        block.fillColor = fallingTer.color;
                        fallingBlockComponents.add(block);
                        mainArea.addSubComponent(block);
                    }
                }
            }

            // add existing blocks to mainArea
            Color[][] data = GameOfTetris.getInstance().getMainAreaData();
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[x].length; y++) {
                    if (data[x][y] != null) {
                        RectComponent block = new RectComponent(mainArea.relativeX((float) x * blockSize), mainArea.relativeY((float) y * blockSize), blockSize, blockSize);
                        block.fillColor = data[x][y];
                        mainArea.addSubComponent(block);
                    }
                }
            }

            // Pause
            {
                String text = "PAUSE";
                pauseLabel.text = text;
                pauseLabel.textColor = Color.blue;
                pauseLabel.borderColor = Color.blue;

                // set size
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                pauseLabel.w = metrics.stringWidth(pauseLabel.text) + margin * 2;
                pauseLabel.h = metrics.getHeight() + margin * 2;
                pauseLabel.setCenter(mainArea.centerX(), mainArea.centerY());
                mainArea.addSubComponent(pauseLabel);
            }

            // Pause
            if (GameOfTetris.getInstance().isGameOver) {
                String text = "Game Over";
                gameOverLabel.text = text;
                gameOverLabel.textColor = Color.red;
                gameOverLabel.borderColor = Color.red;

                // set size
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                gameOverLabel.w = metrics.stringWidth(gameOverLabel.text) + margin * 2;
                gameOverLabel.h = metrics.getHeight() + margin * 2;
                gameOverLabel.setCenter(mainArea.centerX(), mainArea.centerY());
                mainArea.addSubComponent(gameOverLabel);
            }

            // draw
            drawComponent(g, mainArea);
        }

        void drawNextShape(Graphics g) {
            // config nextShape
            {
                float height = 4 * blockSize;
                float width = 5 * blockSize;
                float x = margin + mainArea.x + mainArea.w, y = mainArea.y;
                nextShape.x = x;
                nextShape.y = y;
                nextShape.w = width;
                nextShape.h = height;
                nextShape.subComponents.clear();
            }

            // add blocks to nextShape
            {
                RectComponent blocks = new RectComponent(0, 0, 4 * blockSize, 2 * blockSize);
                blocks.borderColor = null;
                blocks.setCenter(nextShape.centerX(), nextShape.centerY());

                Tetromino nextTetr = GameOfTetris.getInstance().getNextTetr();
                if (nextTetr != null) {
                    int[][] nextTetrPos = Tetromino.generateBlockPos(new int[]{0, 1}, nextTetr.getRelativePos());

                    Color color = nextTetr.color;

                    for (int i = 0; i < nextTetrPos.length; i++) {
                        int x = nextTetrPos[i][0], y = nextTetrPos[i][1] - 1;
                        RectComponent block = new RectComponent(blocks.relativeX((float) x * blockSize), blocks.relativeY((float) y * blockSize), blockSize, blockSize);
                        block.fillColor = color;
                        blocks.addSubComponent(block);
                    }

                    nextShape.addSubComponent(blocks);
                }
            }

            // draw
            drawComponent(g, nextShape);
        }

        void drawScorePanel(Graphics g) {
            int fontHeight = g.getFontMetrics(g.getFont()).getHeight();
            int X = iX(nextShape.x);
            int Y = iY(nextShape.y + nextShape.h);

            Y += fontHeight + margin;
            g.drawString(String.format("Level: %03d", LEVEL), X, Y);
            Y += fontHeight + margin;
            g.drawString(String.format("Lines: %03d", LINE), X, Y);
            Y += fontHeight + margin;
            g.drawString(String.format("Score: %03d", SCORE), X, Y);
        }

        void drawBtns(Graphics g) {
            // QuitBtn
            {
                String text = "QUIT";
                quitBtn.text = text;
                quitBtn.textColor = Color.black;
                quitBtn.borderColor = Color.black;

                // set size
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                quitBtn.w = metrics.stringWidth(quitBtn.text) + margin * 2;
                quitBtn.h = metrics.getHeight() + margin * 2;

                quitBtn.x = nextShape.x;
                quitBtn.y = rHeight - margin - quitBtn.h;
            }
            drawComponent(g, quitBtn);

            // NewGameBtn
            {
                String text = "New Game";
                newGameBtn.text = text;
                newGameBtn.textColor = Color.black;
                newGameBtn.borderColor = Color.black;

                // set size
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                newGameBtn.w = metrics.stringWidth(newGameBtn.text) + margin * 2;
                newGameBtn.h = metrics.getHeight() + margin * 2;

                newGameBtn.x = nextShape.x;
                newGameBtn.y = quitBtn.y - margin - newGameBtn.h;
            }
            drawComponent(g, newGameBtn);

        }

        public void paint(Graphics g) {
            initgr();
            g.setFont(textFont);
            drawMainArea(g);
            drawNextShape(g);
            drawScorePanel(g);
            drawBtns(g);
        }
    }
}