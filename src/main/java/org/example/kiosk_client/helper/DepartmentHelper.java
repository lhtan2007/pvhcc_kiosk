package org.example.kiosk_client.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.shared.model.Department;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DepartmentHelper {
    private static final Gson gson = new Gson();
    public static List<Department> fetchDepartmentList() {
        List<Department> departments = new ArrayList<Department>();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("clientType", "kiosk");
        requestJson.addProperty("action", "GET_DEPARTMENTS");
        requestJson.addProperty("data", "");

        JsonObject response = NetworkInitializer.getInstance().sendRequest(requestJson);
        System.out.println("Raw JSON từ Server: " + response.toString());

        if(response != null && "ok".equals(response.get("status").getAsString())) {
            if(response.has("data")) {
                JsonArray jsonArray = response.getAsJsonArray("data");
                Type listType = new TypeToken<ArrayList<Department>>() {}.getType();
                departments = gson.fromJson(jsonArray, listType);
            }
            else {
                System.err.println("Lỗi: Không thể tải danh sách phòng ban từ Server.");
            }
        }

        return departments;
    };
}
