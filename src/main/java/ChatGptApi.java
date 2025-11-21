import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ChatGptApi {

    private static final String BASE_URL = "https://app.seker.live/fm1/";
    private final OkHttpClient client = new OkHttpClient();

    private final String id = "213063274";

    public String sendMessage(String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "send-message?id=" + id + "&text=" + encodedText;

            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();

            System.out.println("send-message RAW: " + body);

            if (!response.isSuccessful()) {
                System.out.println("HTTP error " + response.code());
                return null;
            }

            try {
                JSONObject obj = new JSONObject(body);

                if (obj.has("code")) {
                    int code = obj.getInt("code");
                    if (code >= 3000) {
                        String msg = obj.optString("message", "");
                        System.out.println("API error " + code + " " + msg);
                        return null;
                    }
                }

                if (obj.has("extra")) {
                    String s = obj.optString("extra", "").trim();
                    if (!s.isEmpty()) return s;
                }
                if (obj.has("answer")) {
                    String s = obj.optString("answer", "").trim();
                    if (!s.isEmpty()) return s;
                }
                if (obj.has("text")) {
                    String s = obj.optString("text", "").trim();
                    if (!s.isEmpty()) return s;
                }
                if (obj.has("message")) {
                    String s = obj.optString("message", "").trim();
                    if (!s.isEmpty()) return s;
                }

                String trimmed = body.trim();
                return trimmed.isEmpty() ? null : trimmed;

            } catch (JSONException je) {
                String trimmed = body.trim();
                return trimmed.isEmpty() ? null : trimmed;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer checkBalance() {
        try {
            String url = BASE_URL + "check-balance?id=" + id;
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();

            System.out.println("check-balance RAW: " + body);

            if (!response.isSuccessful()) {
                System.out.println("HTTP error " + response.code());
                return null;
            }

            try {
                JSONObject obj = new JSONObject(body);

                if (obj.has("code")) {
                    int code = obj.getInt("code");
                    if (code >= 3000) {
                        String msg = obj.optString("message", "");
                        System.out.println("API error " + code + " " + msg);
                        return null;
                    }
                }

                String extra = obj.optString("extra", "").trim();
                if (extra.isEmpty()) {
                    extra = obj.optString("balance", "").trim();
                }
                if (extra.isEmpty()) {
                    return null;
                }

                try {
                    return Integer.parseInt(extra);
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse balance from: " + extra);
                    return null;
                }

            } catch (JSONException je) {
                System.out.println("check-balance not JSON: " + body);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearHistory() {
        try {
            String url = BASE_URL + "clear-history?id=" + id;
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            String body = response.body().string();

            System.out.println("clear-history RAW: " + body);

            try {
                JSONObject obj = new JSONObject(body);

                if (obj.has("code")) {
                    int code = obj.getInt("code");
                    if (code >= 3000) {
                        String msg = obj.optString("message", "");
                        System.out.println("API error " + code + " " + msg);
                    }
                }
            } catch (JSONException je) {
                System.out.println("clear-history not JSON: " + body);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
