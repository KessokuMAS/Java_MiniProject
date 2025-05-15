package Game;

import java.awt.*;

public class Note {
    private int x, y;
    private boolean active;

    public Note(int x, int y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public void update(int speed) {
        if (active) {
            y += speed;
        }
    }

    public void draw(Graphics g) {
        if (active) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, 80, 40);  // 크기 조정
        }
    }

    public String judge(int judgeLineY) {
        if (!active) return null;

        int distance = Math.abs(y - judgeLineY);

        if (distance <= 40) {
            active = false;
            return "Perfect";
        } else if (distance <= 80) {
            active = false;
            return "Good";
        } else if (distance <= 120) {
            active = false;
            return "Miss";
        }

        return null;
    }

    public boolean isMissed(int judgeLineY) {
        return active && y > judgeLineY + 50;
    }

    public boolean isActive() {
        return active;
    }

    public void setInactive() {
        active = false;
    }

    public int getX() {
        return x;
    }
}