package com.phaxio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.phaxio.exception.PhaxioException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.faxapp.util.Log;

public class AreaCodes {
    private static final String TAG = AreaCodes.class.getSimpleName();
    String code;
    String city;
    String state;

    public static List<AreaCodes> list() throws PhaxioException {
        Map<String, Object> options = new HashMap<>();

        JsonObject result = Phaxio.doRequest("areaCodes ", options, "POST");
        JsonArray phoneNumberArray = result.get("data").getAsJsonArray();
        List<AreaCodes> list = new ArrayList<>();
        for (JsonElement element : phoneNumberArray) {
            Log.i(TAG, element.getAsString());
            AreaCodes number = new AreaCodes();
            number.mapJsonToSelf(element.getAsJsonObject());
            list.add(number);
        }

        return list;
    }

    void mapJsonToSelf(JsonObject object) throws PhaxioException {
        this.city = object.get("city").getAsString();
        this.state = object.get("state").getAsString();
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCode() {
        return code;
    }


}
