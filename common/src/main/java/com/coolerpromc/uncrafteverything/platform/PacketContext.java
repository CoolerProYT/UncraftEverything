package com.coolerpromc.uncrafteverything.platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PacketContext {
  private final ServerPlayer sender;
  private final Executor executor;
  private final Disconnectable connection;

  public PacketContext(ServerPlayer sender, Executor executor, Consumer<Component> disconnect) {
    this.sender = sender;
    this.executor = executor;
    this.connection = new Disconnectable(disconnect);
  }

  public ServerPlayer getSender() {
    return sender;
  }

  public Disconnectable getNetworkManager() {
    return connection;
  }

  public CompletableFuture<Void> enqueueWork(Runnable action) {
    return CompletableFuture.runAsync(action, executor);
  }

  public record Disconnectable(Consumer<Component> action) {
    public void disconnect(Component reason) {
      action.accept(reason);
    }
  }
}
