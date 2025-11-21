import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SurveyFrame extends JFrame {

    private ChatGptApi api;
    private Bot bot;

    private JRadioButton manualBtn;
    private JRadioButton chatBtn;

    private JTextField delayField;
    private JTextField topicField;

    private JTextArea q1Text, q2Text, q3Text;
    private JTextArea q1Ans, q2Ans, q3Ans;

    private JButton createBtn;
    private JTextArea resultsArea;

    public SurveyFrame(ChatGptApi api) {
        this.api = api;

        setTitle("יצירת סקר");
        setSize(820, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        Color bgColor      = new Color(255, 240, 245);
        Color cardColor1   = new Color(255, 230, 238);
        Color cardColor2   = new Color(255, 240, 230);
        Color cardColor3   = new Color(235, 245, 255);
        Color borderColor  = new Color(230, 190, 205);
        Color titleColor   = new Color(255, 170, 200);
        Color accentText   = new Color(245, 120, 170);

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 15));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 15));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 14));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(bgColor);
        setContentPane(main);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(bgColor);

        JLabel title = new JLabel("יצירת סקר", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(titleColor);
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        topWrapper.add(title, BorderLayout.NORTH);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(bgColor);

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        modeRow.setBackground(bgColor);
        modeRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        manualBtn = new JRadioButton("ידני");
        chatBtn = new JRadioButton("ChatGPT");
        manualBtn.setBackground(bgColor);
        chatBtn.setBackground(bgColor);
        manualBtn.setForeground(accentText);
        chatBtn.setForeground(accentText);

        ButtonGroup group = new ButtonGroup();
        group.add(manualBtn);
        group.add(chatBtn);
        manualBtn.setSelected(true);

        JLabel modeLbl = new JLabel("מצב יצירה:");
        modeLbl.setForeground(accentText);

        JLabel delayLbl = new JLabel("עיכוב בדקות:");
        delayLbl.setForeground(accentText);

        delayField = new JTextField("0", 3);
        delayField.setHorizontalAlignment(JTextField.CENTER);
        delayField.setBorder(new LineBorder(borderColor, 1, true));
        delayField.setPreferredSize(new Dimension(40, 28));

        modeRow.add(chatBtn);
        modeRow.add(manualBtn);
        modeRow.add(modeLbl);
        modeRow.add(Box.createHorizontalStrut(15));
        modeRow.add(delayField);
        modeRow.add(delayLbl);

        JPanel topicRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topicRow.setBackground(bgColor);
        topicRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel topicLbl = new JLabel("נושא ל-ChatGPT:");
        topicLbl.setForeground(accentText);

        topicField = new JTextField(20);
        topicField.setHorizontalAlignment(JTextField.CENTER);
        topicField.setBorder(new LineBorder(borderColor, 1, true));
        topicField.setPreferredSize(new Dimension(260, 30));

        topicRow.add(topicField);
        topicRow.add(topicLbl);

        top.add(modeRow);
        top.add(topicRow);

        topWrapper.add(top, BorderLayout.SOUTH);
        main.add(topWrapper, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(bgColor);

        q1Text = new JTextArea(2, 20);
        q1Ans = new JTextArea(3, 20);
        q2Text = new JTextArea(2, 20);
        q2Ans = new JTextArea(3, 20);
        q3Text = new JTextArea(2, 20);
        q3Ans = new JTextArea(3, 20);

        center.add(buildQuestionCard("שאלה 1", q1Text, q1Ans, cardColor1, borderColor, accentText));
        center.add(Box.createVerticalStrut(10));
        center.add(buildQuestionCard("שאלה 2 (לא חובה)", q2Text, q2Ans, cardColor2, borderColor, accentText));
        center.add(Box.createVerticalStrut(10));
        center.add(buildQuestionCard("שאלה 3 (לא חובה)", q3Text, q3Ans, cardColor3, borderColor, accentText));

        JScrollPane centerScroll = new JScrollPane(center);
        centerScroll.setBorder(null);
        centerScroll.getVerticalScrollBar().setUnitIncrement(12);
        main.add(centerScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(bgColor);

        createBtn = new JButton("צור ושלח סקר") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c1 = new Color(255, 189, 214);
                Color c2 = new Color(255, 170, 200);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                super.paintComponent(g);
                g2.dispose();
            }
        };
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setContentAreaFilled(false);
        createBtn.setBorder(new RoundedBorder(24));
        createBtn.setPreferredSize(new Dimension(190, 42));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(bgColor);
        btnPanel.add(createBtn);

        bottom.add(btnPanel, BorderLayout.NORTH);

        resultsArea = new JTextArea(5, 40);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        resultsArea.setBorder(new LineBorder(borderColor, 1, true));
        resultsArea.setForeground(accentText);
        resultsArea.setBackground(Color.WHITE);
        resultsArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane resScroll = new JScrollPane(resultsArea);
        resScroll.setPreferredSize(new Dimension(600, 140));

        JPanel resPanel = new JPanel();
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.Y_AXIS));
        resPanel.setBackground(bgColor);

        JLabel resLbl = new JLabel("תוצאות סקר:", SwingConstants.CENTER);
        resLbl.setForeground(accentText);
        resLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        resLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        resPanel.add(resLbl);
        resPanel.add(resScroll);
        resPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottom.add(resPanel, BorderLayout.CENTER);

        main.add(bottom, BorderLayout.SOUTH);

        createListeners();
    }

    private JPanel buildQuestionCard(String title,
                                     JTextArea qText,
                                     JTextArea qAns,
                                     Color cardColor,
                                     Color borderColor,
                                     Color accentText) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(650, 220));
        card.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(accentText);
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel qLbl = new JLabel("טקסט השאלה:");
        qLbl.setForeground(accentText);
        qLbl.setHorizontalAlignment(SwingConstants.CENTER);
        qLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aLbl = new JLabel("תשובות (כל תשובה בשורה):");
        aLbl.setForeground(accentText);
        aLbl.setHorizontalAlignment(SwingConstants.CENTER);
        aLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        qText.setBorder(new LineBorder(borderColor, 1, true));
        qText.setForeground(accentText);
        qText.setBackground(Color.WHITE);

        qAns.setLineWrap(true);
        qAns.setWrapStyleWord(true);
        qAns.setBorder(new LineBorder(borderColor, 1, true));
        qAns.setForeground(accentText);
        qAns.setBackground(Color.WHITE);

        JScrollPane qScroll = new JScrollPane(qText);
        qScroll.setPreferredSize(new Dimension(600, 50));
        qScroll.setMaximumSize(new Dimension(600, 50));
        qScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane aScroll = new JScrollPane(qAns);
        aScroll.setPreferredSize(new Dimension(600, 70));
        aScroll.setMaximumSize(new Dimension(600, 70));
        aScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(qLbl);
        card.add(qScroll);
        card.add(Box.createVerticalStrut(6));
        card.add(aLbl);
        card.add(aScroll);

        return card;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    private void createListeners() {
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (bot == null) {
                        JOptionPane.showMessageDialog(null, "הבוט לא מוגדר");
                        return;
                    }

                    if (bot.getCommunitySize() < 3) {
                        JOptionPane.showMessageDialog(null, "חייבים לפחות 3 חברים בקהילה");
                        return;
                    }

                    int delay = Integer.parseInt(delayField.getText());

                    Question[] qArr;

                    if (manualBtn.isSelected()) {
                        qArr = buildManualQuestions();
                    } else {
                        qArr = buildQuestionsFromChat();
                    }

                    if (qArr.length == 0) {
                        JOptionPane.showMessageDialog(null, "לא הוזנו שאלות");
                        return;
                    }

                    Survey survey = new Survey(qArr);

                    Long creatorId = bot.getAnyUser();
                    if (creatorId == null) {
                        JOptionPane.showMessageDialog(null, "אין משתמשים בקהילה");
                        return;
                    }

                    bot.startSurveyFromGui(survey, delay, creatorId);

                    JOptionPane.showMessageDialog(null, "הסקר נוצר ונשלח");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "שגיאה: " + ex.getMessage());
                }
            }
        });
    }

    private Question[] buildManualQuestions() {
        java.util.List<Question> list = new java.util.ArrayList<>();

        addQuestion(list, q1Text.getText(), q1Ans.getText());
        addQuestion(list, q2Text.getText(), q2Ans.getText());
        addQuestion(list, q3Text.getText(), q3Ans.getText());

        return list.toArray(new Question[0]);
    }

    private void addQuestion(java.util.List<Question> list, String text, String ansText) {
        text = text.trim();
        ansText = ansText.trim();

        if (text.isEmpty()) return;
        if (ansText.isEmpty()) return;

        String[] lines = ansText.split("\n");
        java.util.List<Answer> answers = new java.util.ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String t = lines[i].trim();
            if (!t.isEmpty()) answers.add(new Answer(t, i));
        }

        if (answers.size() >= 2 && answers.size() <= 4) {
            list.add(new Question(text, answers.toArray(new Answer[0])));
        }
    }

    private Question[] buildQuestionsFromChat() {
        try {
            String topic = topicField.getText();
            if (topic.trim().isEmpty()) return new Question[0];

            Integer balance = api.checkBalance();
            if (balance != null && balance <= 0) {
                JOptionPane.showMessageDialog(this,
                        "נגמרה מכסת ההודעות ל-ChatGPT עבור תעודת הזהות הזו.");
                return new Question[0];
            }

            api.clearHistory();

            String prompt =
                    "Create a survey with 3 questions about the topic: " + topic + ". " +
                            "For EACH question return EXACTLY in this format, and nothing else:" +
                            "\nQ: <question text>" +
                            "\nA: <answer 1>" +
                            "\nA: <answer 2>" +
                            "\nA: <answer 3>" +
                            "\nA: <answer 4>";

            String txt = api.sendMessage(prompt);

            if (txt == null || txt.trim().isEmpty()) {
                return new Question[0];
            }

            String[] lines = txt.split("\n");

            java.util.List<Question> questions = new java.util.ArrayList<>();
            java.util.List<Answer> answers = new java.util.ArrayList<>();

            String currentQ = null;
            int ansId = 0;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Q:")) {
                    if (currentQ != null && answers.size() >= 2) {
                        questions.add(new Question(currentQ, answers.toArray(new Answer[0])));
                    }
                    currentQ = line.substring(2).trim();
                    answers.clear();
                    ansId = 0;
                } else if (line.startsWith("A:")) {
                    String at = line.substring(2).trim();
                    if (!at.isEmpty()) {
                        answers.add(new Answer(at, ansId));
                        ansId++;
                    }
                }
            }

            if (currentQ != null && answers.size() >= 2) {
                questions.add(new Question(currentQ, answers.toArray(new Answer[0])));
            }

            return questions.toArray(new Question[0]);

        } catch (Exception e) {
            return new Question[0];
        }
    }

    public void showResults(String text) {
        resultsArea.setText(text);
    }

    private static class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(new Color(255, 170, 200));
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
