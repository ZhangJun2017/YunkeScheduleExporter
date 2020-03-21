import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

class Tools {
    public static String post(String url, RequestBody form) {
        Request request = new Request.Builder()
                .url(url)
                .post(form)
                .build();
        try {
            return new OkHttpClient().newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown error";
    }

    public static JsonObject parseJson(String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }
}
