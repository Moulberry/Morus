package io.github.moulberry.morus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.morus.utils.JsonUtils;

public class MorusSubstitutor {

    private static final JsonObject substiutionObject = new JsonObject();

    public static String processString(String text) {
        StringBuilder wholeString = new StringBuilder();
        StringBuilder substituteString = new StringBuilder();
        int depth = 0;

        for(int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if(c == '{') {
                depth++;
            } else if(c == '}') {
                depth--;
            }
            if(depth < 0) depth = 0;

            if(depth == 1 && c == '{') continue;

            if(depth == 0) {
                String sub = substituteString.toString();

                if(sub.length() > 0) {
                    wholeString.append(getSubstitution(sub));
                    substituteString = new StringBuilder();
                } else {
                    wholeString.append(c);
                }
            } else {
                substituteString.append(c);
            }
        }

        return wholeString.toString();
    }

    private static String getSubstitution(String text) {
        String substituted = processString(text);

        JsonElement element = JsonUtils.getElement(substiutionObject, substituted);
        if(element == null || !element.isJsonPrimitive()) {
            return "{"+text+"}";
        } else {
            return element.getAsJsonPrimitive().getAsString();
        }
    }

    public static boolean putSubstiution(String modid, String path, String value) {
        return JsonUtils.putElement(substiutionObject, modid+"."+path, new JsonPrimitive(value));
    }

}
