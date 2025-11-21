package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private Set<Long> community = new HashSet<>();
    private Map<Long, String> names = new HashMap<>();

    private Survey currentSurvey;
    private boolean surveyActive = false;
    private long surveyEndTime = 0;
    private long surveyCreatorId = 0;

    private SurveyFrame frame;
    private ChatGptApi api;

    public Bot(SurveyFrame frame, ChatGptApi api) {
        this.frame = frame;
        this.api = api;
    }

    @Override
    public String getBotUsername() {
        return "OtzarNewBot";
    }

    @Override
    public String getBotToken() {
        return "8306597669:AAE8K-P5wDyiEykA-Wc-lZFc7viZr2HiGig";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg) throws TelegramApiException {
        Long chatId = msg.getChatId();
        String text = msg.getText();
        if (text == null) return;
        String t = text.trim();
        if (t.equalsIgnoreCase("/start") || t.equals("היי") || t.equalsIgnoreCase("hi")) {
            boolean newUser = !community.contains(chatId);
            if (newUser) {
                community.add(chatId);
                String name = buildName(msg);
                names.put(chatId, name);
                sendText(chatId, "הצטרפת לקהילה, גודל קהילה: " + community.size());
                String info = "חבר חדש הצטרף: " + name + "\nגודל קהילה: " + community.size();
                for (Long id : community) {
                    if (!id.equals(chatId)) {
                        sendText(id, info);
                    }
                }
            } else {
                sendText(chatId, "את/ה כבר בקהילה");
            }
        } else {
            sendText(chatId, "כדי להצטרף לקהילה שלח/י /start או היי או Hi");
        }
    }

    private String buildName(Message msg) {
        String f = msg.getFrom().getFirstName();
        String l = msg.getFrom().getLastName();
        String u = msg.getFrom().getUserName();
        String s = "";
        if (f != null) s += f;
        if (l != null) {
            if (!s.isEmpty()) s += " ";
            s += l;
        }
        if (s.trim().isEmpty() && u != null) {
            s = u;
        }
        if (s.trim().isEmpty()) {
            s = "משתמש";
        }
        return s;
    }

    private void handleCallback(CallbackQuery cb) throws TelegramApiException {
        if (!surveyActive || currentSurvey == null) {
            AnswerCallbackQuery a = new AnswerCallbackQuery();
            a.setCallbackQueryId(cb.getId());
            a.setText("אין כרגע סקר פעיל");
            execute(a);
            return;
        }
        long now = System.currentTimeMillis();
        if (now > surveyEndTime) {
            finishSurvey();
            AnswerCallbackQuery a = new AnswerCallbackQuery();
            a.setCallbackQueryId(cb.getId());
            a.setText("הסקר נסגר");
            execute(a);
            return;
        }
        String data = cb.getData();
        if (data == null) return;
        String[] parts = data.split(":");
        if (parts.length != 3) return;
        if (!"Q".equals(parts[0])) return;
        int qIndex;
        int ansIndex;
        try {
            qIndex = Integer.parseInt(parts[1]);
            ansIndex = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            return;
        }
        Long userId = cb.getFrom().getId();
        if (!community.contains(userId)) {
            AnswerCallbackQuery a = new AnswerCallbackQuery();
            a.setCallbackQueryId(cb.getId());
            a.setText("אינך בקהילה");
            execute(a);
            return;
        }
        if (qIndex < 0 || qIndex >= currentSurvey.getQuestions().length) {
            return;
        }
        Question q = currentSurvey.getQuestions()[qIndex];
        Answer[] arr = q.getAnswers();
        if (ansIndex < 0 || ansIndex >= arr.length) {
            return;
        }
        if (currentSurvey.hasUserAnswered(userId, qIndex)) {
            AnswerCallbackQuery a = new AnswerCallbackQuery();
            a.setCallbackQueryId(cb.getId());
            a.setText("כבר הצבעת על שאלה זו");
            execute(a);
            return;
        }
        currentSurvey.addAnswer(qIndex, userId, ansIndex);
        AnswerCallbackQuery a = new AnswerCallbackQuery();
        a.setCallbackQueryId(cb.getId());
        a.setText("הצבעתך נקלטה");
        execute(a);
        if (allUsersAnsweredAll()) {
            finishSurvey();
        }
    }

    private boolean allUsersAnsweredAll() {
        if (currentSurvey == null) return false;
        if (community.isEmpty()) return false;
        Question[] qs = currentSurvey.getQuestions();
        for (Long id : community) {
            for (int i = 0; i < qs.length; i++) {
                if (!currentSurvey.hasUserAnswered(id, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void finishSurvey() {
        if (!surveyActive || currentSurvey == null) return;
        surveyActive = false;
        currentSurvey.closeSurvey();
        StringBuilder sb = new StringBuilder();
        sb.append("תוצאות הסקר:\n\n");
        Question[] qs = currentSurvey.getQuestions();
        HashMap<Long, Integer>[] all = currentSurvey.getAllAnswers();
        for (int i = 0; i < qs.length; i++) {
            Question q = qs[i];
            sb.append("שאלה ").append(i + 1).append(": ").append(q.getText()).append("\n");
            Answer[] ansArr = q.getAnswers();
            int[] counts = new int[ansArr.length];
            int total = 0;
            HashMap<Long, Integer> map = all[i];
            for (Integer v : map.values()) {
                if (v != null && v >= 0 && v < ansArr.length) {
                    counts[v]++;
                    total++;
                }
            }
            List<Integer> order = new ArrayList<>();
            for (int j = 0; j < ansArr.length; j++) {
                order.add(j);
            }
            order.sort((a, b) -> Integer.compare(counts[b], counts[a]));
            for (Integer idx : order) {
                String txt = ansArr[idx].getText();
                int c = counts[idx];
                double p = 0.0;
                if (total > 0) {
                    p = (100.0 * c) / total;
                }
                sb.append("  - ").append(txt).append(": ").append(c).append(" קולות (");
                sb.append(String.format(Locale.US, "%.1f", p)).append("%)\n");
            }
            sb.append("\n");
        }
        for (Long id : community) {
            sendText(id, sb.toString());
        }

        if (frame != null) {
            frame.showResults(sb.toString());
        }

        currentSurvey = null;
        surveyCreatorId = 0;
        surveyEndTime = 0;

    }

    private void sendText(Long chatId, String text) {
        SendMessage m = new SendMessage();
        m.setChatId(String.valueOf(chatId));
        m.setText(text);
        try {
            execute(m);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup buildKeyboard(int qIndex, Answer[] answers) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < answers.length; i++) {
            Answer a = answers[i];
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(a.getText());
            b.setCallbackData("Q:" + qIndex + ":" + i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(b);
            rows.add(row);
        }
        InlineKeyboardMarkup mk = new InlineKeyboardMarkup();
        mk.setKeyboard(rows);
        return mk;
    }

    public int getCommunitySize() {
        return community.size();
    }

    public Long getAnyUser() {
        if (community.isEmpty()) return null;
        Iterator<Long> it = community.iterator();
        if (it.hasNext()) return it.next();
        return null;
    }

    public boolean hasActiveSurvey() {
        return surveyActive;
    }

    public void startSurveyFromGui(Survey survey, int delayMinutes, long creatorId) throws Exception {
        if (surveyActive) {
            throw new Exception("כבר קיים סקר פעיל");
        }
        if (community.size() < 3) {
            throw new Exception("חייבים לפחות 3 חברים בקהילה");
        }
        if (survey == null || survey.getQuestions() == null || survey.getQuestions().length == 0) {
            throw new Exception("אין שאלות בסקר");
        }
        currentSurvey = survey;
        surveyCreatorId = creatorId;
        survey.openSurvey(community.size());
        surveyActive = true;
        long now = System.currentTimeMillis();
        surveyEndTime = now + 5 * 60 * 1000;
        int delayMs = delayMinutes * 60 * 1000;
        if (delayMs <= 0) {
            sendSurveyToCommunity();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                    sendSurveyToCommunity();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
                if (surveyActive) {
                    finishSurvey();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendSurveyToCommunity() {
        if (currentSurvey == null) return;
        Question[] qs = currentSurvey.getQuestions();
        for (int i = 0; i < qs.length; i++) {
            Question q = qs[i];
            Answer[] ansArr = q.getAnswers();
            for (Long id : community) {
                SendMessage m = new SendMessage();
                m.setChatId(String.valueOf(id));
                m.setText("שאלה " + (i + 1) + ": " + q.getText());
                InlineKeyboardMarkup mk = buildKeyboard(i, ansArr);
                m.setReplyMarkup(mk);
                try {
                    execute(m);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
