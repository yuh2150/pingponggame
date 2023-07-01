package Ping;

import java.awt.*;
import java.awt.Graphics;
import java.util.Set;

public class Ball {
    private float size;
    private double x, y, xVel, yVel;
    private PingPongGame game;

    public Ball(PingPongGame game, float size, double xVel, double yVel) {
        this.size = size;
        this.xVel = xVel;
        this.yVel = yVel;
        this.game = game;
        reset();
    }

    public void reset() {
        x = game.getWidth() / 2 - (size / 2);
        y = game.getHeight() / 2 - (size / 2);

    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setXVel(double x) {
        xVel = x;
    }

    public void setYVel(double y) {
        yVel = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getXVel() {
        return xVel;
    }

    public double getYVel() {
        return yVel;
    }

    public int getSize() {
        return (int) size;
    }

    public void move() {
        x += xVel;
        y += yVel;
        if (y <= size) {
            yVel = -yVel;
            y = size;
        } else if (y >= game.getHeight() - size) {
            yVel = -yVel;
            y = game.getHeight() - size;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int) (x - size), (int) (y - size), (int) (2 * size), (int) (2 * size));

    }

    public boolean checkCollision(Set<Paddle> paddles) {
        for (Paddle paddle : paddles) {
            if (intersects(paddle)) {
                xVel = -xVel;
                yVel = yVel;
                return true;

            }
        }
        return false;
    }

    public boolean intersects(Paddle paddles) {
        int Ax = (int) x;
        int Ay = (int) y;
        int rect_left = paddles.getX();
        int rect_top = paddles.getY();
        int rect_right = paddles.getX() + paddles.getWidth();
        int rect_bottom = paddles.getY() + paddles.getHeight();
        if (x < rect_left)
            Ax = rect_left;
        else if (x > rect_right)
            Ax = rect_right;
        if (y < rect_top)
            Ay = rect_top;
        else if (y > rect_bottom)
            Ay = rect_bottom;
        int dx = (int) (x - Ax);
        int dy = (int) (y - Ay);
        return (dx * dx + dy * dy) <= size * size;
    }
}
