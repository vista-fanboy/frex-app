/*
 * Frex - a fractal image generator for Android mobile devices
 *
 * Copyright (C) 2013 by Norman Fomferra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nf.frex.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Norman Fomferra
 */
public class Registry<T> {

    private final List<String> idList;
    private final Map<String, T> idValueMap;

    public static <T> Registry<T> create(Class<? extends T> type) {
        return create(type, type);
    }

    public static <T> Registry<T> create(Class<?> fieldProvider, Class<? extends T> fieldType) {
        ArrayList<String> idList = new ArrayList<String>();
        HashMap<String, T> idValueMap = new HashMap<String, T>();
        Field[] fields = fieldProvider.getFields();
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && fieldType.isAssignableFrom(field.getType())
                    && !field.getName().startsWith("_")) {
                try {
                    String id = field.getName();
                    T value = (T) field.get(null);
                    idList.add(id);
                    idValueMap.put(id, value);
                } catch (IllegalAccessException e) {
                    // ?!?
                }
            }
        }
        // Collections.sort(idList);
        return new Registry<T>(idList, idValueMap);
    }

    private Registry(List<String> idList, Map<String, T> idValueMap) {
        this.idList = idList;
        this.idValueMap = idValueMap;
    }

    public int getSize() {
        return idList.size();
    }

    public String[] getIds() {
        return idList.toArray(new String[idList.size()]);
    }

    public List<String> getIdList() {
        return Collections.unmodifiableList(idList);
    }

    public String getId(int index) {
        return idList.get(index);
    }

    public String getId(T value) {
        for (Map.Entry<String, T> entry : idValueMap.entrySet()) {
            if (value == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }


    public int getIndex(String id) {
        return idList.indexOf(id);
    }

    public Map<String, T> getIdValueMap() {
        return Collections.unmodifiableMap(idValueMap);
    }

    public T getValue(String id) {
        return idValueMap.get(id);
    }

    public T getValue(int index) {
        String id = idList.get(index);
        return idValueMap.get(id);
    }

    public T getValue(String id, T defaultValue) {
        T value = idValueMap.get(id);
        return value != null ? value : defaultValue;
    }
}
