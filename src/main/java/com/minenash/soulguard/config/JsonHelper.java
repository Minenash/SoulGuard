package com.minenash.soulguard.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHelper {

    public static Double getDouble(JsonObject json, String member, SoulPropertyResult<?> result) {
        if (!json.has(member))
            return null;
        JsonPrimitive jCount = json.getAsJsonPrimitive(member);
        if (!jCount.isNumber()) {
            result.addDebugMessage(member + " is not a number, using 0");
            return 0.0;
        }
        return jCount.getAsDouble();
    }

    public static Float getFloat(JsonObject json, String member, SoulPropertyResult<?> result) {
        if (!json.has(member))
            return null;
        JsonPrimitive jCount = json.getAsJsonPrimitive(member);
        if (!jCount.isNumber()) {
            result.addDebugMessage(member + " is not a number, using 0");
            return 0F;
        }
        return jCount.getAsFloat();
    }

    public static Integer getInt(JsonObject json, String member, SoulPropertyResult<?> result) {
        if (!json.has(member))
            return null;
        JsonPrimitive jCount = json.getAsJsonPrimitive(member);
        if (!jCount.isNumber()) {
            result.addDebugMessage(member + " is not a number, using 0");
            return 0;
        }
        return jCount.getAsInt();
    }


    public static Double getSpecialDouble(JsonObject json, String member, String oldMember, Double oldValue, SoulPropertyResult<?> result) {
        if (!json.has(member))
            return oldValue;
        if (oldValue == null)
            return getDouble(json, "brightness", result);

        result.addDebugMessage("The " + member + " property and the " + oldMember + " property are incompatible for this type, using " + oldMember + "'s value");
        return oldValue;
    }

    public static Double[] getVec3Array(JsonObject json,  String member, SoulPropertyResult<?> result) {
        if (!json.has(member))
            return new Double[]{null, null, null};
        if (!json.get(member).isJsonArray()) {
            result.addDebugMessage("The " + member + " property isn't a list, using 0,0,0");
            return new Double[]{0.0, 0.0, 0.0};
        }
        JsonArray value = json.getAsJsonArray(member);
        if (value.size() > 3)
            result.addDebugMessage("The " + member + " property has more than 3 items, there should be one for X, Y, and Z. The first 3 will be used");
        if (value.size() < 3)
            result.addDebugMessage("The " + member + " property has less than 3 items, there should be one for X, Y, and Z. The missing items will be substituted with 0");

        Double[] vec3 = new Double[]{0.0, 0.0, 0.0};
        for (int i = 0; i < 3; i++) {
            if (value.size() > i+1) {
                JsonPrimitive e = value.get(i).getAsJsonPrimitive();
                if (e.isNumber())
                    vec3[i] = e.getAsDouble();
                else
                    result.addDebugMessage("Element " + i + " of " + member + "is not a number, using 0");
            }
        }

        return vec3;
    }

    public static final Pattern COLOR_PATTERN = Pattern.compile("(0x|#)?([A-Fa-f\\d]{6}|[A-Fa-f\\d]{3})");
    public static Color getColor(JsonObject json, SoulPropertyResult<SoulParticle> result) {
        JsonPrimitive jColor = json.getAsJsonPrimitive("color");
        if (!jColor.isString())
            result.addDebugMessage("Color Property not a Hexadecimal String, using White");
        else {
            Matcher matcher = COLOR_PATTERN.matcher(jColor.getAsString());
            if (!matcher.matches())
                result.addDebugMessage("Color Property not a Hexadecimal String, using White");
            else {
                String hexColor = matcher.group(2);
                if (hexColor.length() == 3)
                    hexColor = "" + hexColor.charAt(0) + hexColor.charAt(0) + hexColor.charAt(1) + hexColor.charAt(1) + hexColor.charAt(2) + hexColor.charAt(2);
                return new Color(Integer.parseInt(hexColor, 16));
            }
        }
        return null;
    }

    public static JsonArray doubleJsonArray(double... elements) {
        JsonArray array = new JsonArray();
        for (double e : elements)
            array.add(e);
        return array;
    }

}
