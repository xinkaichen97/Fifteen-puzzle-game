
/**
 * Write a description of class FifteenPuzzle here.
 *
 * @author Nathan Santoso
 */

import java.lang.Math;
import java.lang.Object;
import java.awt.geom.Path2D;
import java.awt.event.*;
import java.awt.Color;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class FifteenPuzzle implements MouseListener
{
    private static int[][] grid = {{1,5,9,13}, {2,6,10,14}, {3,7,11,15}, {4,8,12, 0}}; // the current positions of the tiles and space, denoted by 0..15
    private int xspace;   // xspace,yspace are the current coordinates of the space
    private int yspace;
    private SimpleCanvas sc; // the canvas for display
    private boolean initialising = false;
    private int clickCount = 0;
    private int score = 1010000;
    private int shuffleCount = 1600;
    private int stars = 3;
    private int mouseclickX;
    private int mouseclickY;

    private final int size     = 4;               // the number of tiles across and down
    private final int tilesize = 100;              // the size of a tile
    private final int gridsize = size * tilesize; // the size of the grid
    private final int border = 50;
    private final int padding = 30;
    private final int squareSize = gridsize + border*2 + padding * (size-1);
    private final int ui = 35;
    private final Color bgColor = Color.white;

    private static final int[][] goal    = {{1,5,9,13}, {2,6,10,14}, {3,7,11,15}, {4,8,12, 0}};
    // these two are public so that they can be used in BlueJ
    public  static int[][] close   = {{1, 5,9,13}, {2,6,10,14}, {3,7,11, 0}, {4,8,12,15}};
    public  static int[][] example = {{5,11,14,0}, {9,3,13, 7}, {2,8,10,12}, {4,1,15, 6}};

    public static void main(String[] args)
    {
        new FifteenPuzzle();
    }

    // this constructor sets up the grid as initialGrid and displays it on the canvas
    // (plus it initialises the other instance variables)
    public FifteenPuzzle (int[][] initialGrid)
    {
        // TODO
        int[] checkDupe = new int[16];
        checkloop:
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++){
                checkDupe[i*4+j] = initialGrid[i][j];
                for (int k = j+1; k < initialGrid.length; k++){
                    if((k != j) && (initialGrid[i][j] == initialGrid[i][k])){
                        grid = goal;
                        System.err.println("Duplicate number");
                        break checkloop;
                    }
                }
                if(initialGrid[i][j] < 0 || initialGrid[i][j] > 15){
                    System.err.println("Invalid number; too big");
                    break checkloop;
                }
                else{
                    grid = initialGrid;
                }
            }
        xspace = 0;
        yspace = 0;
        sc = new SimpleCanvas("FifteenPuzzle", squareSize, squareSize+ui, bgColor);
        drawUI();
        sc.repaint();
        fontBig(3);
        sc.addMouseListener(this);
        drawGrid();
    }

    // this constructor sets up the grid as goal,
    // then it makes random moves to set up the puzzle and displays it on the canvas
    // (plus it initialises the other instance variables)
    public FifteenPuzzle ()
    {
        // TODO
        this(grid);
        initialising = true;
        double sS = (double)squareSize;
        double pd = (double)padding;
        double sC = (double)shuffleCount;
        for(double i = 0; i < shuffleCount; i++){
            int a = randRange(0,(size-1));
            int b = randRange(0,(size-1));
            if(legalClick(a,b)){
                moveTile(a,b);
                try {
                    TimeUnit.MICROSECONDS.sleep(100);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            sc.drawLine((int)((pd/2)+((sS-(pd))*(i/sC))), (int)(sS+ui-(pd/4)), (int)((pd/2)+((sS-(pd))*(i/sC))),(int)(sS+(pd/4)));
            if(i%5 == 0) sc.repaint();
        }
        drawUI();
        drawClicks();
        initialising = false;
    }

    // sets grid to be 1 click from goal
    public void almostWin()
    {
        grid = close.clone();
        drawGrid();
    }

    // increases font size by a factor of x
    public void fontBig(double x)
    {
        sc.setFont(sc.getFont().deriveFont(sc.getFont().getSize() * (float)x));
    }

    // pauses for x microseconds
    private void waitMicro(int x)
    {
        try {
            TimeUnit.MICROSECONDS.sleep(x);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // returns a random integer between min and max
    private int randRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    // returns an integer which is the pixel coordinate of a grid corner
    private int getPixelCoord(int x)
    {
        return border + x*(tilesize + padding);
    }

    // returns array of size 2 with the x and y coordinate (0->3) of the space
    private int[] getSpaceCoord()
    {
        int[] space = new int[2];
        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                if(grid[i][j] == 0){
                    space[0] = i;
                    space[1] = j;
                }
        return space;
    }

    // returns true iff x,y is adjacent to the space
    private boolean legalClick(int x, int y)
    {
        // TODO
        int a = 0, b = 0;
        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                if(grid[i][j] == 0){
                    a = i;
                    b = j;
                }
        if(Math.abs(x-a) + Math.abs(y-b) == 1)
            return true;
        return false;
    }

    // returns true if x,y is the space
    private boolean isBlank(int x, int y)
    {

        if(grid[x][y] == 0){
            return true;
        }
        return false;
    }

    // returns true iff the puzzle is finished
    private boolean finished()
    {
        // TODO
        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                if(grid[i][j] != goal[i][j])
                    return false;
        return true;
    }

    // after a short delay, draws a finish screen which changes background colour based on what score you get
    // also draws 'You Win!' and Score on page, and draws Reset button
    public void YouWin()
    {
        int resetX1 = padding-10;
        int resetX2 = padding*(3/2)+tilesize+40;
        int resetY1 = squareSize - tilesize + padding-5;
        int resetY2 = squareSize - padding + 5;
        Color theColor = new Color(0,0,0);

        sc.wait(200);
        updateScore();
        switch((int)(score/100000)){
            case 10:                        drawRainbow(2);                             break;
            case 9:                         sc.setForegroundColour(Color.GREEN);        break;
            case 5: case 6: case 7: case 8: sc.setForegroundColour(Color.BLUE);         break;
            case 1: case 2: case 3: case 4: sc.setForegroundColour(Color.LIGHT_GRAY);   break;
            case 0:                         sc.setForegroundColour(Color.DARK_GRAY);    break;
        }
        if(score < 10000)    sc.setForegroundColour(Color.BLACK);
        if(score < 1000000)  sc.drawRectangle(0, 0, squareSize, squareSize+ui);

        fontBig(4);
        sc.setForegroundColour(Color.gray);
        sc.drawString("You win!", (squareSize+padding)/(size*3)-30,(squareSize)/2-5);

        fontBig(0.8);
        sc.setForegroundColour(Color.white);
        sc.drawString("You win!", (squareSize+padding)/(size*3)+30,(squareSize)/2);

        sc.setForegroundColour(Color.black);
        sc.drawRectangle(resetX1-3, resetY1-3, resetX2+3, resetY2+3);
        sc.setForegroundColour(Color.blue);
        sc.drawRectangle(resetX1, resetY1, resetX2, resetY2);

        fontBig(0.36);
        sc.setForegroundColour(Color.white);
        sc.drawString("Reset", padding+8,squareSize-padding-3);

        drawScore();

        sc.repaint();

    }

    // draws a rainbow gradient with x altering the 'frequency' of rainbow
    public void drawRainbow(int x)
    {
        int a = 0;
        int counter = 0;
        Color theColor = new Color(0,0,0);
        if(score > 1000000){
            for(int i = 0; i < 1500; i++){
                a = a+x;
                if(a > 255) {a = 0; counter++;}
                switch(counter%6){
                    case 0 : theColor = new Color(255, a, 0);       break;
                    case 1 : theColor = new Color(255-a, 255, 0);   break;
                    case 2 : theColor = new Color(0, 255, a);       break;
                    case 3 : theColor = new Color(0, 255-a, 255);   break;
                    case 4 : theColor = new Color(a, 0, 255);       break;
                    case 5 : theColor = new Color(255, 0, 255-a);   break;
                }
                sc.setForegroundColour(theColor);
                sc.drawLine(0, i, squareSize, i);
                sc.repaint();
            }
        }
    }

    // moves the tile containing the number x into the space
    public void moveTileNumber (int x)
    {
        int a = 0, b = 0;
        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                if(grid[i][j] == x){
                    a = i;
                    b = j;
                }
        moveTile(a, b);
    }

    // moves the tile at x,y into the space, if it is adjacent, and re-draws the grid; o/w do nothing
    public void moveTile (int x, int y)
    {
        // TODO
        if(!finished() || initialising){
            int xSpace = getSpaceCoord()[0];
            int ySpace = getSpaceCoord()[1];
            if(legalClick(x, y)){
                grid[xSpace][ySpace] = grid[x][y];
                grid[x][y] = 0;
                if(!initialising) {
                    clickCount++;
                    updateScore();
                }
                drawGrid();
            }
        }
    }

    // draws the current grid on the canvas
    private void drawGrid()
    {
        // TODO
        Color theColor = new Color(75,165,255);
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                drawTile(i, j, theColor);
                if((i == getSpaceCoord()[0]) && (j == getSpaceCoord()[1])){
                    drawTile(i, j, bgColor);
                }
            }
        }
        sc.repaint();
    }

    // draws the tile at x,y in colour c at the appropriate spot on the canvas
    private void drawTile(int x, int y, Color c)
    {
        Color theColor = new Color(0,0,0);
        int top = getPixelCoord(y);
        int bot = getPixelCoord(y+1) - padding;
        int left = getPixelCoord(x);
        int right = getPixelCoord(x+1) - padding;
        for(int i = 0; i < 10; i++){
            if(isBlank(x,y)){
                theColor = new Color(255, 255, 255);
            }
            else{
                theColor = new Color(255-20*i, 255-10*i, 255);
            }
            sc.setForegroundColour(theColor);
            sc.drawRectangle(left-(10-i), top-(10-i), right+(10-i), bot+(10-i));
        }
        sc.setForegroundColour(c);
        sc.drawRectangle(left, top, right, bot);
        sc.setForegroundColour(Color.white);
        for(int i = 0; i < 10; i++){
            theColor = new Color(75+20*i, 165+10*i, 255);
            sc.setForegroundColour(theColor);
            if(grid[x][y] < 10){
                sc.drawString("" + grid[x][y], left+42, top+63);
            }
            else{
                sc.drawString("" + grid[x][y], left+30, top+63);
            }
            //delay?
        }

    }

    // redraws tile to be green/red gradient depending on if its a legal or illegal click
    /*
     * Color tileColor = new Color(75,165,255);
     * Color wrongColor = new Color(200,0,0);
     * Color rightColor = new Color(0,200,30);
     */
    private void drawTileClick(int x, int y)
    {
        Color theColor = new Color(0,0,0);
        int s = 30;
        int top = getPixelCoord(y);
        int bot = getPixelCoord(y+1) - padding;
        int left = getPixelCoord(x);
        int right = getPixelCoord(x+1) - padding;
        for(int i = 0; i < s; i++){
            if(isBlank(x,y)){
                theColor = new Color(255, 255, 255);
            }
            else{
                if(legalClick(x,y))
                    theColor = new Color((60 - 60*i/s),(170 + 30*i/s),(245 - 215*i/s));
                else{theColor =  new Color((75 + 125*i/s),(165 - 165*i/s),(255 - 255*i/s));
                }
            }
            sc.setForegroundColour(theColor);
            sc.drawRectangle(left+i, top+i, right-i, bot-i);
            sc.repaint();
        }
        sc.setForegroundColour(Color.white);
        if(grid[x][y] < 10){
            sc.drawString("" + grid[x][y], left+42, top+63);
        }
        else{
            sc.drawString("" + grid[x][y], left+30, top+63);
        }
        sc.repaint();
    }

    // checks if x,y is within rectangle of reset button
    private boolean resetClick(int x, int y)
    {
        int resetX1 = padding-10;
        int resetX2 = padding*(3/2)+tilesize+40;
        int resetY1 = squareSize - tilesize + padding-5;
        int resetY2 = squareSize - padding + 5;
        if(resetX1 < x && x < resetX2 && resetY1 < y && y < resetY2)
            return true;
        return false;
    }

    // resets game (and reshuffles)
    public void resetGame()
    {
        initialising = true;
        double sS = (double)squareSize;
        double pd = (double)padding;
        double sC = (double)shuffleCount;
        sc.setForegroundColour(bgColor);
        sc.drawRectangle(0,0,squareSize,squareSize);
        drawUI();
        for(double i = 0; i < shuffleCount; i++){
            int a = randRange(0,(size-1));
            int b = randRange(0,(size-1));
            if(legalClick(a,b)){
                moveTile(a,b);
            }
            sc.drawLine((int)(pd/2+(sS-pd*(i/sC))), (int)(sS+ui-pd/4), (int)(pd/2+(sS-pd*(i/sC))),(int)(sS+pd/4));
            if(i%20 == 0) sc.repaint();
        }
        sc.setFont(new Font(sc.getFont().getFontName(), Font.PLAIN, 36));
        clickCount = 0;
        score = 1010000;
        initialising = false;
        drawGrid();
        drawClicks();

    }

    // updates score
    private void updateScore()
    {
        double a = 5;
        double c = 1010000;
        double d = 230;
        double k = 0.02;
        double b = Math.pow(Math.E, -k);

        score = (int) (c - (c/(1+a*Math.pow(b,clickCount-d))));

    }

    // draws final score for win screen
    private void drawScore()
    {
        sc.setForegroundColour(Color.white);
        sc.drawString("Score: " + score, (squareSize+padding)/(size*3),(squareSize)/2 + padding*3);
        sc.repaint();
    }

    // draws black bar at bottom of screen
    private void drawUI()
    {
        sc.setForegroundColour(Color.black);
        sc.drawRectangle(0,squareSize,squareSize,squareSize+ui);
    }

    // draws clicks and score at bottom of screen
    private void drawClicks ()
    {
        String printScore = "";
        String scoreStr = "" + score;
        drawUI();
        fontBig(0.64);
        sc.setForegroundColour(Color.white);
        sc.drawString("Moves: " + clickCount, padding/2,squareSize+ui-8);
        switch(clickCount/10){
            case 0 : printScore = "Score: ???????"; break;
            case 1 : printScore = "Score: ??????" + scoreStr.substring(scoreStr.length()-1); break;
            case 2 : printScore = "Score: ?????" + scoreStr.substring(scoreStr.length()-2); break;
            case 3 : printScore = "Score: ????" + scoreStr.substring(scoreStr.length()-3); break;
            case 4 : printScore = "Score: ???" + scoreStr.substring(scoreStr.length()-4); break;
            case 5 : printScore = "Score: ??" + scoreStr.substring(scoreStr.length()-5); break;
            case 6 : printScore = "Score: ?" + scoreStr.substring(scoreStr.length()-6); break;
            case 7 : printScore = "Score: " + scoreStr; break;
            default: printScore = "Score: " + scoreStr; break;
        }
        sc.drawString(printScore, squareSize - (int) (tilesize * (double)3/(double)2) - padding, squareSize+ui-8);
        fontBig(1.5625);
        sc.repaint();
    }

    // checks if coordinates x,y are clicking in a tile or not
    private boolean clickingTile(int x, int y)
    {
        if((x > border)                                                                       &&
                (x < squareSize-border)                                                               &&
                !(  (x < border + tilesize + padding)      && (x > border + tilesize)              )  &&
                !(  (x < border + 2*tilesize + 2*padding)  && (x > border + 2*tilesize + padding)  )  &&
                !(  (x < border + 3*tilesize + 3*padding)  && (x > border + 3*tilesize + 2*padding))  &&
                (y > border)                                                                          &&
                (y < squareSize - border)                                                             &&
                !(  (y < border + tilesize + padding)      && (y > border + tilesize)              )  &&
                !(  (y < border + 2*tilesize + 2*padding)  && (y > border + 2*tilesize + padding)  )  &&
                !(  (y < border + 3*tilesize + 3*padding)  && (y > border + 3*tilesize + 2*padding))   )
            return true;
        return false;
    }

    // draws numbers (on top of tiles)
    private void drawNumber(int a, int x, int y)
    {
        sc.drawString("" + a, getPixelCoord(x),getPixelCoord(y));
    }

    public void mouseClicked (MouseEvent e) {}

    // turns tile green/red depending on if its a legal click or not, if game is not finished, not initialising, and is clicking within the tile
    public void mousePressed (MouseEvent e)
    {
        if(!finished() && !initialising)
            if(clickingTile(e.getX(), e.getY()))
                drawTileClick((e.getX()-border) / (tilesize+padding), (e.getY()-border) / (tilesize+padding));
        mouseclickX = (e.getX()-border) / (tilesize+padding);
        mouseclickY = (e.getY()-border) / (tilesize+padding);
    }

    // moves tile into blank space if game is not finished, not initialising, and is clicking within the tile
    // resets game if game is finished, and clicking within reset button
    public void mouseReleased(MouseEvent e)
    {

        if(!finished() && !initialising){
            if(legalClick(mouseclickX, mouseclickY))
                if(clickingTile(e.getX(), e.getY()))
                    moveTile((e.getX()-border) / (tilesize+padding), (e.getY()-border) / (tilesize+padding));
            drawGrid();
            drawClicks();
            if(finished()){
                YouWin();
            }
        }
        if(resetClick(e.getX(), e.getY()) && finished()){
            resetGame();
        }
    }

    public void mouseEntered (MouseEvent e) {}

    public void mouseExited (MouseEvent e) {}
}
