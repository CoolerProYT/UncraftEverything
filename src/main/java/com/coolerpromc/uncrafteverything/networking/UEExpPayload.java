package com.coolerpromc.uncrafteverything.networking;

import com.coolerpromc.uncrafteverything.UncraftEverything;
import com.coolerpromc.uncrafteverything.util.BufferUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public class UEExpPayload {
    public static final Identifier TYPE = new Identifier(UncraftEverything.MODID, "ue_exp_payload");

    private final Map<String, Integer> perItemExp;

    public UEExpPayload(Map<String, Integer> perItemExp){
        this.perItemExp = perItemExp;
    }

    public static PacketByteBuf encode(PacketByteBuf buf, UEExpPayload payload) {
        BufferUtil.writeMap(buf, payload.perItemExp);
        return buf;
    }

    public static UEExpPayload decode(PacketByteBuf buf) {
        return new UEExpPayload(BufferUtil.readMap(buf));
    }

    public Map<String, Integer> perItemExp() {
        return perItemExp;
    }
}