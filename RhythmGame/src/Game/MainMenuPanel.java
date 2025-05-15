package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;

public class MainMenuPanel extends JPanel {
    private JButton startButton;
    private JLabel bpmLabel;
    private JLabel durationLabel;
    private Game game;
    private int selectedSpeed = 10;
    private int selectedBPM = 110;
    private JComboBox<String> songComboBox;
    private String[] songFiles;
    private String selectedDifficulty = "EASY";

    public MainMenuPanel(Game game) {
        try {
            if (game == null) {
                throw new IllegalArgumentException("Game cannot be null");
            }

            this.game = game;
            setLayout(new BorderLayout());

            // 제목 라벨 - 크기 증가
            JLabel titleLabel = new JLabel("간단한 4키 리듬게임", SwingConstants.CENTER);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            titleLabel.setForeground(Color.BLUE);

            // 시작 버튼 - 크기 증가
            startButton = new JButton("게임 시작");
            startButton.setFont(new Font("맑은 고딕", Font.BOLD, 30));
            startButton.setPreferredSize(new Dimension(300, 100));
            startButton.addActionListener(e -> {
                try {
                    String selectedSong = (String) songComboBox.getSelectedItem();
                    game.startGame(selectedSpeed, songFiles[songComboBox.getSelectedIndex()], selectedBPM, selectedDifficulty);
                } catch (Exception ex) {
                    System.err.println("Error starting game: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "게임 시작 중 오류 발생 : " + ex.getMessage(),
                            "오류", JOptionPane.ERROR_MESSAGE);
                }
            });

            // 노트 속도 슬라이더
            JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 5, 15, 10);
            speedSlider.setMajorTickSpacing(5);
            speedSlider.setMinorTickSpacing(1);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
            speedSlider.addChangeListener(e -> selectedSpeed = speedSlider.getValue());

            // 속도 라벨 - 크기 증가
            JLabel speedLabel = new JLabel("노트 속도");
            speedLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            // BPM 라벨 - 크기 증가
            bpmLabel = new JLabel("BPM : " + selectedBPM);
            bpmLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            // 재생 시간 라벨 - 크기 증가
            durationLabel = new JLabel("음악 시간 : 00:00");
            durationLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            // 노래 선택 라벨 - 크기 증가
            JLabel songLabel = new JLabel("노래 선택");
            songLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            // 난이도 선택 라벨 - 크기 증가
            JLabel difficultyLabel = new JLabel("난이도 선택");
            difficultyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

            // 난이도 라디오 버튼 - 크기 증가
            JPanel difficultyPanel = new JPanel();
            ButtonGroup difficultyGroup = new ButtonGroup();
            JRadioButton easyButton = new JRadioButton("쉬움", true);
            JRadioButton normalButton = new JRadioButton("보통");
            JRadioButton hardButton = new JRadioButton("어려움");

            easyButton.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
            normalButton.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
            hardButton.setFont(new Font("맑은 고딕", Font.PLAIN, 20));

            easyButton.addActionListener(e -> selectedDifficulty = "EASY");
            normalButton.addActionListener(e -> selectedDifficulty = "NORMAL");
            hardButton.addActionListener(e -> selectedDifficulty = "HARD");

            difficultyGroup.add(easyButton);
            difficultyGroup.add(normalButton);
            difficultyGroup.add(hardButton);

            difficultyPanel.add(easyButton);
            difficultyPanel.add(normalButton);
            difficultyPanel.add(hardButton);

            // songs 폴더 내 wav 파일 목록 불러오기
            File songsDir = new File("songs");
            if (!songsDir.exists()) {
                songsDir.mkdir();
            }

            File[] songFilesArray = songsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            songFiles = new String[songFilesArray != null ? songFilesArray.length : 0];
            String[] songNames = new String[songFiles.length];

            for (int i = 0; i < songFiles.length; i++) {
                songFiles[i] = songFilesArray[i].getName();
                songNames[i] = songFiles[i].replace(".wav", "");
            }

            if (songFiles.length == 0) {
                songFiles = new String[]{"song.wav"};
                songNames = new String[]{"기본 노래"};
            }

            songComboBox = new JComboBox<>(songNames);
            songComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
            songComboBox.setPreferredSize(new Dimension(400, 40));  // 크기 증가
            songComboBox.addActionListener(e -> {
                updateBPM();
                updateDuration();
            });

            // 속도 조절 패널
            JPanel speedControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            speedControlPanel.add(speedLabel);
            speedControlPanel.add(speedSlider);

            // BPM 및 시간 표시 패널
            JPanel bpmControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bpmControlPanel.add(bpmLabel);
            bpmControlPanel.add(durationLabel);

            // 노래 선택 패널
            JPanel songSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            songSelectPanel.add(songLabel);
            songSelectPanel.add(songComboBox);

            // 난이도 선택 패널
            JPanel difficultySelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            difficultySelectPanel.add(difficultyLabel);
            difficultySelectPanel.add(difficultyPanel);

            // 설정 패널 묶기
            JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 20));  // 간격 증가
            settingsPanel.add(speedControlPanel);
            settingsPanel.add(bpmControlPanel);
            settingsPanel.add(songSelectPanel);
            settingsPanel.add(difficultySelectPanel);

            // 버튼 패널
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(startButton);

            // 메인 레이아웃에 추가
            add(titleLabel, BorderLayout.NORTH);
            add(settingsPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // 초기화 시 BPM/재생시간 갱신
            updateBPM();
            updateDuration();

        } catch (Exception e) {
            System.err.println("Error initializing MainMenuPanel : " + e.getMessage());
            throw e;
        }
    }

    private void updateBPM() {
        int selectedIndex = songComboBox.getSelectedIndex();
        if (selectedIndex == 0) {
            selectedBPM = 110;
        } else if (selectedIndex == 1) {
            selectedBPM = 120;
        } else if (selectedIndex == 2) {
            selectedBPM = 80;
        } else {
            selectedBPM = 100;
        }
        bpmLabel.setText("BPM : " + selectedBPM);
    }

    private void updateDuration() {
        try {
            int index = songComboBox.getSelectedIndex();
            File file = new File("songs", songFiles[index]);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            float durationInSeconds = frames / format.getFrameRate();

            int minutes = (int) (durationInSeconds / 60);
            int seconds = (int) (durationInSeconds % 60);
            durationLabel.setText(String.format("    음악 시간 : %02d:%02d", minutes, seconds));
        } catch (Exception e) {
            System.err.println("Error calculating duration: " + e.getMessage());
            durationLabel.setText("음악 시간 : 알 수 없음");
        }
    }
}