import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import javax.sound.sampled.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class Ugaday extends JFrame {

    private final JTextField pole;
    private final JLabel description;
    private final JProgressBar progress;
    private final JTextArea history;
    private int theNumber;
    private int attempts;
    private int maxAttempts;
    private final JButton step;
    private final ExecutorService soundExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
    private static final Color COLOR_HINT_UP = Color.CYAN;
    private static final Color COLOR_HINT_DOWN = Color.PINK;
    private static final Color COLOR_WIN = Color.GREEN;
    private static final Color COLOR_LOSE = Color.RED;
    private static final int MAX_DIGITS = 3;
    private boolean gameActive;
    private final Map<String, Clip> soundCache = new HashMap<>();
    private static final Pattern DIGITS = Pattern.compile("\\d*");
    private final Color defaultFieldColor;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;
    private final JComboBox<Difficulty> difficultyBox;

    private void loadSound(String fileName) {
        try {
            var url = getClass().getResource(fileName);
            if (url == null) {
                System.out.println("Файл не найден: " + fileName);
                return;
            }

            try (AudioInputStream audioIn =
                         AudioSystem.getAudioInputStream(url)) {

                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                soundCache.put(fileName, clip);
            }

        } catch (Exception e) {
            System.out.println("Ошибка загрузки звука: " + fileName);
        }
    }

    private void playSound(String fileName) {
        synchronized (soundCache) {
            Clip clip = soundCache.get(fileName);
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void updateStepState() {
        step.setEnabled(gameActive && !pole.getText().isBlank());
    }

    private void playAsync(String file) {
        if (!soundExecutor.isShutdown()) {
            soundExecutor.execute(() -> playSound(file));
        }
    }

    public Ugaday() {
        setTitle("Угадай число 3.2");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        loadSound("sounds/win.wav");
        loadSound("sounds/lose.wav");
        loadSound("sounds/error.wav");
        loadSound("sounds/beep.wav");

        // Верхняя панель
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton newGameButton = new JButton("Новая игра");
        topPanel.add(newGameButton);
        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setPreferredSize(new Dimension(300, 25));
        topPanel.add(progress);
        add(topPanel, BorderLayout.NORTH);
        progress.setMinimum(0);
        difficultyBox = new JComboBox<>(Difficulty.values());
        difficultyBox.setSelectedItem(currentDifficulty);
        topPanel.add(new JLabel("Сложность:"));
        topPanel.add(difficultyBox);
        difficultyBox.addActionListener(_ -> {
            currentDifficulty = (Difficulty) difficultyBox.getSelectedItem();
            startNewGame();
        });


        // Центр
        var centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel write = new JLabel("Введите число:", SwingConstants.RIGHT);
        pole = new JTextField();
        defaultFieldColor = pole.getBackground();

        pole.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStepState(); }
            public void removeUpdate(DocumentEvent e) { updateStepState(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        ((AbstractDocument) pole.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int offset,
                                     String string, AttributeSet attr)
                    throws BadLocationException {
                if (string != null &&
                        DIGITS.matcher(string).matches()) {
                    int newLength = fb.getDocument().getLength() + string.length();
                    if (newLength <= MAX_DIGITS)
                        super.insertString(fb, offset, string, attr);

                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length,
                                String text, AttributeSet attrs)
                    throws BadLocationException {

                if (text != null &&
                        DIGITS.matcher(text).matches()) {
                    int newLength = fb.getDocument().getLength() - length + text.length();
                    if (newLength <= MAX_DIGITS)
                        super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        step = new JButton("Ход");
        description = new JLabel("Начните угадывать число!", SwingConstants.CENTER);
        description.setOpaque(true);
        step.setEnabled(false);
        description.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
        history.setLineWrap(true);
        history.setWrapStyleWord(true);

        // Hover эффект для кнопок
        addHoverEffect(step, Color.LIGHT_GRAY);
        addHoverEffect(newGameButton, Color.ORANGE);

        // Действия кнопок
        newGameButton.addActionListener(_ -> startNewGame());
        step.addActionListener(_ -> checkGuess());
        pole.addActionListener(_ -> checkGuess());

        startNewGame();
    }

    private void addHoverEffect(JButton button, Color hover) {
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private enum Difficulty {
        EASY("Лёгкий", 1, 50, 12),
        MEDIUM("Средний", 1, 100, 10),
        HARD("Сложный", 1, 500, 8);

        final String title;
        final int min;
        final int max;
        final int attempts;

        Difficulty(String title, int min, int max, int attempts) {
            this.title = title;
            this.min = min;
            this.max = max;
            this.attempts = attempts;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private void startNewGame() {
        history.setText("");
        theNumber = ThreadLocalRandom.current()
                .nextInt(currentDifficulty.min, currentDifficulty.max + 1);
        maxAttempts = currentDifficulty.attempts;
        attempts = 0;
        //score = 0;
        pole.setEditable(true);
        description.setText("Введите число от " + currentDifficulty.min + " до " + currentDifficulty.max + " и нажмите \"Ход\"");
        description.setBackground(Color.WHITE);
        description.setForeground(Color.BLACK);
        progress.setMaximum(maxAttempts);
        progress.setValue(0);
        progress.setString("0 / " + maxAttempts);
        SwingUtilities.invokeLater(pole::requestFocusInWindow);
        pole.setText("");
        pole.setBackground(defaultFieldColor);
        pole.setFocusable(true);
        gameActive = true;
        updateStepState();

    }

    private void checkGuess() {
        if (!gameActive) return;
        description.setForeground(Color.BLACK);
        String input = pole.getText();
        int guess;
        if (input.isEmpty()) {
            description.setText("Введите число!");
            description.setBackground(Color.PINK);
        }
        try {
            guess = Integer.parseInt(input);
        }
        catch(NumberFormatException e) {
            description.setText("❌ Введите число!");
            description.setBackground(Color.PINK);
            pole.setText("");
            return;
        }
        if (guess < currentDifficulty.min || guess > currentDifficulty.max) {
            description.setText("Введите число от " + currentDifficulty.min + " до " + currentDifficulty.max + "!");
            description.setBackground(Color.YELLOW);
            pole.setText("");
            return;
        }

        attempts++;
        progress.setValue(attempts);
        progress.setString(attempts + " / " + maxAttempts);
        String feedback;
        if (guess == theNumber) {
            feedback = "🎉 Правильно! Вы угадали за " + attempts + " попыток!";
            description.setBackground(COLOR_WIN);
            pole.setBackground(Color.LIGHT_GRAY);
            playAsync("sounds/win.wav");
            description.setText(feedback);
            addToHistory(guess + " -> Победа!");
            pole.setText("");
            gameActive = false;
            pole.setEditable(false);
            pole.setFocusable(false);
            updateStepState();
            //score = Math.max(0, 100 - attempts*10);
            return;
            }
        else if (attempts >= maxAttempts) {
            description.setText("💀 Попытки закончились! Число было " + theNumber);
            description.setBackground(COLOR_LOSE);
            description.setForeground(Color.WHITE);
            pole.setBackground(Color.LIGHT_GRAY);
            playAsync("sounds/lose.wav");
            addToHistory(guess + " -> Не угадали");
            addToHistory("Число было: " + theNumber);
            pole.setText("");
            gameActive = false;
            pole.setEditable(false);
            pole.setFocusable(false);
            updateStepState();
            return;
        }
        else if (guess < theNumber) {
            feedback = "⬆ Больше";
            description.setBackground(COLOR_HINT_UP);
            playAsync("sounds/beep.wav");
        }
        else {
            feedback = "⬇ Меньше";
            description.setBackground(COLOR_HINT_DOWN);
            playAsync("sounds/beep.wav");
        }

        description.setText(feedback);
        addToHistory(guess + " -> " + feedback);
        pole.setText("");
    }

    private void addToHistory(String text) {
        history.append(text + "\n");
        history.setCaretPosition(history.getDocument().getLength());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Ugaday().setVisible(true));
    }
    @Override
    public void dispose() {
        soundExecutor.shutdownNow();

        for (Clip clip : soundCache.values()) {
            clip.stop();
            clip.close();
        }

        super.dispose();
    }
}
//Проверь код на ошибки и подробно расскажи как их исправить