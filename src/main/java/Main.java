import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {

        ChatGptApi api = new ChatGptApi();

        SurveyFrame frame = new SurveyFrame(api);
        frame.setVisible(true);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot(frame, api);
            frame.setBot(bot);
            botsApi.registerBot(bot);
        } catch (Exception e) {
            System.out.println("Error starting bot");
        }
    }
}
