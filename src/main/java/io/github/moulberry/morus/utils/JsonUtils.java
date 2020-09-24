package io.github.moulberry.morus.utils;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class JsonUtils {

    public static Splitter PATH_SPLITTER = Splitter.on(".").omitEmptyStrings().limit(2);
    public static JsonElement getElement(JsonElement parent, String path) {
        List<String> path_split = PATH_SPLITTER.splitToList(path);
        if(parent instanceof JsonObject) {
            JsonElement e = parent.getAsJsonObject().get(path_split.get(0));
            if(path_split.size() > 1) {
                return getElement(e, path_split.get(1));
            } else {
                return e;
            }
        } else {
            return parent;
        }
    }

    public static boolean putElement(JsonObject parent, String path, JsonElement element) {
        List<String> path_split = PATH_SPLITTER.splitToList(path);
        if(parent != null) {
            if(path_split.size() > 1) {
                JsonElement e = parent.getAsJsonObject().get(path_split.get(0));
                if(e != null) {
                    if(e.isJsonObject()) {
                        return putElement(e.getAsJsonObject(), path_split.get(1), element);
                    } else {
                        return false;
                    }
                } else {
                    e = new JsonObject();
                    if(putElement(e.getAsJsonObject(), path_split.get(1), element)) {
                        parent.add(path_split.get(0), e);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                parent.add(path_split.get(0), element);
                return true;
            }
        }
        return false;
    }

}
