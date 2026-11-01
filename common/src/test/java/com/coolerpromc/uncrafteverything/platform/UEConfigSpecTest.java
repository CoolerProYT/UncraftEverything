package com.coolerpromc.uncrafteverything.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UEConfigSpecTest {
  @TempDir Path dir;

  enum Mode {
    LEVEL,
    POINT
  }

  static final class Config {
    final UEConfigSpec.IntValue xp;
    final UEConfigSpec.EnumValue<Mode> mode;
    final UEConfigSpec.BooleanValue prioritized;
    final UEConfigSpec.ConfigValue<List<? extends String>> restrictions;

    Config(UEConfigSpec.Builder builder) {
      builder.push("Experience");
      xp = builder.defineInRange("experiences", 1, 0, Integer.MAX_VALUE);
      mode = builder.defineEnum("experienceType", Mode.LEVEL, Mode.values());
      builder.pop();
      builder.push("RecipeSelectionOrder");
      prioritized = builder.define("prioritizeVanillaIngredientRecipe", true);
      builder.pop();
      builder.push("Restrictions");
      restrictions =
          builder.defineList(
              "restrictions", List.of("minecraft:crafting_table"), v -> v instanceof String);
      builder.pop();
    }
  }

  @Test
  void readsLegacyConfigWithoutResettingSettings() throws Exception {
    Path path = dir.resolve("uncrafteverything_common.toml");
    Files.writeString(
        path,
        "[Experience]\n"
            + "experiences = 27\n"
            + "experienceType = \"POINT\"\n"
            + "[Restrictions]\n"
            + "restrictions = [\"mod:keep\"]\n");
    var pair = new UEConfigSpec.Builder().configure(Config::new);
    var config = pair.getLeft();
    var spec = pair.getRight();
    try {
      spec.load(path, () -> {});
      assertEquals(27, config.xp.get());
      assertEquals(Mode.POINT, config.mode.get());
      assertEquals(List.of("mod:keep"), config.restrictions.get());
      assertTrue(config.prioritized.get());
    } finally {
      spec.close();
    }
  }

  @Test
  void persistsUiChangesAndReloadsEnumsAndLists() {
    Path path = dir.resolve("config.toml");
    var first = new UEConfigSpec.Builder().configure(Config::new);
    first.getRight().load(path, () -> {});
    first.getLeft().xp.set(8);
    first.getLeft().mode.set(Mode.POINT);
    first.getLeft().restrictions.set(List.of("#minecraft:logs"));
    first.getRight().save();
    first.getRight().close();
    var second = new UEConfigSpec.Builder().configure(Config::new);
    try {
      second.getRight().load(path, () -> {});
      assertEquals(8, second.getLeft().xp.get());
      assertEquals(Mode.POINT, second.getLeft().mode.get());
      assertEquals(List.of("#minecraft:logs"), second.getLeft().restrictions.get());
    } finally {
      second.getRight().close();
    }
  }

  @Test
  void invalidValuesFallBackSafelyAndXpIsClamped() throws Exception {
    Path path = dir.resolve("config.toml");
    Files.writeString(
        path,
        "[Experience]\n"
            + "experiences = -4\n"
            + "experienceType = \"INVALID\"\n"
            + "[Restrictions]\n"
            + "restrictions = [3]\n");
    var pair = new UEConfigSpec.Builder().configure(Config::new);
    try {
      pair.getRight().load(path, () -> {});
      assertEquals(0, pair.getLeft().xp.get());
      assertEquals(Mode.LEVEL, pair.getLeft().mode.get());
      assertEquals(List.of("minecraft:crafting_table"), pair.getLeft().restrictions.get());
      assertThrows(IllegalArgumentException.class, () -> pair.getLeft().xp.set(-1));
    } finally {
      pair.getRight().close();
    }
  }
}
