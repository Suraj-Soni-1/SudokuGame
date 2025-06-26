import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGame extends JFrame {
    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] solution = new int[SIZE][SIZE];
    private int[][] puzzle = new int[SIZE][SIZE];

    public SudokuGame() {
        setTitle("Sudoku Solver - Java Version");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Font font = new Font("Arial", Font.BOLD, 20);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(font);
                tf.setForeground(Color.BLACK);
                tf.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                final int r = row, c = col;

                tf.addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        char ch = e.getKeyChar();
                        if (!Character.isDigit(ch) || ch == '0') {
                            e.consume();
                            return;
                        }

                        SwingUtilities.invokeLater(() -> {
                            String text = tf.getText();
                            if (text.isEmpty()) return;
                            int val = Integer.parseInt(text);
                            if (!isValidInput(r, c, val)) {
                                tf.setForeground(Color.RED);
                                javax.swing.Timer timer = new javax.swing.Timer(400, (ActionEvent evt) -> {
                                    tf.setText("");
                                    tf.setForeground(Color.BLACK);
                                });
                                timer.setRepeats(false);
                                timer.start();
                            } else {
                                tf.setForeground(Color.BLACK);
                            }
                        });
                    }

                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_S) {
                            solvePuzzle(0, 0);
                            updateGridWithSolution();
                        }
                    }
                });

                cells[row][col] = tf;
                gridPanel.add(tf);
            }
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton resetBtn = new JButton("Reset");
        JButton newBtn = new JButton("New");

        resetBtn.addActionListener(e -> loadPuzzle(puzzle));
        newBtn.addActionListener(e -> generateAndLoadPuzzle());

        buttonPanel.add(resetBtn);
        buttonPanel.add(newBtn);

        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        generateAndLoadPuzzle();
    }

    private void loadPuzzle(int[][] board) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                int val = board[i][j];
                JTextField tf = cells[i][j];
                tf.setText(val == 0 ? "" : String.valueOf(val));
                tf.setEditable(val == 0);
                tf.setFocusable(val == 0); // 🔧 Prevent cursor/focus on fixed cells
                tf.setForeground(Color.BLACK);
            }
    }

    private void updateGridWithSolution() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                JTextField tf = cells[i][j];
                tf.setText(String.valueOf(solution[i][j]));
                tf.setEditable(false);
                tf.setFocusable(false);
                tf.setForeground(Color.BLUE);
            }
    }

    private boolean isValidInput(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (i != col && getValue(row, i) == num) return false;
            if (i != row && getValue(i, col) == num) return false;
        }
        int boxRow = row / 3 * 3;
        int boxCol = col / 3 * 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                int r = boxRow + i;
                int c = boxCol + j;
                if ((r != row || c != col) && getValue(r, c) == num)
                    return false;
            }
        return true;
    }

    private int getValue(int row, int col) {
        String text = cells[row][col].getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private void generateAndLoadPuzzle() {
        int[][] full = new int[SIZE][SIZE];
        generateFullBoard(full);
        solution = copyBoard(full);
        puzzle = copyBoard(full);
        removeCells(puzzle, 40);
        loadPuzzle(puzzle);
    }

    private boolean solvePuzzle(int row, int col) {
        if (row == SIZE) return true;
        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;
        if (solution[row][col] != 0)
            return solvePuzzle(nextRow, nextCol);
        for (int num = 1; num <= 9; num++) {
            if (isValid(solution, row, col, num)) {
                solution[row][col] = num;
                if (solvePuzzle(nextRow, nextCol))
                    return true;
                solution[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num)
                return false;
        }
        int boxRow = row / 3 * 3, boxCol = col / 3 * 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[boxRow + i][boxCol + j] == num)
                    return false;
        return true;
    }

    private void generateFullBoard(int[][] board) {
        fillBoard(board, 0, 0);
    }

    private boolean fillBoard(int[][] board, int row, int col) {
        if (row == SIZE) return true;
        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 9; i++) nums.add(i);
        Collections.shuffle(nums);
        for (int num : nums) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoard(board, nextRow, nextCol))
                    return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private void removeCells(int[][] board, int count) {
        Random rand = new Random();
        while (count > 0) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                count--;
            }
        }
    }

    private int[][] copyBoard(int[][] src) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(src[i], 0, copy[i], 0, SIZE);
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGame().setVisible(true));
    }
}