package com.coolerpromc.uncrafteverything.platform.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface RegistryHandler<T> extends Supplier<T> {
    Identifier id();
    Holder<T> holder();
}