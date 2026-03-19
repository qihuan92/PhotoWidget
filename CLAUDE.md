# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PhotoWidget is an Android photo widget application that allows users to add multiple photos to home screen widgets with support for image cropping, rounded corners, and margin adjustments.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run a single test
./gradlew test --tests "com.example.MyTestClass"

# Clean and rebuild
./gradlew clean assembleDebug
```

## Architecture

This is a multi-module Android project using Kotlin:

- **app** - Main application module with app entry point and DI configuration
- **core:model** - Data models and enums (WidgetType, LinkType, PhotoScaleType, etc.)
- **core:database** - Room database with DAOs, entities, and migrations
- **core:analysis** - App analytics and event tracking via AppCenter
- **core:common** - Shared utilities, extensions, views, and repository implementations
- **feature:main** - Main screen with widget list using Paging 3
- **feature:widget** - Widget provider, configuration activities, and photo management
- **feature:settings** - App settings
- **feature:link** - Link/URL handling and app picker
- **feature:about** - About screen and licenses

## Key Technologies

- **DI**: Koin 4.2.0
- **Database**: Room 2.8.4 with KSP
- **Image Loading**: Glide 5.0.5 with KSP
- **UI**: ViewBinding + DataBinding, Material Design 3
- **Async**: Kotlin Coroutines
- **Paging**: AndroidX Paging 3.4.2
- **Widget Updates**: JobScheduler (not WorkManager - see note below)

## Important Implementation Notes

1. **Widget Updates**: The project uses JobScheduler instead of WorkManager for widget refresh. This is intentional - WorkManager triggers AppWidgetProvider.onUpdate() which can cause infinite loops when combined with widget refresh logic.

2. **Build Configuration**: Uses Gradle version catalog (`gradle/libs.versions.toml`). Versions are defined centrally there.

3. **Database Migrations**: Migrations are in `core/database/src/main/java/.../migration/`. Each migration is numbered (e.g., MigrationFor1To2, MigrationFor3To4).

4. **Widget Provider**: Located in `feature/widget/src/main/java/.../provider/PhotoWidgetProvider.kt`