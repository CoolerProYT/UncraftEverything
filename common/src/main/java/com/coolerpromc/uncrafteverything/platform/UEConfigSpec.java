package com.coolerpromc.uncrafteverything.platform;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

/** The existing TOML schema with shared loading and change notifications. */
public final class UEConfigSpec {
  private final List<ConfigValue<?>> values;
  private CommentedFileConfig file;
  private Path path;
  private Runnable changed = () -> {};

  private UEConfigSpec(List<ConfigValue<?>> values) {
    this.values = values;
  }

  public synchronized void load(Path path, Runnable changed) {
    this.path = path;
    this.changed = changed;
    file = CommentedFileConfig.builder(path).sync().preserveInsertionOrder().build();
    file.load();
    readValues();
    save();
    try {
      FileWatcher.defaultInstance()
          .addWatch(
              path,
              () -> {
                synchronized (this) {
                  file.load();
                  readValues();
                }
              });
    } catch (Exception e) {
      throw new IllegalStateException("Unable to watch " + path, e);
    }
  }

  private void readValues() {
    for (ConfigValue<?> value : values) value.read(file.getOrElse(value.path, value.defaultValue));
    changed.run();
  }

  public synchronized void save() {
    if (file == null) return;
    for (ConfigValue<?> value : values) {
      Object raw = value.value;
      file.set(value.path, raw instanceof Enum<?> e ? e.name() : raw);
      if (!value.comment.isEmpty()) file.setComment(value.path, value.comment);
    }
    file.save();
    changed.run();
  }

  public synchronized void close() {
    if (file == null) return;
    try {
      FileWatcher.defaultInstance().removeWatch(path);
    } catch (RuntimeException ignored) {
    }
    file.close();
    file = null;
  }

  public static class ConfigValue<T> {
    final String path, comment;
    final T defaultValue;
    final Predicate<Object> validator;
    volatile T value;

    ConfigValue(String path, String comment, T value, Predicate<Object> validator) {
      this.path = path;
      this.comment = comment;
      this.defaultValue = value;
      this.value = value;
      this.validator = validator;
    }

    public T get() {
      return value;
    }

    public void set(T value) {
      if (!validator.test(value)) throw new IllegalArgumentException("Invalid " + path);
      this.value = value;
    }

    @SuppressWarnings("unchecked")
    void read(Object raw) {
      try {
        value = validator.test(raw) ? (T) raw : defaultValue;
      } catch (RuntimeException e) {
        value = defaultValue;
      }
    }
  }

  public static final class BooleanValue extends ConfigValue<Boolean> {
    BooleanValue(String p, String c, boolean d) {
      super(p, c, d, v -> v instanceof Boolean);
    }
  }

  public static final class IntValue extends ConfigValue<Integer> {
    final int min, max;

    IntValue(String p, String c, int d, int min, int max) {
      super(p, c, d, v -> v instanceof Number n && n.longValue() >= min && n.longValue() <= max);
      this.min = min;
      this.max = max;
    }

    @Override
    void read(Object raw) {
      value =
          raw instanceof Number n
              ? (int) Math.max(min, Math.min(max, n.longValue()))
              : defaultValue;
    }
  }

  public static final class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
    EnumValue(String p, String c, T d) {
      super(p, c, d, v -> d.getDeclaringClass().isInstance(v));
    }

    @Override
    void read(Object raw) {
      try {
        value = Enum.valueOf(defaultValue.getDeclaringClass(), raw.toString());
      } catch (RuntimeException e) {
        value = defaultValue;
      }
    }
  }

  public static final class Builder {
    private final Deque<String> sections = new ArrayDeque<>();
    private final List<ConfigValue<?>> values = new ArrayList<>();
    private String comment = "";

    public Builder push(String s) {
      sections.addLast(s);
      return this;
    }

    public Builder pop() {
      sections.removeLast();
      return this;
    }

    public Builder comment(String... lines) {
      comment = String.join("\n", lines);
      return this;
    }

    private String path(String name) {
      return sections.isEmpty() ? name : String.join(".", sections) + "." + name;
    }

    private <T extends ConfigValue<?>> T add(T value) {
      values.add(value);
      comment = "";
      return value;
    }

    public BooleanValue define(String n, boolean d) {
      return add(new BooleanValue(path(n), comment, d));
    }

    public IntValue defineInRange(String n, int d, int min, int max) {
      return add(new IntValue(path(n), comment, d, min, max));
    }

    public <T extends Enum<T>> EnumValue<T> defineEnum(String n, T d, T[] options) {
      return add(new EnumValue<>(path(n), comment, d));
    }

    public ConfigValue<List<? extends String>> defineList(
        String n, List<String> d, Predicate<Object> valid) {
      return add(
          new ConfigValue<>(
              path(n),
              comment,
              List.copyOf(d),
              v -> v instanceof List<?> list && list.stream().allMatch(valid)));
    }

    public <T> Pair<T, UEConfigSpec> configure(Function<Builder, T> constructor) {
      T config = constructor.apply(this);
      return Pair.of(config, new UEConfigSpec(values));
    }
  }
}
