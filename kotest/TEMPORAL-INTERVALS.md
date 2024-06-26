## New in Kotest: Match Temporal Types With Tolerance

### TL;DR;

Add matchers like this:

```kotlin
LocalDateTime.of(2024, 5, 15, 1, 1, 30) shouldBe
               (LocalDateTime.of(2024, 5, 15, 1, 2, 50) plusOrMinus (2.minutes and 30.seconds))
```
Support the following types:
* `Instant`
* `LocalDateTime`
* `LocalTime`
* `OffsetDateTime`
* `ZonedDateTime`

### DSL to Describe Durations

Introduce an infix function `and` on durations such as `2.minutes` which works like this `2.minutes and 30.seconds`
. For instance:


```kotlin
(1.minutes and 1.milliseconds).inWholeNanoseconds shouldBe 60_001_000_000L
(1.days and 2.hours and 3.minutes and 4.seconds and 5.milliseconds).inWholeNanoseconds shouldBe 93_784_005_000_000L
```

This is useful when creating intervals of data, as follows: `LocalDate.of(12, 30) plusOrMinus (2.minutes and 30.seconds)`
<br/>
<br/>
Matching against such intervals of time is implemented for the following temporal types:

### Instant

```kotlin
val currentInstant = Instant.now()
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

onePm shouldBe (LocalTime.of(13, 1, 25) plusOrMinus (1.minutes and 30.seconds))
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
val newYorkTimeZone = ZoneId.of("America/New_York")

ZonedDateTime.of(2023, 11, 14, 1, 2, 0, 0, chicagoTimeZone) shouldBe
   (ZonedDateTime.of(2023, 11, 14, 2, 30, 0, 0, newYorkTimeZone) plusOrMinus (30.minutes and 30.seconds))
```
