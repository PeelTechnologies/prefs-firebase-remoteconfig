/*
 * Copyright (C) 2019 Peel Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.peel.prefs.firebase;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peel.prefs.Prefs;
import com.peel.prefs.SharedPrefs;
import com.peel.prefs.TypedKey;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PrefsFirebaseRemoteConfigSyncer implements Prefs.EventListener {

    private final Gson gson;
    private final FirebaseRemoteConfig rc;
    private final List<TypedKey<?>> keys;

    public PrefsFirebaseRemoteConfigSyncer(Gson gson, boolean debug, TypedKey<?>... keys) {
        this.gson = gson;
        this.keys = keys == null ? new ArrayList<>() : Arrays.asList(keys);
        rc = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(debug)
                .build();
        rc.setConfigSettings(configSettings);
        refresh();
    }

    public void refresh() {
        rc.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (TypedKey<?> key : keys) {
                    sync(key);
                }
            }
        });
    }

    private <T> void sync(TypedKey<T> key) {
        T instance;
        Type type = key.getTypeOfValue();
        String keyName = key.getName();
        Set<String> keys = rc.getKeysByPrefix(keyName);
        boolean found = keys.contains(keyName);
        if (!found) return;
        if (type == Boolean.class || type == boolean.class) {
            boolean value = rc.getBoolean(keyName);
            instance = (T) Boolean.valueOf(value);
        } else if (type == String.class) {
            String str = rc.getString(keyName);
            str = stripJsonQuotesIfPresent(str);
            instance = (T) str;
        } else if (type == Integer.class || type == int.class) {
            long value = rc.getLong(keyName);
            instance = (T) Integer.valueOf((int) value);
        } else if (type == Long.class || type == long.class) {
            long value = rc.getLong(keyName);
            instance = (T) Long.valueOf(value);
        } else if (type == Float.class || type == float.class) {
            double value = rc.getDouble(keyName);
            instance = (T) Float.valueOf((float) value);
        } else if (type == Double.class || type == double.class) {
            double value = rc.getDouble(keyName);
            instance = (T) Double.valueOf(value);
        } else if (type == Short.class || type == short.class) {
            long value = rc.getLong(keyName);
            instance = (T) Short.valueOf((short) value);
        } else if (type == Byte.class || type == byte.class) {
            long value = rc.getLong(keyName);
            instance = (T) Byte.valueOf((byte) value);
        } else {
            String json = rc.getString(keyName);
            instance = gson.fromJson(json, type);
        }
        if (instance != null) {
            T existing = SharedPrefs.get(key, null);
            if (!equals(existing, instance)) {
                SharedPrefs.put(key, instance);
            }
        }
    }

    private <T> boolean equals(T first, T second) {
        if (first == null) return second == null;
        return first.equals(second);
    }

    @Override
    public <T> void onPut(TypedKey<T> key, T value) {
    }

    @Override
    public <T> void onRemove(TypedKey<T> key) {
    }

    private static String stripJsonQuotesIfPresent(String str) {
        if (str == null) return str;
        int lastCharIndex = str.length() - 1;
        if (lastCharIndex < 1) return str; // one char string
        if (str.charAt(0) == '"' && str.charAt(lastCharIndex) == '"') {
            str = str.substring(1, lastCharIndex);
        }
        return str;
    }
}
