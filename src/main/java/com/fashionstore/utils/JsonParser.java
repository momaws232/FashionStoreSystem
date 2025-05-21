package com.fashionstore.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple JSON parser class to help with reading JSON data
 */
public class JsonParser {
    // Inner class to represent a JSON object
    public static class JsonObject {
        private final Map<String, Object> values = new HashMap<>();

        public boolean has(String key) {
            return values.containsKey(key);
        }

        public String getString(String key) {
            Object value = values.get(key);
            return value != null ? value.toString() : "";
        }

        public int getInt(String key) {
            Object value = values.get(key);
            if (value == null)
                return 0;
            try {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public boolean getBoolean(String key) {
            Object value = values.get(key);
            if (value == null)
                return false;
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return "true".equalsIgnoreCase(value.toString());
        }

        public boolean isNull(String key) {
            return values.get(key) == null;
        }

        public JsonArray getJsonArray(String key) {
            Object value = values.get(key);
            if (value instanceof JsonArray) {
                return (JsonArray) value;
            }
            return new JsonArray();
        }

        public void put(String key, Object value) {
            values.put(key, value);
        }
    }

    // Inner class to represent a JSON array
    public static class JsonArray {
        private final List<Object> values = new ArrayList<>();

        public int length() {
            return values.size();
        }

        public JsonObject getJsonObject(int index) {
            if (index < 0 || index >= values.size()) {
                return new JsonObject();
            }
            Object value = values.get(index);
            if (value instanceof JsonObject) {
                return (JsonObject) value;
            }
            return new JsonObject();
        }

        public String getString(int index) {
            if (index < 0 || index >= values.size()) {
                return "";
            }
            Object value = values.get(index);
            return value != null ? value.toString() : "";
        }

        public void add(Object value) {
            values.add(value);
        }
    }

    /**
     * Parses a JSON string into a JsonObject
     * 
     * @param json The JSON string to parse
     * @return A JsonObject representing the parsed JSON
     * @throws Exception If there is an error parsing the JSON
     */
    public static JsonObject parseObject(String json) throws Exception {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new Exception("Invalid JSON object format");
        }

        JsonObject result = new JsonObject();
        // Remove leading '{' and trailing '}'
        json = json.substring(1, json.length() - 1).trim();

        // Parse key-value pairs
        int pos = 0;
        while (pos < json.length()) {
            // Find key
            if (json.charAt(pos) != '"') {
                pos++;
                continue;
            }

            int keyStart = pos + 1;
            int keyEnd = findClosingQuote(json, keyStart);
            if (keyEnd == -1)
                break;

            String key = json.substring(keyStart, keyEnd);
            pos = keyEnd + 1;

            // Find the colon after the key
            while (pos < json.length() && json.charAt(pos) != ':') {
                pos++;
            }
            pos++; // Skip the colon

            // Find value
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }

            if (pos >= json.length())
                break;

            // Parse value based on the first character
            char firstChar = json.charAt(pos);
            Object value;

            if (firstChar == '{') {
                // If value is an object, recursively parse it
                int endPos = findMatchingBrace(json, pos, '{', '}');
                if (endPos == -1)
                    break;
                String subJson = json.substring(pos, endPos + 1);
                value = parseObject(subJson);
                pos = endPos + 1;
            } else if (firstChar == '[') {
                // If value is an array, parse array
                int endPos = findMatchingBrace(json, pos, '[', ']');
                if (endPos == -1)
                    break;
                String subJson = json.substring(pos, endPos + 1);
                value = parseArray(subJson);
                pos = endPos + 1;
            } else if (firstChar == '"') {
                // If value is a string
                int valueStart = pos + 1;
                int valueEnd = findClosingQuote(json, valueStart);
                if (valueEnd == -1)
                    break;
                value = json.substring(valueStart, valueEnd);
                pos = valueEnd + 1;
            } else if (Character.isDigit(firstChar) || firstChar == '-') {
                // If value is a number
                int valueEnd = pos;
                while (valueEnd < json.length() &&
                        (Character.isDigit(json.charAt(valueEnd)) ||
                                json.charAt(valueEnd) == '.' ||
                                json.charAt(valueEnd) == '-' ||
                                json.charAt(valueEnd) == 'e' ||
                                json.charAt(valueEnd) == 'E' ||
                                json.charAt(valueEnd) == '+')) {
                    valueEnd++;
                }
                String numStr = json.substring(pos, valueEnd);
                try {
                    if (numStr.contains(".")) {
                        value = Double.parseDouble(numStr);
                    } else {
                        value = Integer.parseInt(numStr);
                    }
                } catch (NumberFormatException e) {
                    value = 0;
                }
                pos = valueEnd;
            } else if (json.substring(pos).startsWith("true")) {
                value = Boolean.TRUE;
                pos += 4;
            } else if (json.substring(pos).startsWith("false")) {
                value = Boolean.FALSE;
                pos += 5;
            } else if (json.substring(pos).startsWith("null")) {
                value = null;
                pos += 4;
            } else {
                // Skip unknown characters
                pos++;
                continue;
            }

            // Store key-value pair
            result.put(key, value);

            // Find next key-value pair
            while (pos < json.length() && (json.charAt(pos) == ',' || Character.isWhitespace(json.charAt(pos)))) {
                pos++;
            }
        }

