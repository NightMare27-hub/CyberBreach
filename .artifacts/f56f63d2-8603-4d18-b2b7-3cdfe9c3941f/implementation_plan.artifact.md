# Implementation Plan - Phase 1: Project Foundation & Core UI

This plan outlines the steps to build Phase 1 of **Cyber Breach**, establishing the project structure, models, static screens, tabs, WebView lessons, and the room terminal UI.

## User Review Required

> [!IMPORTANT]
> - **Language Transition**: The project template generated a Kotlin `MainActivity.kt`. Phase 1 uses Java (`MainActivity.java`, etc.). We will remove the template `MainActivity.kt` and replace it with Java implementations.
> - **SDK & Dependencies**: Target SDK 37, minSdk 24, Java 11, with RecyclerView and ViewPager2 dependencies added to `app/build.gradle.kts`.

## Proposed Changes

### Build & Manifest Configuration
- **[MODIFY]** [build.gradle.kts](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/build.gradle.kts): Enable `viewBinding`, Java 11, add RecyclerView and ViewPager2 dependencies.
- **[MODIFY]** [AndroidManifest.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/AndroidManifest.xml): Register `RoomActivity` with `screenOrientation="portrait"`.

### Resources & Assets
- **[MODIFY]** [colors.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/values/colors.xml): Add terminal color definitions (`terminal_bg`, `terminal_green`, etc.).
- **[MODIFY]** [strings.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/values/strings.xml): Add app strings, UI labels, and string arrays for modes, lessons, and tools.
- **[NEW]** [ports.html](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/assets/lessons/ports.html): Lesson HTML on ports.
- **[NEW]** [bruteforce.html](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/assets/lessons/bruteforce.html): Lesson HTML on brute-force attacks.

### Data Models & Sample Data
- **[NEW]** [LogEntry.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/model/LogEntry.java): Plain Java model for log entries.
- **[NEW]** [Level.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/model/Level.java): Plain Java model for game levels.
- **[NEW]** [SampleData.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/model/SampleData.java): Built-in sample levels repository.

### UI Layouts & Components
- **[DELETE]** [MainActivity.kt](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/MainActivity.kt): Remove template Kotlin activity.
- **[NEW]** [MainActivity.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/MainActivity.java): Main activity with ViewPager2 and TabLayoutMediator.
- **[NEW]** [HomePagerAdapter.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/HomePagerAdapter.java): Adapter for Play, Learn, and Profile fragments.
- **[NEW]** [activity_main.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/activity_main.xml): Main layout containing Toolbar, TabLayout, and ViewPager2.

#### Play Tab
- **[NEW]** [PlayFragment.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/PlayFragment.java): Play fragment with mode Spinner and level RecyclerView.
- **[NEW]** [fragment_play.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/fragment_play.xml): Layout for PlayFragment.
- **[NEW]** [LevelAdapter.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/LevelAdapter.java): RecyclerView adapter for level cards.
- **[NEW]** [item_level.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/item_level.xml): Card layout for level items.

#### Learn Tab
- **[NEW]** [LearnFragment.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/LearnFragment.java): Learn fragment with lesson Spinner and WebView.
- **[NEW]** [fragment_learn.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/fragment_learn.xml): Layout for LearnFragment.

#### Profile Tab
- **[NEW]** [ProfileFragment.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/ProfileFragment.java): Profile fragment with placeholder stats and settings.
- **[NEW]** [fragment_profile.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/fragment_profile.xml): Layout for ProfileFragment.

#### Room Screen
- **[NEW]** [RoomActivity.java](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/java/com/example/cyberbreach/RoomActivity.java): Room activity displaying timer, breach meter, toolbelt spinner, and terminal table layout.
- **[NEW]** [activity_room.xml](file:///C:/Users/Asus/AndroidStudioProjects/CyberBreach/app/src/main/res/layout/activity_room.xml): Layout for RoomActivity.

## Verification Plan

### Automated Tests
- Build project using Gradle (`app:assembleDebug`) to verify compilation and resource validity.

### Manual Verification
- Deploy app to emulator/device.
- Verify tab swiping between Play, Learn, and Profile.
- Verify lesson loading in WebView via Spinner selection on Learn tab.
- Tap a level on Play tab to open RoomActivity, verifying terminal table layout, toolbelt spinner, and timer.
