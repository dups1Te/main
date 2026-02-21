import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Game32 extends JFrame {

    private JTextField pole;
    private JLabel description;
    private JButton step, newGameButton;
    private JProgressBar progress;
    private JTextArea history;
    private int theNumber;
    private int attempts;
    private int maxAttempts;
    private int score;
    private ArrayList<String> pastGuesses = new ArrayList<>();

    private void playSound(String fileName) {
        try {
            // ищем файл в том же пакете, что и Game32.class
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(fileName));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Ошибка воспроизведения звука: " + fileName);
            e.printStackTrace();
        }
    }

    public Game32() {
        setTitle("Угадай число 3.2");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        newGameButton = new JButton("Новая игра");
        topPanel.add(newGameButton);
        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setPreferredSize(new Dimension(300, 25));
        topPanel.add(progress);
        add(topPanel, BorderLayout.NORTH);

        // Центр
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel write = new JLabel("Введите число:", SwingConstants.RIGHT);
        pole = new JTextField();
        step = new JButton("Ход");
        description = new JLabel("Начните угадывать число!", SwingConstants.CENTER);
        description.setOpaque(true);

        centerPanel.add(write);
        centerPanel.add(pole);
        centerPanel.add(step);
        centerPanel.add(description);
        add(centerPanel, BorderLayout.CENTER);

        // Правая панель - история и очки
        JPanel rightPanel = new JPanel(new BorderLayout(5,5));
        history = new JTextArea();
        history.setEditable(false);
        history.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(history);
        rightPanel.add(scroll, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Hover эффект для кнопок
        addHoverEffect(step, Color.LIGHT_GRAY, Color.WHITE);
        addHoverEffect(newGameButton, Color.ORANGE, Color.WHITE);

        // Действия кнопок
        newGameButton.addActionListener(e -> startNewGame());
        step.addActionListener(e -> checkGuess());
        pole.addActionListener(e -> checkGuess());

        startNewGame();
    }

    private void addHoverEffect(JButton button, Color hover, Color normal) {
        button.setBackground(normal);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            public void mouseExited(MouseEvent e) { button.setBackground(normal); }
        });
    }

    private void startNewGame() {
        pastGuesses.clear();
        history.setText("");
        theNumber = (int)(Math.random()*100 + 1);
        attempts = 0;
        maxAttempts = 10;
        score = 0;
        pole.setEditable(true);
        description.setText("Введите число и нажмите \"Ход\"");
        description.setBackground(Color.WHITE);
        progress.setMaximum(maxAttempts);
        progress.setValue(maxAttempts);
        SwingUtilities.invokeLater(() -> pole.requestFocusInWindow());
    }

    private void checkGuess() {
        String input = pole.getText();
        int guess;
        try {
            guess = Integer.parseInt(input);
        } catch(NumberFormatException e) {
            description.setText("❌ Введите число!");
            description.setBackground(Color.PINK);
            return;
        }

        attempts++;
        progress.setValue(maxAttempts - attempts);
        String feedback;
        if(guess < theNumber) {
            feedback = "⬆ Больше";
            description.setBackground(Color.CYAN);
            playSound("beep.wav");
        } else if(guess > theNumber) {
            feedback = "⬇ Меньше";
            description.setBackground(Color.PINK);
            playSound("sounds/beep.wav");
        } else {
            feedback = "🎉 Правильно! Вы угадали за " + attempts + " попыток!";
            description.setBackground(Color.GREEN);
            pole.setEditable(false);
            playSound("sounds/win.wav");
            score = Math.max(0, 100 - attempts*10);
        }

        pastGuesses.add(guess + " -> " + feedback);
        updateHistory();

        description.setText(feedback);

        if(attempts >= maxAttempts && guess != theNumber) {
            description.setText("💀 Попытки закончились! Число было " + theNumber);
            description.setBackground(Color.RED);
            pole.setEditable(false);
        }

        pole.setText("");
    }

    private void updateHistory() {
        history.setText("");
        for(String s: pastGuesses) {
            history.append(s + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game32().setVisible(true));
    }
}
