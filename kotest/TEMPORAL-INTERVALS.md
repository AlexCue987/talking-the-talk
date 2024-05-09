## New in Kotest: Temporal Intervals

### DSL to Describe Durations

```kotlin
(1.minutes and 1.milliseconds).inWholeNanoseconds shouldBe 60_001_000_000L
(1.days and 2.hours and 3.minutes and 4.seconds and 5.milliseconds).inWholeNanoseconds shouldBe 93_784_005_000_000L
```

### Instant

```kotlin
val currentInstant = Instant.now()
val hundredNanosAgoInstant = currentInstant.minusNanos(5L)
val fiveNanosAgoInstant = currentInstant.minusNanos(5L)

currentInstant shouldBe (fiveNanosAgoInstant plusOrMinus 5L.nanoseconds)
fiveNanosAgoInstant shouldBe (currentInstant plusOrMinus 5L.nanoseconds)
```

### LocalDateTime
```kotlin
LocalDateTime.of(2023, 11, 14, 1, 1) shouldBe
               (LocalDateTime.of(2023, 11, 14, 1, 31) plusOrMinus 30.minutes)
```

### LocalTime

```kotlin
val onePm = LocalTime.of(13, 0, 0)

onePm shouldBe (LocalTime.of(13, 1, 0) plusOrMinus 2.minutes)
onePm shouldBe (LocalTime.of(12, 59, 0) plusOrMinus 2.minutes)
```

There is a twist to intervals of `LocalTime`: unlike all intervals of other temporal types, they can wrap around midnight, as follows:

```kotlin
val oneMinuteAfterMidnight = LocalTime.of(0, 1, 0)
LocalTime.of(23, 59) shouldBe (oneMinuteAfterMidnight plusOrMinus 2.minutes)
```

### OffsetDateTime

```kotlin
OffsetDateTime.of(2023, 11, 14, 1, 2, 0, 0, plusHour) shouldBe
               (OffsetDateTime.of(2023, 11, 14, 1, 30, 0, 0, plusHour) plusOrMinus 30.minutes)
```

### ZonedDateTime

```kotlin
val chicagoTimeZone = ZoneId.of("America/Chicago")
newYorkTimeZone = ZoneId.of("America/New_York")

ZonedDateTime.of(2023, 11, 14, 1, 2, 0, 0, chicagoTimeZone) shouldBe
   (ZonedDateTime.of(2023, 11, 14, 2, 30, 0, 0, newYorkTimeZone) plusOrMinus (30.minutes and 30.seconds))
```
