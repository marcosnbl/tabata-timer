# Implementation Plan - Fix Errors, Vulnerabilities, and Bugs

This plan addresses several technical issues identified in the Tabata Timer project, ranging from timer accuracy to resource management and UI/UX improvements.

## User Review Required

> [!IMPORTANT]
> The timer logic will be changed from a simple `delay(1000)` to a time-reference based approach. This ensures the timer doesn't drift over long workouts.

> [!NOTE]
> I will add `android:launchMode="singleTop"` to `MainActivity` to prevent multiple instances of the app from opening when clicking notifications.

## Proposed Changes

### Domain Layer

#### [MODIFY] [TimerManager.kt](file:///C:/Users/marco/Documents/repos/tabatatimer/app/src/main/java/com/example/tabata_timer/domain/timer/TimerManager.kt)
- Replace `delay(1000)` with a calculation based on `System.currentTimeMillis()` to prevent drift.
- Improve `tick` function to be more robust against coroutine cancellation and pauses.

### Data Layer

#### [MODIFY] [AndroidSoundPlayer.kt](file:///C:/Users/marco/Documents/repos/tabatatimer/app/src/main/java/com/example/tabata_timer/data/audio/AndroidSoundPlayer.kt)
- Implement a simple queue to hold sounds that are requested while TextToSpeech is still initializing.
- Ensure `release()` is called when appropriate (though it's a Singleton, it's good practice).

#### [MODIFY] [TimerService.kt](file:///C:/Users/marco/Documents/repos/tabatatimer/app/src/main/java/com/example/tabata_timer/data/service/TimerService.kt)
- Use proper monochrome icons for notifications (if available, otherwise keep placeholder but note it).
- Ensure `stopForeground(STOP_FOREGROUND_REMOVE)` is called when the service stops.

### App / Manifest

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/marco/Documents/repos/tabatatimer/app/src/main/AndroidManifest.xml)
- Set `android:launchMode="singleTop"` for `MainActivity`.

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure no regressions in basic logic.
- I will create a small scratch script to verify the timing accuracy if possible, or manually verify by comparing with a system clock.

### Manual Verification
- Deploy to the device and verify that:
    - The timer starts and counts down correctly.
    - Sounds are played even for the first "Get ready" message.
    - Clicking the notification resumes the existing app instance.
    - Pausing and resuming works without losing time.
