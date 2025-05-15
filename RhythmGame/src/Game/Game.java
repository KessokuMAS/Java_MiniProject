package Game;

import javax.swing.*;

public class Game extends JFrame {
    private GamePanel gamePanel;
    private MainMenuPanel mainMenuPanel;
    private ResultPanel resultPanel;

    public Game() {
        try {
            setTitle("4키 리듬게임");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(800, 600);  // 크기 변경
            setResizable(false);
            setLocationRelativeTo(null);

            showMainMenu();
            setVisible(true);
        } catch (Exception e) {
            System.err.println("Error initializing Game : " + e.getMessage());
            throw e;
        }
    }

    public void showMainMenu() {
        try {
            if (gamePanel != null) {
                getContentPane().remove(gamePanel);
                gamePanel.stopMusic();
            }
            if (resultPanel != null) {
                getContentPane().remove(resultPanel);
            }
            mainMenuPanel = new MainMenuPanel(this);
            setContentPane(mainMenuPanel);
            revalidate();
        } catch (Exception e) {
            System.err.println("Error showing main menu : " + e.getMessage());
            throw e;
        }
    }

    public void startGame(int speed, String songFile, int bpm, String difficulty) {
        try {
            if (mainMenuPanel != null) {
                getContentPane().remove(mainMenuPanel);
            }
            gamePanel = new GamePanel(speed, songFile, this, bpm, difficulty);
            setContentPane(gamePanel);
            gamePanel.requestFocusInWindow();
            revalidate();
        } catch (Exception e) {
            System.err.println("Error starting game : " + e.getMessage());
            throw e;
        }
    }

    public void showResult(int score, int perfectCount, int goodCount, int missCount) {
        resultPanel = new ResultPanel(score, perfectCount, goodCount, missCount, this);
        setContentPane(resultPanel);
        revalidate();
    }

    // 현재 GamePanel을 반환하는 메서드 추가
    public GamePanel getCurrentGamePanel() {
        return gamePanel;
    }
}