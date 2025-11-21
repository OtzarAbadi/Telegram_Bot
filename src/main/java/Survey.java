import java.time.LocalDateTime;
import java.util.HashMap;

public class Survey {

    private Question[] questions;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private boolean open;
    private int totalParticipants;

    private HashMap<Long, Integer>[] answersByUsers;

    public Survey(Question[] questions) {
        this.questions = questions;
        this.open = false;

        answersByUsers = new HashMap[questions.length];
        for (int i = 0; i < questions.length; i++) {
            answersByUsers[i] = new HashMap<>();
        }
    }

    public void openSurvey(int participants) {
        this.open = true;
        this.totalParticipants = participants;
        this.openTime = LocalDateTime.now();
    }

    public void closeSurvey() {
        this.open = false;
        this.closeTime = LocalDateTime.now();
    }

    public boolean isOpen() {
        return open;
    }

    public Question[] getQuestions() {
        return questions;
    }

    public void addAnswer(int questionIndex, long userId, int answerId) {
        answersByUsers[questionIndex].put(userId, answerId);
    }

    public boolean hasUserAnswered(long userId, int questionIndex) {
        return answersByUsers[questionIndex].containsKey(userId);
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public HashMap<Long, Integer>[] getAllAnswers() {
        return answersByUsers;
    }
}
