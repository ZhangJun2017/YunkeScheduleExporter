import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.FormBody;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("8_Orange_Yunke Class Schedule Exporter Running!");
        System.out.println("===============================================");

        final String yunkeLoginUrl = "https://cloud.mizholdings.com/mizhu/api/mobile/login";
        final String yunkeGetUserClassesUrl = "https://cloud.mizholdings.com/mizhu/api/lessonInfo/myLesson?token=%s&lessonTerm=1";
        final String yunkeGetClassInfoUrl = "https://cloud.mizholdings.com/mizhu/api/lessonInfo/lessonInfo?token=%s";

        String loginName, password;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your login name:");
        loginName = scanner.nextLine();
        System.out.print("Enter your password:");
        password = scanner.nextLine();
        System.out.println();
        JsonObject loginResponse = Tools.parseJson(Tools.post(yunkeLoginUrl, new FormBody.Builder().add("account", loginName).add("password", password).build()));
        if (!loginResponse.get("code").toString().equals("0")) {
            System.err.println("Login failed.");
            System.exit(-2);
        }
        loginResponse = loginResponse.getAsJsonObject("data");
        String userName = loginResponse.get("nickname").getAsString() + "(" + loginResponse.get("name").getAsString() + ")";
        String token = loginResponse.get("token").getAsString();
        System.out.println("Hi " + userName + ",pull lessons for you...");
        System.out.println();
        ArrayList<String> classIdCollection = new ArrayList<>();
        int page = 1;
        while (true) {
            JsonArray getUserClassesResponse = Tools.parseJson(Tools.post(String.format(yunkeGetUserClassesUrl, token), new FormBody.Builder().add("page", Integer.toString(page)).build())).getAsJsonArray("data");
            if (getUserClassesResponse.size() == 0) {
                break;
            }
            System.out.println();
            System.out.println("====== Page " + page + " ======");
            for (int i = 0; i < getUserClassesResponse.size(); i++) {
                JsonObject nowOperating = getUserClassesResponse.get(i).getAsJsonObject();
                System.out.println(String.format("[%d-%s]%s - %s", nowOperating.get("lessonTypeId").getAsInt(), nowOperating.get("lessonTypeName").getAsString(), nowOperating.get("lessonName").getAsString(), nowOperating.get("lessonId").getAsString()));
                classIdCollection.add(nowOperating.get("lessonId").getAsString());
            }
            System.out.println("====== Page " + page + " ======");
            page++;
        }
    }
}
