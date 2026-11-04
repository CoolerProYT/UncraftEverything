# Sophisticated Backpacks compatibility checks

Run the shared regression suite with Java 25:

```powershell
./gradlew.bat :common:compatibilityTest
```

It uses vanilla Minecraft 26.1.2 and isolated fixtures for the optional APIs, including NeoForge's `size()` handlers, Fabric's `getSlots()` handlers and the Fabric wrapper constructor fallback. The fixtures and test platform services are in their own source set and are never packaged in either mod jar. This suite also runs with `:common:check`.

The NeoForge development runtime includes Sophisticated Backpacks 26.1.2-3.26.18.2238 and Sophisticated Core 26.1.2-1.5.26.1.2454. To test manually, run `./gradlew.bat :neoforge:runClient`.

For integration testing with the real NeoForge mods, run:

```powershell
./gradlew.bat -I tests/sophisticated-backpacks/integration.gradle :neoforge:runSophisticatedCompatibility
```

Gradle downloads the mods from Modrinth Maven; no manual jar downloads are needed. The disposable GameTest world is stored in `.gradle/sophisticated-compat/server-26.1.2/`. The test mod stops the server and exits with a failing status on any assertion failure.

Integration coverage includes actual recipe discovery for five backpack tiers and ordinary, advanced and stack upgrades; storage UUID and installed-upgrade preservation; capacity refresh; overflow and filled-base protection; and uncrafting through both manual and automatic tables, including full output slots. Expected output: `REAL SOPHISTICATED INTEGRATION PASSED 35 checks`.

There is no official Sophisticated Backpacks Fabric build for 26.1.2 available at the time of implementation. Fabric compilation and API fixtures are verified; an end-to-end Fabric run requires a compatible mod release.
