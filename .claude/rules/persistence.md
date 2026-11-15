---
paths:
  - "core/database/**"
  - "core/datastore/**"
  - "feature/*/data/**"
---

# Persistence recipes, applied when the app first needs local storage

Check the latest versions first (last checked: Room 2.8.x, sqlite-bundled 2.7.x, KSP 2.3.x,
DataStore 1.3.x). KSP must support the project's Kotlin version: add it to Renovate's "Kotlin + SKIE" group.

## Relational data: Room KMP in `core:database`

One database for the app, in `core:database` (Room needs every entity in one `@Database` class).
Feature data modules depend on it and map entities ↔ domain models.

Catalog: `androidx-room-runtime`, `androidx-room-compiler`, `androidx-sqlite-bundled`; plugins
`com.google.devtools.ksp` and `androidx.room`.

`core/database/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.template.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.androidx.room.runtime)
        implementation(libs.androidx.sqlite.bundled)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
    }
}
room { schemaDirectory("$projectDir/schemas") } // commit the schemas, they drive migrations
dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { add(it, libs.androidx.room.compiler) }
}
```

Code (`commonMain`):
```kotlin
@Database(entities = [XEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun xDao(): XDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT") // Room generates the actuals
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
```
- The builder is platform-specific, provided by Koin modules in `androidMain` / `iosMain`:
  - Android: `Room.databaseBuilder<AppDatabase>(androidContext(), androidContext().getDatabasePath("app.db").absolutePath)`.
  - iOS: path from `NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, ...)` + `/app.db`.
- DAOs return `Flow<List<XEntity>>` for observed queries and use `suspend` for writes.
- Entities never reach the domain: map them in the feature's data module (`mapper/`).
- Every schema change bumps `version` and ships an `AutoMigration` or `Migration` (with a test).

## Key-value settings: DataStore Preferences in `core:datastore`

- `androidx.datastore:datastore-preferences-core` in `commonMain`.
- `PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })` with a platform path
  (Android: `context.filesDir.resolve("app.preferences_pb")`; iOS: Documents directory), created once
  as a Koin `single`.
- Wrap it in a domain-facing repository (`SettingsRepository` interface in the feature's domain,
  implemented in its data module). Never expose `Preferences` keys outside data.

## Tests
- Repository logic: fake the DAO/data source interface in `commonTest`.
- Real database: `androidHostTest` with `Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver())`.
