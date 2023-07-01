package Ping;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PingPongGame extends Frame implements Runnable, KeyListener, MouseListener, WindowListener {

    ThreadLocalRandom rnd;
    Thread thread;

    private Paddle hPaddle, cPaddle;
    private Ball ball;

    public static final int WIDTH = 1000, HEIGHT = 500;

    public static final Font FONT = new Font("Consolas", Font.BOLD, 24);

    int bWidth = 200, bHeight = 50, buttonX = 400;

    int singleY = 100;
    int multiY = 200;
    int aiY = 300;

    int pWidth = 70, pHeight = 15;
    int hPlayerX = 950;

    private Set<Paddle> paddles;

    private BufferedImage img;
    private Graphics gfx;

    private int p1Score = 0, p2Score = 0;

    private int winner, hits;

    private Status status;

    private boolean mousePressed = false;

    private double prevMouseX, prevMouseY;

    private List<Button> buttons;

    public static void main(String[] args) {
        PingPongGame game = new PingPongGame();
        game.setSize(WIDTH + 15, HEIGHT + 33);
        // game.setUndecorated(true);
        game.setVisible(true);
        game.setLayout(new FlowLayout());
        game.setTitle("Pong");
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PingPongGame() {
        hits = 0;
        rnd = ThreadLocalRandom.current();
        paddles = new HashSet<Paddle>();
        buttons = new ArrayList<>();

        ball = new Ball(this, 15, 5, 5);

        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        gfx = img.getGraphics();

        gfx.setFont(FONT);

        buttons.add(new Button("Single Player", FONT, Color.YELLOW, Color.BLACK, buttonX, singleY, bWidth, bHeight) {
            @Override
            public void onClick() {
                registerPlayers(1);
                start();
                super.onClick();
            }
        });
        buttons.add(new Button("Multi Player", FONT, Color.YELLOW, Color.BLACK, buttonX, multiY, bWidth, bHeight) {
            @Override
            public void onClick() {
                registerPlayers(2);
                start();
                super.onClick();
            }
        });
        buttons.add(new Button("Self Player", FONT, Color.YELLOW, Color.BLACK, buttonX, aiY, bWidth, bHeight) {
            @Override
            public void onClick() {
                registerPlayers(0);
                start();
                super.onClick();
            }
        });

        addKeyListener(this);
        addMouseListener(this);
        addWindowListener(this);

        thread = new Thread(this);
        thread.start();

        status = Status.MENU;
    }

    public void registerPlayers(int human) {
        switch (human) {
            case 0:
                hPaddle = new AI(this, Color.WHITE, hPlayerX, pWidth, pHeight, ball, true, 50, hPlayerX, 1);
                cPaddle = new AI(this, Color.WHITE, 30, pWidth, pHeight, ball, false, 50, hPlayerX, 1);
                break;
            case 1:
                cPaddle = new AI(this, Color.YELLOW, 30, pWidth, pHeight, ball, false, 50, hPlayerX, 0.03);
                hPaddle = new Paddle(this, Color.WHITE, hPlayerX, pWidth, pHeight);
                break;
            case 2:
                cPaddle = new Paddle(this, Color.WHITE, 30, pWidth, pHeight);
                hPaddle = new Paddle(this, Color.WHITE, hPlayerX, pWidth, pHeight);
                break;
            default:
        }
        paddles.clear();
        paddles.add(cPaddle);
        paddles.add(hPaddle);
    }

    @Override
    public void paint(Graphics g) {

        drawBackground(gfx);

        if (status == Status.MENU) {
            drawButtons(gfx);

            g.drawImage(img, 0, 0, this);
            return;
        }

        drawLines(gfx);

        if (status == Status.SCORE) {
            gfx.setColor(Color.GREEN);
            gfx.drawString("Player " + winner + " Scored", 450, 250);
            gfx.setFont(FONT.deriveFont(18f));
            gfx.drawString("Press Enter To Continue", 400, 280);
            gfx.setFont(FONT);
        } else {
            drawBall(gfx);
            drawPaddles(gfx);

            if (status == Status.PAUSE) {
                gfx.setColor(Color.blue);
                gfx.setFont(FONT.deriveFont(FONT.getSize() * 3));
                gfx.drawString("Game Paused", 420, 250);
                gfx.setFont(FONT);
                gfx.drawString("Press Enter To Continue", 360, 300);
                gfx.setFont(FONT.deriveFont(FONT.getSize() * 2));
                gfx.drawString("(ESC) To Exit (SPACE) Menu", 350, 340);
            }

        }

        int sm = manageTextAndScores(gfx);
        if (sm != 0) {
            status = Status.SCORE;
            hits = 0;
            resetPositions();
            winner = sm;
        }
        g.drawImage(img, 10, 33, this);
    }

    public void start() {
        status = Status.RUNNING;
        hits = 0;
        p1Score = 0;
        p2Score = 0;
        resetPositions();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public Status getStatus() {
        return status;
    }

    private int manageTextAndScores(Graphics g) {

        g.setColor(Color.BLUE);
        g.drawString(p1Score + "", 250, 65);
        if (cPaddle instanceof AI) {
            g.drawString("(" + ((AI) cPaddle).getSkill() + ")", 250, 90);
        }

        g.setColor(Color.RED);
        g.drawString(p2Score + "", 750, 65);
        if (hPaddle instanceof AI) {
            g.drawString("(" + ((AI) hPaddle).getSkill() + ")", 750, 90);
        }

        g.setColor(Color.GRAY);
        g.drawString("" + hits, 500, 50);

        g.setColor(Color.WHITE);

        if (ball.getX() <= 0) {
            p2Score++;
            return 2;
        } else if (ball.getX() >= WIDTH - ball.getSize()) {
            p1Score++;
            return 1;
        }
        return 0;
    }

    public void resetPositions() {
        ball.reset();
        cPaddle.resetPosition();
        hPaddle.resetPosition();
    }

    public void drawBackground(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    public void drawLines(Graphics g) {
        // int lineWidth = 2, lineHeight = 30, lineGap = 15;

        // g.setColor(Color.WHITE);

        // for (int y = 0; y < HEIGHT; y += lineHeight + lineGap) {
        // g.fillRect(WIDTH / 2 - (lineWidth / 2), y, lineWidth, lineHeight);
        // }
        g.setColor(Color.white);
        g.drawLine(0, 15, 1000, 15);
        g.drawLine(0, HEIGHT - 30, WIDTH - 10, HEIGHT - 30);
        g.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        g.drawLine(20, 0, 20, HEIGHT);
        g.drawLine(980, 0, 980, HEIGHT);
    }

    public void drawPaddles(Graphics g) {
        cPaddle.draw(g);
        hPaddle.draw(g);
    }

    public void drawBall(Graphics g) {
        ball.draw(g);
    }

    public void drawButtons(Graphics g) {
        buttons.forEach((b) -> b.draw(g));
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void run() {
        while (true) {
            if (status == Status.RUNNING) {
                cPaddle.move();
                hPaddle.move();
                ball.move();

                setTitle("Pong " + p1Score + " | " + p2Score);

                if (mousePressed && getMousePosition() != null) {
                    double velX = getMousePosition().getX() - prevMouseX, velY = getMousePosition().getY() - prevMouseY;
                    ball.setX((int) getMousePosition().getX() - ball.getSize() / 2);
                    ball.setY((int) getMousePosition().getY() - ball.getSize() / 2);
                    // ball.setXVel(ball.getXVel() + velX / 10);
                    // ball.setYVel(ball.getYVel() + velY / 10);
                    ball.setXVel(5);
                    ball.setYVel(5);
                    prevMouseX = getMousePosition().getX();
                    prevMouseY = getMousePosition().getY();
                }

                if (ball.checkCollision(paddles)) {
                    hits++;
                }

            }
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent key) {

    }

    @Override
    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (!(hPaddle instanceof AI))
                    hPaddle.setYVel(-5);
                break;
            case KeyEvent.VK_DOWN:
                if (!(hPaddle instanceof AI))
                    hPaddle.setYVel(5);
                break;
            case KeyEvent.VK_W:
                if (!(cPaddle instanceof AI))
                    cPaddle.setYVel(-5);
                break;
            case KeyEvent.VK_S:
                if (!(cPaddle instanceof AI))
                    cPaddle.setYVel(5);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                switch (status) {
                    case SCORE:
                        ball.reset();
                    case PAUSE:
                        status = Status.RUNNING;
                        break;
                    case RUNNING:
                        status = Status.PAUSE;
                        break;
                    default:
                        break;
                }
                break;
            case KeyEvent.VK_SPACE:
                if (status == Status.PAUSE)
                    status = Status.MENU;
                break;
            case KeyEvent.VK_ESCAPE:
                if (status != Status.PAUSE && status != Status.MENU)
                    break;
                dispose();
                System.exit(0);
                break;
        }

        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
            if (status == Status.PAUSE) {
                status = Status.MENU;
            }
        }

        if (!(cPaddle instanceof AI)) {
            if ((key.getKeyCode() == KeyEvent.VK_W || key.getKeyCode() == KeyEvent.VK_S)) {
                cPaddle.setYVel(0);
            }
        }

        if (!(key.getKeyCode() == KeyEvent.VK_UP || key.getKeyCode() == KeyEvent.VK_DOWN))
            return;

        hPaddle.setYVel(0);
    }

    @Override
    public void mouseClicked(MouseEvent e) { // Let go
        int x = e.getX(), y = e.getY();

        if (e.getButton() != MouseEvent.BUTTON1)
            return;

        buttons.stream().filter((button) -> button.contains(x, y)).forEach(Button::onClick);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (status != Status.RUNNING)
            return;
        ball.setXVel(0);
        ball.setYVel(0);
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}