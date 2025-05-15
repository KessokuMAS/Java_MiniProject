package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public class ResultPanel extends JPanel {
    private JButton mainMenuButton;
    
    public ResultPanel(int score, int perfectCount, int goodCount, int missCount, Game game) {
        setLayout(new BorderLayout());
        
        // 결과 표시 패널 (6행으로 변경)
        JPanel resultPanel = new JPanel(new GridLayout(6, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // 배경에 흰색 설정 (투명 방지)
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        resultPanel.setBorder(BorderFactory.createEmptyBorder(80, 0, 80, 0));
        resultPanel.setOpaque(false);
        
        // 게임 오버 여부 확인
        boolean isGameOver = (game.getCurrentGamePanel() != null && game.getCurrentGamePanel().isHealthDepleted());
        
        if (isGameOver) {
            JLabel gameOverLabel = new JLabel("Game Over", SwingConstants.CENTER);
            gameOverLabel.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            gameOverLabel.setForeground(Color.RED);
            resultPanel.add(gameOverLabel);
        } else {
            JLabel clearLabel = new JLabel("CLEAR!", SwingConstants.CENTER);
            clearLabel.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            clearLabel.setForeground(Color.BLUE);
            resultPanel.add(clearLabel);
        }
        
        // 기존 점수 표시 라벨들
        resultPanel.add(createStyledLabel("최종 점수 : " + score, 40, Font.BOLD));
        resultPanel.add(createStyledLabel("Perfect : " + perfectCount, 30, Font.PLAIN));
        resultPanel.add(createStyledLabel("Good : " + goodCount, 30, Font.PLAIN));
        resultPanel.add(createStyledLabel("Miss : " + missCount, 30, Font.PLAIN));
        
        // 올퍼펙트/올콤보 메시지
        if (missCount == 0 && goodCount == 0 && perfectCount > 0) {
            resultPanel.add(new RainbowTextLabel("ALL PERFECT!", 36));
        } else if (missCount == 0 && (perfectCount > 0 || goodCount > 0)) {
            JLabel allComboLabel = new JLabel("ALL COMBO!", SwingConstants.CENTER);
            allComboLabel.setFont(new Font("맑은 고딕", Font.BOLD, 36));
            allComboLabel.setForeground(new Color(255, 165, 0)); // 주황색
            resultPanel.add(allComboLabel);
        } else {
            resultPanel.add(new JLabel("")); // 빈 공간
        }
        
        // 메인 메뉴 버튼
        mainMenuButton = new JButton("메인 메뉴로 돌아가기");
        mainMenuButton.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        mainMenuButton.setPreferredSize(new Dimension(300, 80));
        mainMenuButton.addActionListener(e -> game.showMainMenu());
        
        add(resultPanel, BorderLayout.CENTER);
        add(mainMenuButton, BorderLayout.SOUTH);
    }
    
    // 무지개 텍스트를 위한 커스텀 JLabel
    private static class RainbowTextLabel extends JLabel {
        public RainbowTextLabel(String text, int fontSize) {
            super(text, SwingConstants.CENTER);
            setFont(new Font("맑은 고딕", Font.BOLD, fontSize));
            setForeground(Color.WHITE); // 기본 텍스트 색상 (실제로는 무지개로 덮어씌움)
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            
            // 안티앨리어싱 적용
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 텍스트 레이아웃 계산
            FontMetrics fm = g2d.getFontMetrics();
            String text = getText();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            // 텍스트 도형으로 변환
            TextLayout tl = new TextLayout(text, getFont(), g2d.getFontRenderContext());
            Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
            
            // 무지개 그라데이션 생성
            float[] fractions = {0.0f, 0.16f, 0.33f, 0.5f, 0.66f, 0.83f, 1.0f};
            Color[] colors = {
                new Color(255, 0, 0),    // 빨강
                new Color(255, 165, 0),  // 주황
                new Color(255, 255, 0),  // 노랑
                new Color(0, 255, 0),    // 초록
                new Color(0, 0, 255),    // 파랑
                new Color(75, 0, 130),   // 남색
                new Color(238, 130, 238) // 보라
            };
            
            LinearGradientPaint rainbow = new LinearGradientPaint(
                new Point2D.Float(shape.getBounds().x, 0),
                new Point2D.Float(shape.getBounds().x + shape.getBounds().width, 0),
                fractions,
                colors
            );
            
            // 테두리 검정색으로 처리
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2f));
            g2d.draw(shape);
            
            // 글자 내부 무지개 색 채우기
            g2d.setPaint(rainbow);
            g2d.fill(shape);
            
            g2d.dispose();
        }
    }
    
    // 기본 스타일 라벨 생성 메서드
    private JLabel createStyledLabel(String text, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", fontStyle, fontSize));
        return label;
    }
}