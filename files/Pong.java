/*
 * Copyright (c) 2007 Eric Woroshow
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.ericw.pong;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

public class Pong {
    
    public static final String GAME_NAME = "PONG!";
    private static final int GAME_WIDTH = 320;
    private static final int GAME_HEIGHT = 240;
    private static final int GAME_FPS = 60;
    private static final int PADDLE_HEIGHT = 60;
    private static final int PADDLE_HALFWIDTH = 5;
    private static final float PADDLE_SPEED = 2f;
    private static final float BALL_RADIUS = 5;
    private static final float BALL_SPEED_INCREASE = 1.05f;
    private static final int GFX_SPACER = 10;
    private static final int SCORE_TO_WIN = 5;
    private static final int INTERPOINT_DELAY = GAME_FPS;
    private static final int INTERGAME_DELAY = 3 * GAME_FPS;

    private Frame window;
    private BufferStrategy bufStrat;
    private boolean[] keys;
    
    private Font titleFont;
    private Font menuFont;
    private Font scoreFont;
    private Font winnerFont;
    private Stroke centreStroke;
    
    private boolean finished;
    
    private int timer;
    private long timeThen, timeNow, timeLate;
    
    private enum GameState { MENU, INGAME, POINTSCORED, WINNER };
    private GameState state;
    
    private float playerLY, playerRY;
    private float ballX, ballY, ballVX, ballVY;
    private int playerLScore, playerRScore;
    private boolean singlePlayer;

    /**
     * Creates and runs a new game of Pong.
     */
    public Pong() {
        init();
        run();
        quit();
    }
    
    /**
     * Initializes the game state and display.
     */
    private void init() {
        // setup game state
        titleFont = new Font("Verdana", Font.BOLD, 60);
        menuFont = new Font("Verdana", Font.BOLD, 10);
        scoreFont = new Font("Fixed Width", Font.BOLD, 80);
        winnerFont = new Font("Verdana", Font.BOLD, 18);
        centreStroke = new BasicStroke(BALL_RADIUS, BasicStroke.CAP_BUTT,
                                       BasicStroke.JOIN_MITER, 10f, new float[]{8f}, 0f);
        keys = new boolean[256];
        singlePlayer = false;
        state = GameState.MENU;
        resetPoint();
        
        // setup the game window
        window = new Frame(GAME_NAME);
        window.setIgnoreRepaint(true);
        window.setUndecorated(true);
        window.setSize(GAME_WIDTH, GAME_HEIGHT);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent evt){ finished = true; }
        });
        window.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { keys[e.getKeyCode()] = true; }
            public void keyReleased(KeyEvent e) { keys[e.getKeyCode()] = false; }
        });
        
        // show the window
        window.setVisible(true);
        window.requestFocus();
        
        // setup double buffering on the display
        window.createBufferStrategy(2);
        bufStrat = window.getBufferStrategy();
    }
    
    /**
     * Runs the game, executing game logic and rendering the current state.
     */
    private void run() {
        while(!finished) {
            logic();
            render();
            sync();
        }
    }
    
    /**
     * Cleans up any resources and exits the program as soon as possible.
     */
    private void quit() {
        window.dispose();
    }
    
    /**
     * Updates the game state for a frame.
     */
    private void logic() {
        if (keys[KeyEvent.VK_ESCAPE]) {
            finished = true;
            return;
        }
        
        switch(state) {
            case MENU:
                updateMenu(); break;
            case INGAME:
                updateGame(); break;
            case POINTSCORED:
                updatePointScored(); break;
            case WINNER:
                updateWinner(); break;
        }
    }
    
    private void updateMenu() {
        if (keys[KeyEvent.VK_1]) { // start single player game
            singlePlayer = true;
            resetGame();
            state = GameState.INGAME;
        } else if (keys[KeyEvent.VK_2]) { // start two player game
            singlePlayer = false;
            resetGame();
            state = GameState.INGAME;
        }
    }
    
    private void updateGame() {
        // calculate new position for player one
        if (keys[KeyEvent.VK_A] && playerLY > 20) {
            playerLY -= PADDLE_SPEED;
        }
        if (keys[KeyEvent.VK_Z] && playerLY + PADDLE_HEIGHT < GAME_HEIGHT - 20) {
            playerLY += PADDLE_SPEED;
        }
        
        // calculate new position for player two 
        if (!singlePlayer) {
            if (keys[KeyEvent.VK_UP] && playerRY > 20) {
                playerRY -= PADDLE_SPEED;
            }
            if (keys[KeyEvent.VK_DOWN] && playerRY + PADDLE_HEIGHT < GAME_HEIGHT - 20) {
                playerRY += PADDLE_SPEED;
            }
        } else {
            updateAI();
        }
        
        // do collision detection
        updateBallCollision();        
        
        // calculate new position for the ball
        ballX += ballVX;
        ballY += ballVY;
    }
    
    private void updatePointScored() {
        timer++;
        if (timer >= INTERPOINT_DELAY) {
            timer = 0;
            
            if (playerLScore >= SCORE_TO_WIN || playerRScore >= SCORE_TO_WIN) {
                // one player has one after the last point
                state = GameState.WINNER;
            } else {
                // need to keep playing to find a winner
                resetPoint();
                state = GameState.INGAME;
            }
        }
    }
    
    private void updateWinner() {
        timer++;
        if (timer >= INTERGAME_DELAY) {
            timer = 0;
            state = GameState.MENU;
        }
    }
    
    /**
     * Renders the current state of the game.
     */
    private void render() {
        Graphics2D g = (Graphics2D)bufStrat.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        switch(state) {
            case MENU:
                renderMenu(g); break;
            case INGAME:
            case POINTSCORED:
                renderGame(g); break;
            case WINNER:
                renderWinner(g); break;
        }
        
        g.dispose();
        bufStrat.show();
    }
    
    private void renderMenu(Graphics2D g) {
        // clear the screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // draw the title
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        g.fillRect(0, 70, GAME_WIDTH, 5);
        g.drawString(GAME_NAME, 55, 130);
        g.fillRect(0, 140, GAME_WIDTH, 5);
        
        // draw the instruction text
        g.setFont(menuFont);
        g.drawString("(1) player - (2) players - (Esc)ape", 70, GAME_HEIGHT - 10);
    }
    
    private void renderGame(Graphics2D g) {
        // clear the screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // draw the scores
        g.setFont(scoreFont);
        g.setColor(Color.DARK_GRAY);
        g.drawString(String.valueOf(playerLScore), 120, 70);
        g.drawString(String.valueOf(playerRScore), 155, 70);
        
        // draw the top and bottom edges
        g.setColor(Color.WHITE);
        g.fillRect(GFX_SPACER, GAME_HEIGHT - 2 * GFX_SPACER, GAME_WIDTH - 2 * GFX_SPACER, GFX_SPACER);
        g.fillRect(GFX_SPACER, GFX_SPACER, GAME_WIDTH - 2 * GFX_SPACER, GFX_SPACER);
        
        // draw the centre line
        g.setStroke(centreStroke);
        g.drawLine(GAME_WIDTH / 2, 2 * GFX_SPACER, GAME_WIDTH / 2, GAME_HEIGHT - 2 * GFX_SPACER);
        
        // draw the two paddles
        g.setColor(Color.WHITE);
        g.fillRect(GFX_SPACER, (int)playerLY, GFX_SPACER, PADDLE_HEIGHT);
        g.fillRect(GAME_WIDTH - 2 * GFX_SPACER, (int)playerRY, PADDLE_HALFWIDTH * 2, PADDLE_HEIGHT);
        
        // draw the ball
        g.setColor(Color.WHITE);
        g.fillRect((int)(ballX - BALL_RADIUS), (int)(ballY - BALL_RADIUS),
                   (int)(BALL_RADIUS * 2), (int)(BALL_RADIUS * 2));
    }
    
    private void renderWinner(Graphics2D g) {
        // render the game in the background
        renderGame(g);
        Color maskBack = new Color(100, 100, 100, 128);
        g.setColor(maskBack);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // draw the winner string
        String winner = (playerLScore > playerRScore)
                            ? "Left player wins!"
                            : "Right player wins!";
        
        g.setFont(winnerFont);
        g.setColor(Color.WHITE);
        g.drawString(winner, 85, 120);
    }
    
    /**
     * Resets the position of the ball and paddles for a new point.
     */
    private void resetPoint() {
        playerLY = GAME_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        playerRY = GAME_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        ballX = GAME_WIDTH / 2;
        ballY = GAME_HEIGHT / 2;
        ballVX = (Math.random() > 0.5) ? 2 : -2;
        ballVY = (Math.random() > 0.5) ? 2 : -2;
    }
    
    /**
     * Resets the game state for a new game.
     */
    private void resetGame() {
        playerLScore = 0;
        playerRScore = 0;
        resetPoint();
    }
    
    /**
     * Checks for collision of the ball again the walls and player paddles. If a
     * player has scored this method will change the state appropriately.
     */
    private void updateBallCollision() {
        // check for collision against the top and bottom
        if ((ballY - BALL_RADIUS <= 2 * GFX_SPACER) ||
            (ballY + BALL_RADIUS >= GAME_HEIGHT - 2 * GFX_SPACER)) {
            ballVY = -ballVY;
        }
        
        // check for collision with paddles
        final int PADDLE_HALFHEIGHT = PADDLE_HEIGHT / 2; 
        
        // calculate the penetration on each axis
        float penRX = PADDLE_HALFWIDTH + BALL_RADIUS - Math.abs(ballX - (GAME_WIDTH - 15));
        float penRY = PADDLE_HALFHEIGHT + BALL_RADIUS - Math.abs(ballY - (playerRY + PADDLE_HALFHEIGHT));
        float penLX = PADDLE_HALFWIDTH + BALL_RADIUS - Math.abs(ballX - 15);
        float penLY = PADDLE_HALFHEIGHT + BALL_RADIUS - Math.abs(ballY - (playerLY + PADDLE_HALFHEIGHT));
        
        if (penRX > 0 && penRY > 0) { // hit right paddle
            ballVX = -ballVX;
            if (penRX < penRY) {
                ballX -= penRX;
            } else {
                ballY += (ballY > playerRY) ? penRY : -penRY;
                ballVY = -ballVY;
            }
        } else if (penLX > 0 && penLY > 0) { // hit left paddle
            ballVX = -ballVX;
            if (penLX < penLY) {
                ballX += penLX;
            } else {
                ballY += (ballY > playerLY) ? penLY : -penLY;
                ballVY = -ballVY;
            }
        }
        
        // increase the speed of the ball with every hit
        if ((penRX > 0 && penRY > 0) || (penLX > 0 && penLY > 0)) {
            ballVX *= BALL_SPEED_INCREASE;
            ballVY *= BALL_SPEED_INCREASE;
        }
        
        // check for points scored
        if (ballX < 0) {
            playerRScore++;
            state = GameState.POINTSCORED;
        } else if (ballX > GAME_WIDTH) {
            playerLScore++;
            state = GameState.POINTSCORED;
        }
    }
    
    /**
     * Runs the artificial stupidity calculations for this frame.
     */
    private void updateAI() {
        float paddleDest;
        
        if (ballVX > 0) {
            // ball is moving toward AI, move paddle to ball
            paddleDest = ballY - PADDLE_HEIGHT / 2;
        } else {
            // ball is moving away, move paddle back to centre
            paddleDest = GAME_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        }
        
        if (playerRY > paddleDest && playerRY > 20) {
            playerRY -= PADDLE_SPEED;
        } else if (playerRY < paddleDest && playerRY + PADDLE_HEIGHT < GAME_HEIGHT - 20) {
            playerRY += PADDLE_SPEED;
        }
    }
    
    /**
     * Synchrnoizes the display to the desired frame rate.
     */
    private void sync() {
        long timeOfNextFrame = (1000000000l / GAME_FPS) + timeThen;
        timeNow = System.nanoTime();
        
        while(timeOfNextFrame > timeNow + timeLate) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) { }
            timeNow = System.nanoTime();
        }
        
        if (timeNow > timeOfNextFrame) {
            timeLate = timeNow - timeOfNextFrame;
        } else {
            timeLate = 0;
        }
        
        timeThen = timeNow;
    }

    /**
     * Entry point to the application.
     */
    public static void main(String[] args) {
        Pong p = new Pong();
    }
}