        return result;
    }

    /**
     * Parses a JSON array string into a JsonArray
     * 
     * @param json The JSON array string to parse
     * @return A JsonArray representing the parsed JSON array
     * @throws Exception If there is an error parsing the JSON array
     */
    public static JsonArray parseArray(String json) throws Exception {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new Exception("Invalid JSON array format");
        }

        JsonArray result = new JsonArray();
        // Remove leading '[' and trailing ']'
        json = json.substring(1, json.length() - 1).trim();

        // Parse array elements
        int pos = 0;
        while (pos < json.length()) {
            // Skip whitespace
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }

            if (pos >= json.length())
                break;

            // Parse value based on the first character
            char firstChar = json.charAt(pos);
            Object value;

            if (firstChar == '{') {
                // If value is an object, recursively parse it
                int endPos = findMatchingBrace(json, pos, '{', '}');
                if (endPos == -1)
                    break;
                String subJson = json.substring(pos, endPos + 1);
                value = parseObject(subJson);
                pos = endPos + 1;
            } else if (firstChar == '[') {
                // If value is an array, recursively parse it
                int endPos = findMatchingBrace(json, pos, '[', ']');
                if (endPos == -1)
                    break;
                String subJson = json.substring(pos, endPos + 1);
                value = parseArray(subJson);
                pos = endPos + 1;
            } else if (firstChar == '"') {
                // If value is a string
                int valueStart = pos + 1;
                int valueEnd = findClosingQuote(json, valueStart);
                if (valueEnd == -1)
                    break;
                value = json.substring(valueStart, valueEnd);
                pos = valueEnd + 1;
            } else if (Character.isDigit(firstChar) || firstChar == '-') {
                // If value is a number
                int valueEnd = pos;
                while (valueEnd < json.length() &&
                        (Character.isDigit(json.charAt(valueEnd)) ||
                                json.charAt(valueEnd) == '.' ||
                                json.charAt(valueEnd) == '-' ||
                                json.charAt(valueEnd) == 'e' ||
                                json.charAt(valueEnd) == 'E' ||
                                json.charAt(valueEnd) == '+')) {
                    valueEnd++;
                }
                String numStr = json.substring(pos, valueEnd);
                try {
                    if (numStr.contains(".")) {
                        value = Double.parseDouble(numStr);
                    } else {
                        value = Integer.parseInt(numStr);
                    }
                } catch (NumberFormatException e) {
                    value = 0;
                }
                pos = valueEnd;
            } else if (json.substring(pos).startsWith("true")) {
                value = Boolean.TRUE;
                pos += 4;
            } else if (json.substring(pos).startsWith("false")) {
                value = Boolean.FALSE;
                pos += 5;
            } else if (json.substring(pos).startsWith("null")) {
                value = null;
                pos += 4;
            } else {
                // Skip unknown characters
                pos++;
                continue;
            }

            // Add value to array
            result.add(value);

            // Find next element
            while (pos < json.length() && (json.charAt(pos) == ',' || Character.isWhitespace(json.charAt(pos)))) {
                pos++;
            }
        }

        return result;
    }

    /**
     * Finds the closing quote for a string
     * 
     * @param json  The JSON string
     * @param start The position after the opening quote
     * @return The position of the closing quote, or -1 if not found
     */
    private static int findClosingQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '\\') {
                // Skip escaped characters
                i++;
            } else if (json.charAt(i) == '"') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the matching closing brace/bracket
     * 
     * @param json  The JSON string
     * @param start The position of the opening brace/bracket
     * @param open  The opening brace/bracket character
     * @param close The closing brace/bracket character
     * @return The position of the closing brace/bracket, or -1 if not found
     */
    private static int findMatchingBrace(String json, int start, char open, char close) {
        int count = 1;
        for (int i = start + 1; i < json.length(); i++) {
            if (json.charAt(i) == '\\') {
                // Skip escaped characters
                i++;
            } else if (json.charAt(i) == '"') {
                // Skip string literals
                i = findClosingQuote(json, i + 1);
                if (i == -1)
                    return -1;
            } else if (json.charAt(i) == open) {
                count++;
            } else if (json.charAt(i) == close) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}