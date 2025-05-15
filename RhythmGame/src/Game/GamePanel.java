package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private List<Note> notes;
    private String judgementText = "";
    private int judgementDisplayCounter = 0;
    private int combo = 0;
    private int noteSpeed;

    private int score = 0;
    private int perfectCount = 0;
    private int goodCount = 0;
    private int missCount = 0;
    private long gameStartTime;
    private boolean gameEnded = false;

    private static final int JUDGE_LINE_Y = 450;
    private static final int NOTE_WIDTH = 80;
    private static final int NOTE_HEIGHT = 40;
    private final int[] lanes = {200, 300, 400, 500};

    private boolean[] keyBeam = new boolean[4];
    private long lastNoteTime = 0;
    private Clip musicClip;
    private String songFile;
    private Game game;

    private String countdownText = "";
    private boolean musicStarted = false;
    private long musicStartTime = 0;

    private int bpm;
    private long noteInterval;
    private String difficulty;
    private Random random = new Random();

    // 체력바 관련 변수
    private int maxHealth = 100;
    private int currentHealth = maxHealth;
    private int healthBarWidth = 20;
    private int healthBarHeight = 200;

    // 잘못된 키 입력 관련 변수
    private boolean wrongKeyPressed = false;
    private int wrongKeyCounter = 0;

    // 음악 총 길이 저장 변수 추가
    private long musicTotalMicroseconds = 0;

    // 사용자 정의 예외 클래스
    class InvalidKeyException extends Exception {
        public InvalidKeyException(String message) {
            super(message);
        }
    }
    
    public boolean isHealthDepleted() {
        return currentHealth <= 0;
    }

    public GamePanel(int speed, String songFile, Game game, int bpm, String difficulty) {
        this.noteSpeed = speed;
        this.songFile = songFile;
        this.game = game;
        this.bpm = bpm;
        this.difficulty = difficulty;
        this.noteInterval = calculateNoteInterval(bpm);
        setFocusable(true);
        addKeyListener(this);
        notes = new ArrayList<>();
        timer = new Timer(10, this);
        gameStartTime = System.currentTimeMillis();
        timer.start();
        loadAndPrepareMusic();
    }

    private long calculateNoteInterval(int bpm) {
        if ("HARD".equalsIgnoreCase(difficulty)) {
            return 60000 / bpm / 2;
        }
        return 60000 / bpm;
    }

    private void loadAndPrepareMusic() {
        try {
            String musicPath = "songs/" + songFile;
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new java.io.File(musicPath));
            musicClip = AudioSystem.getClip();
            musicClip.open(audioInput);

            // 음악 총 길이 저장
            musicTotalMicroseconds = (long)(audioInput.getFrameLength() * 1_000_000 / audioInput.getFormat().getFrameRate());

            musicClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && musicStarted && !gameEnded) {
                    gameEnded = true;
                    timer.stop();
                    musicClip.close();
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {}
                        SwingUtilities.invokeLater(() -> game.showResult(score, perfectCount, goodCount, missCount));
                    }).start();
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading music : " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 배경 하얀색으로 설정
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 잘못된 키 입력 시 빨간색 오버레이
        if (wrongKeyPressed) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 노트 레인 그리기
        g.setColor(new Color(240, 240, 240));
        for (int lane : lanes) {
            g.fillRect(lane, 0, NOTE_WIDTH, JUDGE_LINE_Y + NOTE_HEIGHT);
        }

        // 판정선 그리기
        g.setColor(Color.RED);
        g.fillRect(0, JUDGE_LINE_Y, getWidth(), NOTE_HEIGHT);

        // 노트 그리기
        for (Note note : notes) {
            note.draw(g);
        }

        // 키 빔 효과 그리기
        for (int i = 0; i < lanes.length; i++) {
            if (keyBeam[i]) {
                g.setColor(new Color(255, 255, 0, 128));
                g.fillRect(lanes[i], JUDGE_LINE_Y, NOTE_WIDTH, NOTE_HEIGHT);
            }
        }

        // 판정 텍스트
        if (!judgementText.isEmpty()) {
            g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            if (judgementText.startsWith("잘못된 키")) {
                g.setFont(new Font("맑은 고딕", Font.BOLD, 34));
                g.setColor(Color.RED);
                g.drawString(judgementText, 20, 150);
            } else {
                g.setColor(Color.BLACK);
                g.drawString(judgementText, getWidth() / 2 - 80, 300);
            }
        }

        // 콤보 표시
        g.setColor(Color.BLACK);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        g.drawString("Combo " + combo, getWidth() / 2 - 120, 100);

        // 속도 및 점수 표시
        g.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        g.drawString("Speed: " + noteSpeed, 20, 40);
        g.drawString("Score: " + score, 20, 80);

        // 체력바 그리기
        drawHealthBar(g);

        // 카운트다운 텍스트
        if (!countdownText.isEmpty()) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 80));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(countdownText);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;

            g.drawString(countdownText, x, y);
        }

        // 사용 가능한 키 안내 메시지
        g.setColor(Color.BLACK);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        g.drawString("사용 키: D, F, J, K", 20, getHeight() - 30);
    }

    private void drawHealthBar(Graphics g) {
        int x = lanes[lanes.length - 1] + NOTE_WIDTH + 20;
        int y = 80;

        // 배경 그리기
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, healthBarWidth, healthBarHeight);

        // 체력 비율에 따른 채워질 높이 계산
        int filledHeight = (int)(healthBarHeight * ((double)currentHealth / maxHealth));

        // 색상 결정
        Color healthColor;
        if (currentHealth > 60) {
            healthColor = Color.GREEN;
        } else if (currentHealth > 30) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.RED;
        }

        // 체력 채우기
        g.setColor(healthColor);
        g.fillRect(x, y + (healthBarHeight - filledHeight), healthBarWidth, filledHeight);

        // 테두리
        g.setColor(Color.BLACK);
        g.drawRect(x, y, healthBarWidth, healthBarHeight);

        // 체력 텍스트
        g.setColor(Color.BLACK);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        g.drawString("HP", x + 1, y - 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - gameStartTime;

        if (!musicStarted) {
            int secondsLeft = 3 - (int)(elapsed / 1000);
            countdownText = (secondsLeft > 0) ? String.valueOf(secondsLeft) : "Start!";
            repaint();

            if (elapsed >= 3000) {
                countdownText = "";
                musicStarted = true;
                musicStartTime = System.currentTimeMillis();
                musicClip.start();
            }
            return;
        }

        if (gameEnded) {
            countdownText = "Finished!";
            repaint();
            return;
        }

        // 잘못된 키 입력 효과 처리
        if (wrongKeyPressed && wrongKeyCounter > 0) {
            wrongKeyCounter--;
            if (wrongKeyCounter == 0) {
                wrongKeyPressed = false;
            }
        }

        // 음악 남은 시간 계산 (마이크로초 단위)
        long musicPositionMicros = musicClip.getMicrosecondPosition();
        long remainingMicros = musicTotalMicroseconds - musicPositionMicros;

        // 2초(2,000,000 마이크로초) 전에는 노트 생성하지 않음
        if (remainingMicros > 2_000_000 && musicStarted && currentTime - lastNoteTime >= noteInterval) {
            spawnNote();
            lastNoteTime = currentTime;
        }

        for (Note note : notes) {
            note.update(noteSpeed);
            if (note.isMissed(JUDGE_LINE_Y)) {
                note.setInactive();
                judgementText = "Miss";
                judgementDisplayCounter = 100;
                missCount++;
                combo = 0;
                decreaseHealth(5);
            }
        }

        if (judgementDisplayCounter > 0 && --judgementDisplayCounter == 0) {
            judgementText = "";
        }

        repaint();
    }

    private void spawnNote() {
        if ("EASY".equalsIgnoreCase(difficulty)) {
            int laneIndex = random.nextInt(lanes.length);
            notes.add(new Note(lanes[laneIndex], -NOTE_HEIGHT));
        } else if ("NORMAL".equalsIgnoreCase(difficulty) || "HARD".equalsIgnoreCase(difficulty)) {
            if (random.nextBoolean()) {
                int laneIndex = random.nextInt(lanes.length);
                notes.add(new Note(lanes[laneIndex], -NOTE_HEIGHT));
            } else {
                int lane1 = random.nextInt(lanes.length);
                int lane2;
                do {
                    lane2 = random.nextInt(lanes.length);
                } while (lane2 == lane1);

                notes.add(new Note(lanes[lane1], -NOTE_HEIGHT));
                notes.add(new Note(lanes[lane2], -NOTE_HEIGHT));
            }
        }
    }

    private void decreaseHealth(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        if (currentHealth <= 0) {
            gameEnded = true;
            timer.stop();
            musicClip.stop();
            SwingUtilities.invokeLater(() -> game.showResult(score, perfectCount, goodCount, missCount));
        }
    }

    private void increaseHealth(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int laneX = -1;

        try {
            switch (keyCode) {
                case KeyEvent.VK_D:
                    keyBeam[0] = true;
                    laneX = lanes[0];
                    break;
                case KeyEvent.VK_F:
                    keyBeam[1] = true;
                    laneX = lanes[1];
                    break;
                case KeyEvent.VK_J:
                    keyBeam[2] = true;
                    laneX = lanes[2];
                    break;
                case KeyEvent.VK_K:
                    keyBeam[3] = true;
                    laneX = lanes[3];
                    break;
                default:
                    throw new InvalidKeyException("잘못된 키를 눌렀습니다! (D, F, J, K만 사용 가능)");
            }
        } catch (InvalidKeyException ex) {
            judgementText = ex.getMessage();
            judgementDisplayCounter = 100;
            wrongKeyPressed = true;
            wrongKeyCounter = 20;
            repaint();
            return;
        }

        if (laneX != -1) {
            for (Note note : notes) {
                if (note.isActive() && note.getX() == laneX) {
                    String result = note.judge(JUDGE_LINE_Y);
                    if (result != null) {
                        judgementText = result;
                        judgementDisplayCounter = 100;

                        switch (result) {
                            case "Perfect":
                                score += 100;
                                perfectCount++;
                                combo++;
                                increaseHealth(3);
                                break;
                            case "Good":
                                score += 50;
                                goodCount++;
                                combo++;
                                increaseHealth(1);
                                break;
                            case "Miss":
                                missCount++;
                                combo = 0;
                                decreaseHealth(5);
                                break;
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D: keyBeam[0] = false; break;
            case KeyEvent.VK_F: keyBeam[1] = false; break;
            case KeyEvent.VK_J: keyBeam[2] = false; break;
            case KeyEvent.VK_K: keyBeam[3] = false; break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}