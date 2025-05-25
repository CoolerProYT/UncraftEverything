package com.coolerpromc.uncrafteverything.util;

import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BufferUtil {
    public static void writeStringList(PacketByteBuf buffer, List<String> list) {
        buffer.writeVarInt(list.size());
        for (String str : list) {
            buffer.writeString(str);
        }
    }

    public static List<String> readStringList(PacketByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buffer.readString());
        }
        return list;
    }

    public static void writeMap(PacketByteBuf buffer, Map<String, Integer> map){
        buffer.writeVarInt(map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public static Map<String, Integer> readMap(PacketByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<String, Integer> map = new java.util.HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = buffer.readString();
            int value = buffer.readVarInt();
            map.put(key, value);
        }
        return map;
    }
}