package com.coolerpromc.uncrafteverything.util;

import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BufferUtil {
    public static void writeStringList(PacketBuffer buffer, List<String> list) {
        buffer.writeVarInt(list.size());
        for (String str : list) {
            buffer.writeUtf(str);
        }
    }

    public static List<String> readStringList(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buffer.readUtf());
        }
        return list;
    }

    public static void writeMap(PacketBuffer buffer, Map<String, Integer> map){
        buffer.writeVarInt(map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public static Map<String, Integer> readMap(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        Map<String, Integer> map = new java.util.HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = buffer.readUtf();
            int value = buffer.readVarInt();
            map.put(key, value);
        }
        return map;
    }
}
