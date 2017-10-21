/**
 * CS 6366.001 - Computer Graphics - F17
 * Instructor: Kang Zhang
 * Student: Binhan Wang (bxw161330)
 *
 * Assignment 2
 *
 * Enhance your Tetris program (from your Assignment 1 submission)
 *
 */

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    static int MAINAREA_WIDTH = DEFAULT_MAINAREA_WIDTH;
    static int MAINAREA_HEIGHT = DEFAULT_MAINAREA_HEIGHT;

    // Gaming
    final static int BASE_TIMER_INTERVAL = 500;

    static int LEVEL = 1;
    static int LINE = 1;
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
     * @param args
     */
    public static void main(String[] args) {
        GameOfTetris.getInstance().showSettingDialog();
    }

    /**
     * Some sub class for the application logic
     */
    // class for timer task
    static class GameTimerTask extends TimerTask {
        GameTimerTask(){

        }
        @Override
        public void run(){
            System.out.println("Timer tick!");
            if(GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.MoveDown))
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
            RotateRight;
        }

        private enum Tetromino_Type {
            Tetromino_S(new Color(255,250,0), new boolean[][]{
                    {false, true, true},
                    { true, true,false},
                    {false,false,false}
            }, new int[]{3,-2}),
            Tetromino_Z(new Color(91,47,139),new boolean[][]{
                    { true, true,false},
                    {false, true, true},
                    {false,false,false}
            }, new int[]{3,-2}),
            Tetromino_J(new Color(0,93,175),new boolean[][]{
                    { true,false,false},
                    { true, true, true},
                    {false,false,false}
            }, new int[]{3,-2}),
            Tetromino_L(new Color(255,33,0),new boolean[][]{
                    {false,false, true},
                    { true, true, true},
                    {false,false,false}
            }, new int[]{3,-2}),
            Tetromino_O(new Color(0,153,62),new boolean[][]{
                    {false, true, true,false},
                    {false, true, true,false},
                    {false,false,false,false},
                    {false,false,false,false}
            }, new int[]{3,-2}),
            Tetromino_T(new Color(255,174,0),new boolean[][]{
                    {false, true,false},
                    { true, true, true},
                    {false,false,false}
            }, new int[]{3,-2}),
            Tetromino_I(new Color(0,159,236),new boolean[][]{
                    {false,false,false,false},
                    { true, true, true, true},
                    {false,false,false,false},
                    {false,false,false,false}
            }, new int[]{3,-2});

            private Color color;
            private boolean[][] initRelativePos;
            private int[] initAnchor;
            Tetromino_Type(Color color, boolean[][] initRelativePos, int[] initAnchor){
                this.color = color;
                this.initRelativePos = initRelativePos;
                this.initAnchor = initAnchor;
            }

            public Color getColor() {
                return color;
            }

            /**
             * Pick a random value of the Tetromino_Type enum.
             * @return a random Tetromino_Type.
             */
            public static Tetromino_Type getRandomType() {
                Random random = new Random();
                return values()[random.nextInt(values().length)];
            }

            /**
             * Get a deep copy of initRelativePos
             * @return
             */
            public boolean[][] getInitRelativePos() {
                boolean[][] initPosCopy = new boolean[initRelativePos.length][];
                for(int i=0;i<initRelativePos.length;i++)
                    initPosCopy[i] = Arrays.copyOf(initRelativePos[i],initRelativePos[i].length);
                return initPosCopy;
            }

            /**
             * Get a deep copy of InitAnchor
             * @return
             */
            public int[] getInitAnchor() {
                return Arrays.copyOf(initAnchor,initAnchor.length);
            }
        }

        private Tetromino_Type type;
        private int[] anchor;
        private boolean[][] relativePos;

        Color color;
        Tetromino(){
            type = Tetromino_Type.getRandomType();
            relativePos = type.getInitRelativePos();
            anchor = type.getInitAnchor();
            color = type.getColor();
        }

        public static int[][] generateBlockPos(int[] anchor, boolean[][] relativePos){
            int[][] blockPos = new int[4][2];
            int idx=0;
            for(int i=0;i<relativePos.length;i++){
                for(int j=0;j<relativePos[i].length;j++)
                    if(relativePos[i][j]) {
                        blockPos[idx][0] = j+anchor[0];
                        blockPos[idx++][1] = i+anchor[1];
                    }
            }
            return blockPos;
        }

        public void setAnchor(int[] anchor) {
            this.anchor = anchor;
        }

        public int[] getAnchor() {
            return Arrays.copyOf(anchor,anchor.length);
        }

        public void setRelativePos(boolean[][] relativePos) {
            this.relativePos = relativePos;
        }

        public boolean[][] getRelativePos() {
            boolean[][] relativePosCopy = new boolean[relativePos.length][];
            for(int i=0;i<relativePos.length;i++)
                relativePosCopy[i] = Arrays.copyOf(relativePos[i],relativePos[i].length);
            return relativePosCopy;
        }

        public int[][] getBlocksPos(){
            return generateBlockPos(anchor,relativePos);
        }
    }

    private enum GameStatus{
       Init, Playing, Over;
    }

    // Application logic singleton object
    private static final GameOfTetris instance = new GameOfTetris();

    // static UI properties
    CvGameOfTetris canvas;
    GameOfTetrisFrame frame;
    Dialog dialog;

    // application properties
    private boolean pause;
    private Timer timer;
    private Color[][] mainAreaData;
    private Tetromino fallingTetr;
    private Tetromino nextTetr;
    private GameStatus status;

    private GameOfTetris(){
        // Init UI element
        status = GameStatus.Init;

        mainAreaData = new Color[MAINAREA_WIDTH][MAINAREA_HEIGHT];
        canvas = new CvGameOfTetris();
        frame = new GameOfTetrisFrame(canvas);
    }

    public static synchronized GameOfTetris getInstance(){
        return instance;
    }

    /**
     * Game Controls
     */
    void startGame(){
        System.out.println("Game Start!");
        status = GameStatus.Playing;
        // trigger timer
        pause = true;
        setPause(false);

        fallingTetr = new Tetromino();
        nextTetr = new Tetromino();
        status = GameStatus.Playing;
        canvas.repaint();
    }
    void endGame(){
        System.out.println("Game Over!");
        status = GameStatus.Over;
        setPause(true);
        fallingTetr = null;
    }

    /**
     * Getters & Setters
     */

    // property setter which also help to trigger timer
    public void setPause(boolean pause) {
        if(pause!=this.pause) {
            this.pause = pause;
            if(pause){
                System.out.println("Timer stop!");
                timer.cancel();
            }
            else {
                System.out.println("Timer start!");
                timer = new Timer();
                //FS = FS x (1 + Level  x S).
                float interval = (float)BASE_TIMER_INTERVAL/(1f+S*(float)LEVEL);
                timer.scheduleAtFixedRate(new GameTimerTask(), (int)interval, (int)interval);
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

    public boolean hitTest(int[][] blocks){
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
     * @param x
     * @param y
     * @return
     */
    public boolean insideMainArea(int x, int y) {
        return x>=0&&x< MAINAREA_WIDTH &&y>=0&&y<MAINAREA_HEIGHT;
    }

    /**
     * Helper function - Rotate an boolean matrix in-place
     * @param mat
     * @param clockwise
     */
    public static void rotateMatrix(boolean mat[][], boolean clockwise)
    {
        int N = mat.length;
        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++)
        {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N-x-1; y++)
            {
                if(clockwise) {
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
                }
                else {
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
     * @param action
     * @return whether should repaint
     */
    public boolean moveAction(Tetromino.Action_Type action){
        if(fallingTetr==null)
            return false;

        boolean[][] newRelativePos = fallingTetr.getRelativePos();
        int[] newAnchor = fallingTetr.getAnchor();
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
                if(fallingTetr.type!= Tetromino.Tetromino_Type.Tetromino_O)
                    rotateMatrix(newRelativePos,true);
                break;
            case RotateRight:
                if(fallingTetr.type!= Tetromino.Tetromino_Type.Tetromino_O)
                    rotateMatrix(newRelativePos,false);
                break;
            default:
                break;
        }

        int[][] newBlocksPos = Tetromino.generateBlockPos(newAnchor,newRelativePos);

        if(hitTest(newBlocksPos)) {
            fallingTetr.setAnchor(newAnchor);
            fallingTetr.setRelativePos(newRelativePos);
            return true;
        }
        else {
            if (action == Tetromino.Action_Type.MoveDown) {
                // merge fallingTetr into main area data
                boolean endOfGame = false;
                int[][] blocksPos = fallingTetr.getBlocksPos();
                PriorityQueue<Integer> rowsToRemove = new PriorityQueue<>(MAINAREA_HEIGHT,Collections.reverseOrder());
                for (int i = 0; i < blocksPos.length; i++) {
                    int x = blocksPos[i][0];
                    int y = blocksPos[i][1];
                    if(!insideMainArea(x,y))
                        endOfGame = true;
                    else {
                        // merge color block to maiArea
                        mainAreaData[x][y] = fallingTetr.color;

                        // add rows to remove
                        boolean hasHole = false;
                        for(int j=0;j<MAINAREA_WIDTH;j++) {
                            if (mainAreaData[j][y] == null) {
                                hasHole = true;
                                break;
                            }
                        }
                        if(!hasHole)
                            rowsToRemove.add(y);
                    }
                }
                if(!endOfGame) {
                    fallingTetr = nextTetr;
                    nextTetr = new Tetromino();
                    // remove rows if necessary
                    if(rowsToRemove.size()>0){
                        removeRows(rowsToRemove);
                    }
                }
                else {
                    //Game Over
                    endGame();
                }
            }

            return false;
        }
    }

    void removeRows(PriorityQueue<Integer> rows){

        // Already in descending order
        int skip = 0;
        Iterator<Integer> it = rows.iterator();
        int lowRow = -1;
        int highRow;
        while (it.hasNext()) {
            highRow = lowRow;
            lowRow = it.next();
            skip++;
            for (int r = lowRow - 1; r > highRow; r--) {
                for(int c = 0; c<MAINAREA_WIDTH;c++)
                    // move down
                    mainAreaData[c][r+skip] = mainAreaData[c][r];
            }
        }
    }

    /**
     * Dialogs
     */
    private void showSettingDialog(){
        // Create a modal dialog
        dialog = new Dialog(frame, "Settings", true);

        // Use a flow layout
        dialog.setLayout( new GridLayout(5,1) );

        Panel row = null;
        {
            row = new Panel();
            row.setLayout(new FlowLayout(FlowLayout.LEADING));
            row.add( new Label ("Game Setting:"));
        }
        dialog.add(row);

        // Scoring Factor M
        {
            row = new Panel();
            row.setLayout(new FlowLayout(FlowLayout.LEADING));

            row.add(new Label("M:"));

            Label v = new Label(String.format("%02d",M));

            JSlider slider = new JSlider(JSlider.HORIZONTAL,M_MIN,M_MAX,M);
            slider.setMajorTickSpacing(3);
            slider.setMinorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    M = slider.getValue();
                    v.setText(String.format("%02d",M));
                }
            });
            row.add(slider);
            row.add(v);
        }
        dialog.add(row);

        // Difficulty Factor N
        {
            row = new Panel();
            row.setLayout(new FlowLayout(FlowLayout.LEADING));

            row.add(new Label("N:"));

            Label v = new Label(String.format("%02d",N));

            JSlider slider = new JSlider(JSlider.HORIZONTAL,N_MIN,N_MAX,N);
            slider.setMajorTickSpacing(5);
            slider.setMinorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    N = slider.getValue();
                    v.setText(String.format("%02d",N));
                }
            });
            row.add(slider);
            row.add(v);
        }
        dialog.add(row);


        // Speed Factor S
        {
            row = new Panel();
            row.setLayout(new FlowLayout(FlowLayout.LEADING));

            row.add(new Label("S:"));

            Label v = new Label(String.format("%.1f",S));

            JSlider slider = new JSlider(JSlider.HORIZONTAL,(int)(S_MIN*S_SCALE),(int)(S_MAX*S_SCALE),(int)(S*S_SCALE));
            slider.setMajorTickSpacing(3);
            slider.setMinorTickSpacing(1);

            Hashtable labelTable = new Hashtable();
            labelTable.put( new Integer( (int)(S_MIN*S_SCALE) ), new JLabel(String.format("%.1f",S_MIN)) );
            labelTable.put( new Integer( (int)(S_MAX*S_SCALE/2) ), new JLabel(String.format("%.1f",S_MAX/2)));
            labelTable.put( new Integer( (int)(S_MAX*S_SCALE) ), new JLabel(String.format("%.1f",S_MAX)) );
            slider.setLabelTable( labelTable );

            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    S = (float)slider.getValue()/S_SCALE;
                    v.setText(String.format("%.1f",S));
                }
            });
            row.add(slider);
            row.add(v);
        }
        dialog.add(row);

        {
            row = new Panel();
            row.setLayout(new FlowLayout(FlowLayout.LEADING));

            // Create an OK button
            Button ok = new Button ("OK");
            ok.addActionListener ( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    //todo start game
                    startGame();
                    dialog.setVisible(false);
                }
            });

            row.add(ok);
        }
        dialog.add(row);

        // Show dialog
        dialog.pack();
        dialog.setVisible(true);
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
            setSize(620, 800);
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

            public RectComponent() {}

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
        float pixelSize, rWidth = 620.0F, rHeight = 800.0F;
        float blockSize, margin = rWidth/100; // Margin for layout component
        Font textFont;
        // Components
        RectComponent mainArea, pauseLabel, nextShape, scorePanel, quitBtn;

        CvGameOfTetris() {
            mainArea = new RectComponent();
            pauseLabel = new RectComponent();
            nextShape = new RectComponent();
            scorePanel = new RectComponent();
            quitBtn = new RectComponent();

            pauseLabel.hidden = true;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent evt) {

                    super.mousePressed(evt);

                    // do not response to any event when game is not playing
                    if(GameOfTetris.getInstance().status!=GameStatus.Playing)
                        return;

                    int btn = evt.getButton();
                    float xA = fx(evt.getX()), yA = fy(evt.getY());
                    if (btn==MouseEvent.BUTTON1) {
                        if(quitBtn != null && quitBtn.inside(xA, yA)) {
                            System.exit(0);
                        }
                        else if(!GameOfTetris.getInstance().pause){
                            GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.MoveLeft);
                            repaint();
                        }
                    }
                    else if(btn==MouseEvent.BUTTON3) {
                        if(!GameOfTetris.getInstance().pause){
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
                    if(GameOfTetris.getInstance().status!=GameStatus.Playing)
                        return;

                    if(GameOfTetris.getInstance().pause)
                        return;
                    if (evt.getWheelRotation() < 0) {
                        System.out.println("Rotated Up... " + evt.getWheelRotation());
                        if(GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.RotateLeft))
                            repaint();

                    } else {
                        System.out.println("Rotated Down... " + evt.getWheelRotation());
                        if(GameOfTetris.getInstance().moveAction(Tetromino.Action_Type.RotateRight))
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
                    if(GameOfTetris.getInstance().status!=GameStatus.Playing)
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
                }
            });
        }

        void initgr() {
            Dimension d = getSize();
            int maxX = d.width - 1, maxY = d.height - 1;
            pixelSize = Math.max(rWidth / maxX, rHeight / maxY);
            blockSize = (rHeight - 2 * margin) / 20;
            // Question: how to make font size proportional to size change?
            textFont = new Font("Dialog", Font.BOLD, iL(20/pixelSize));
        }

        int iX(float x) {
            return Math.round(originX + x / pixelSize);
        }

        int iY(float y) {
            return Math.round(originY + y / pixelSize);
        }

        int iL(float length) {
            return Math.round(length / pixelSize);
        }

        float fx(int x) {
            return (x - originX) * pixelSize;
        }

        float fy(int y) {
            return (y - originY) * pixelSize;
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
                if(c.borderColor!=null) {
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
                mainArea.x = x; mainArea.y = y; mainArea.w = width; mainArea.h = height;
                mainArea.subComponents.clear();
            }

            // add falling blocks to mainArea
            Tetromino fallingTer = GameOfTetris.getInstance().getFallingTetr();
            if(fallingTer!=null) {
                int[][] fallingBlocks = fallingTer.getBlocksPos();
                for (int i = 0; i < fallingBlocks.length; i++) {
                    int[] xy = fallingBlocks[i];
                    int x = xy[0];
                    int y = xy[1];
                    if (GameOfTetris.getInstance().insideMainArea(x, y)) {
                        RectComponent block = new RectComponent(mainArea.relativeX((float) x * blockSize), mainArea.relativeY((float) y * blockSize), blockSize, blockSize);
                        block.fillColor = fallingTer.color;
                        mainArea.addSubComponent(block);
                    }
                }
            }

            // add existing blocks to mainArea
            Color[][] data = GameOfTetris.getInstance().getMainAreaData();
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[x].length; y++) {
                    if(data[x][y]!=null) {
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
                pauseLabel.w = metrics.stringWidth(pauseLabel.text) + margin * 4;
                pauseLabel.h = metrics.getHeight() + margin * 4;
                pauseLabel.setCenter(mainArea.centerX(), mainArea.centerY());
                mainArea.addSubComponent(pauseLabel);
            }

            // draw
            drawComponent(g, mainArea);
        }

        void drawNextShape(Graphics g) {
            // config nextShape
            {
                float height = 4 * blockSize;
                float width = 5 * blockSize;
                float x = margin*3+mainArea.x+mainArea.w, y = mainArea.y;
                nextShape.x = x; nextShape.y = y; nextShape.w = width; nextShape.h = height;
                nextShape.subComponents.clear();
            }

            // add blocks to nextShape
            {
                RectComponent blocks = new RectComponent(0, 0, 4 * blockSize, 2 * blockSize);
                blocks.borderColor = null;
                blocks.setCenter(nextShape.centerX(), nextShape.centerY());

                Tetromino nextTetr = GameOfTetris.getInstance().getNextTetr();
                if(nextTetr!=null) {
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
            int Y = iY(nextShape.y+nextShape.h);

            Y +=fontHeight+margin*5;
            g.drawString(String.format("Level: %03d",LEVEL),X, Y);
            Y +=fontHeight+margin*5;
            g.drawString(String.format("Lines: %03d",LINE),X, Y);
            Y +=fontHeight+margin*5;
            g.drawString(String.format("Score: %03d",SCORE),X, Y);
        }

        void drawQuitBtn(Graphics g) {
            // QuitBtn
            {
                String text = "QUIT";
                quitBtn.text = text;
                quitBtn.textColor = Color.black;
                quitBtn.borderColor = Color.black;

                // set size
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                quitBtn.w = metrics.stringWidth(pauseLabel.text) + margin * 4;
                quitBtn.h = metrics.getHeight() + margin * 4;

                quitBtn.x = nextShape.x;
                quitBtn.y = rHeight-margin-quitBtn.h;
            }
            drawComponent(g, quitBtn);
        }

        public void paint(Graphics g) {
            initgr();
            g.setFont(textFont);
            drawMainArea(g);
            drawNextShape(g);
            drawScorePanel(g);
            drawQuitBtn(g);
        }
    }
}