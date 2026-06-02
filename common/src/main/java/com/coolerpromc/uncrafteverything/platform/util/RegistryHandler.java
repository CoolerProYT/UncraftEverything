package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public interface RegistryHandler<R, T> extends Supplier<T> {
    Identifier id();
    Holder<T> holder();
    default ResourceKey<R> key(){
        return (ResourceKey<R>) holder().unwrapKey().orElse(null);
    }
}