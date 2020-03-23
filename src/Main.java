import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.FormBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
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
        System.out.println();
        System.out.println("Requesting class info in your account...");
        ArrayList<HashMap<String, String>> classroomInfoCollection = new ArrayList<>();
        for (int i = 0; i < classIdCollection.size(); i++) {
            Thread.sleep(233);
            //force sleep to prevent us from being banned by the server
            System.out.println((i + 1) + "/" + classIdCollection.size());
            JsonObject nowOperating = Tools.parseJson(Tools.post(String.format(yunkeGetClassInfoUrl, token), new FormBody.Builder().add("lessonId", classIdCollection.get(i)).build())).getAsJsonObject("data");
            JsonArray classroomInfoList = nowOperating.getAsJsonArray("classroomInfoList");
            JsonArray videoList = nowOperating.getAsJsonArray("videoList");
            for (int j = 0; j < classroomInfoList.size(); j++) {
                //System.out.println(" |" + (j + 1) + "/" + classroomInfoList.size());
                JsonObject nowOperatingClassroom = classroomInfoList.get(j).getAsJsonObject();
                HashMap<String, String> classroomInfo = new HashMap<>();
                classroomInfo.put("classroomName", nowOperatingClassroom.get("classroomName").getAsString());
                classroomInfo.put("classroomId", nowOperatingClassroom.get("classroomId").getAsString());
                classroomInfo.put("className", nowOperating.get("lessonName").getAsString());
                classroomInfo.put("classId", nowOperating.get("lessonId").getAsString());
                classroomInfo.put("classTypeName", nowOperating.get("lessonTypeName").getAsString());
                classroomInfo.put("classTypeId", nowOperating.get("lessonTypeId").getAsString());
                classroomInfo.put("classroomStartTime", nowOperatingClassroom.get("factStartTime").getAsString());
                classroomInfo.put("classroomEndTime", nowOperatingClassroom.get("factEndTime").getAsString());
                if (videoList.size() == 0) {
                    classroomInfo.put("videoUrl", "");
                } else {
                    for (int k = 0; k < videoList.size(); k++) {
                        if (videoList.get(k).getAsJsonObject().get("classroomId").getAsString().equals(nowOperatingClassroom.get("classroomId").getAsString())) {
                            classroomInfo.put("videoUrl", videoList.get(k).getAsJsonObject().get("videoPath").getAsString());
                        } else {
                            classroomInfo.put("videoUrl", "");
                        }
                    }
                }
                classroomInfoCollection.add(classroomInfo);
            }
        }
        System.out.println("Done,outputting...");
        for (int i = 0; i < classroomInfoCollection.size(); i++) {
            HashMap nowOperating = classroomInfoCollection.get(i);
            String outputFormat = "%s,%s,%s,%s,%s";
            //String outputFormat = "开始时间,学科,课程名,课时名,视频直链";
            System.out.println(String.format(outputFormat, nowOperating.get("classroomStartTime"), nowOperating.get("classTypeName"), nowOperating.get("className"), nowOperating.get("classroomName"), nowOperating.get("videoUrl")));
        }
        System.out.println("Done,exiting...");
    }
}