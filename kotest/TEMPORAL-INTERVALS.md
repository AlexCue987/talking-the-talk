## New in Kotest: Temporal Intervals

### DSL to Describe Durations

```kotlin
(1.minutes and 1.milliseconds).inWholeNanoseconds shouldBe 60_001_000_000L
(1.days and 2.hours and 3.minutes and 4.seconds and 5.milliseconds).inWholeNanoseconds shouldBe 93_784_005_000_000L
```

### OffsetDateTime

```kotlin
OffsetDateTime.of(2023, 11, 14, 1, 2, 0, 0, plusHour) shouldBe
               (OffsetDateTime.of(2023, 11, 14, 1, 30, 0, 0, plusHour) plusOrMinus 30.minutes)
```

### LocalDateTime
```kotlin
LocalDateTime.of(2023, 11, 14, 1, 1) shouldBe
               (LocalDateTime.of(2023, 11, 14, 1, 31) plusOrMinus 30.minutes)
```
