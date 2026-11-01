# Uncraft Everything — Minecraft 1.20.1

Forge and Fabric share release version **20.1.0**. Both include the current manual
and automatic uncrafting tables, UI, experience controls and configuration.

The build follows [Jared’s MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template/tree/1.20.1),
using the `multiloader-common` and `multiloader-loader` convention plugins in
`buildSrc`. The `common` project contains the gameplay, inventories, configuration, networking
records/handlers, screens, recipes, integrations and assets. It compiles against
vanilla Minecraft. Loader differences use `Services` interfaces, following the
26.1+ branches; Forge and Fabric provide native adapters and lifecycle entrypoints. Common sources/resources are included in each
loader’s JAR; players install only the JAR for their loader.

## Development

Open the repository root and reload Gradle. Minecraft compilation targets Java 17.

- `gradlew build`: build both loaders, including source and Javadoc JARs.
- `gradlew test`: run the shared unit-test suite.
- `gradlew :forge:runClient` or `gradlew runClient`: run Forge with TaCZ available as a development-only runtime dependency.
- `gradlew :fabric:runClient`: run Fabric.
- `gradlew :forge:genIntellijRuns`: regenerate Forge IDE runs. Fabric runs are generated during Gradle import.
- `gradlew runGameTestServer -PtaczTestVersion=1.1.8-hotfix2`: run real-TaCZ Forge gameplay tests; `1.1.4` is also supported.
- `gradlew :fabric:runGameTestServer -PfabricGameTest`: run Fabric gameplay tests.

Release JARs are in `forge/build/libs` and `fabric/build/libs`. TaCZ remains optional
for players and is never bundled. Gun, ammunition and attachment uncrafting uses
existing TaCZ recipes and exact material quantities; loaded ammunition inside guns
is not refunded.

Publishing tasks are available separately as `:forge:publishMods` and
`:fabric:publishMods`, or together as `publishMods`. They require the configured
publishing credentials. Build and verification tasks do not upload releases.
