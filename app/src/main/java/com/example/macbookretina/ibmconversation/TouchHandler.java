package com.example.macbookretina.ibmconversation;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MacbookRetina on 12/11/16.
 */
public class TouchHandler {
    Map<String,String> map = new HashMap();
    WatsonService service;
    String id = "00b866d89308247b213eb6ea90766acb";

    public TouchHandler() {
//        map.put("Coffee", "Can I have a Coffee");
//        map.put("Espresso", "Can I have an Espresso");
//        map.put("Latte", "Can I have a Latte");
//        map.put("Tea", "Can I have a Tea");
//        map.put("Frappe", "Can I have a Frappe");
//        map.put("Ice Tea", "Can I have an Ice Tea");
//        map.put("Hamburger", "Can I have a hamburger");
//        map.put("Fries", "Can I have Fries");
//        map.put("Ice Cream", "Can I have an Ice Cream");
//        map.put("Soft Drink", "Can I have a Soft Drink");
//        map.put("Chicken Nuggets", "Can I have Chicken Nuggets");
//        map.put("Cookies", "Can I have Cookies");
        service = new WatsonService();
    }

    public String handleCommand(String category, String entity, String attribute) {
        JSONObject response = null;
        String answer = "";
        if (category.equals("Midtown Brew")) {
            String input = entity + " " + attribute;
            try {
                response = service.conversation(id, input, "1", "touch");
                answer = response.getJSONArray("actions").getJSONObject(0).get("action_type").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return answer;
    }

}
