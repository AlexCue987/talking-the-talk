# When Mocking Static Functions Causes Tests to Fail Intermittently

## The Problem

Sometimes our automated tests intermittently fail. For example, the following can happen:
* We've created a new class and made no other changes. Yet all of a sudden an old unchanged test fails, for a class we have not touched.
* Tests are passing locally but failing on the CI server. Yet when we rerun the same tests on the same server again, they pass.

There are multiple reasons that may cause our tests to be flaky. We'll discuss just one of them.

## One Possible Cause - Mocking Static Method.

Whenever we mock a static function, such as `LocalDate.now()`, in a test, it can affect all other tests running at the same time. 
<br />
<br />
The reason is simple: because generally multiple tests are run in parallel, some other tests should be running at the same time with our test. If these other tests invoke the same static function, they will get mock response instead of the real thing and so they will break. If, on the other hand, some other test also mocks the same function, that other mock will override the previous one, and our original test will fail.
<br />
<br />
For example, suppose we need to unit test the following function, which uses static function `LocalDate.now()`:

```kotlin
fun nextMonday(): LocalDate {
    var date = LocalDate.now()
    while(date.dayOfWeek != DayOfWeek.MONDAY) { 
        date = date.plusDays(1L)
    }
    return date
}
```
We might create a few unit tests that mock `LocalDate.now()` and look like this:
```kotlin
  "nextMonday returns same date on Monday" {
      mockkStatic(LocalDate::class)
      val monday = LocalDate.of(2024, 4, 1)
      every { LocalDate.now(any<Clock>()) } returns monday
      nextMonday() shouldBe monday
  }
```
In isolation, this test will pass. But let's suppose that we have another function named `daysToNextNewYear()`, and it also uses `LocalDate.now()` under the hood, as follows:

```kotlin
fun daysToNextNewYear(): Long {
    val today = LocalDate.now()
    val nextNewYear = LocalDate.of(today.year + 1, 1, 1)
    return ChronoUnit.DAYS.between(today, nextNewYear)
}
```
Of course, the unit tests for `daysToNextNewYear()` might also mock the same `LocalDate.now()`. For example:
```kotlin
"daysToNextNewYear" {
      mockkStatic(LocalDate::class)
      val today = LocalDate.of(2024, 4, 5)
      every { LocalDate.now(any<Clock>()) } returns today
      daysToNextNewYear() shouldBe 271L
}
```

So when we run all tests, unit tests for `nextMonday()` might run at the same time as `daysToNextNewYear()`, and we can get a collision and failing unit test(s). Let us reproduce the problem.

## Reproducing Race Condition Between Unit Tests

Let's use `RaceConditionReproducer` which was [discussed here](https://github.com/AlexCue987/talking-the-talk/blob/main/konkurrensy/reproduce-race-conditions/RACE-CONDITIONS-REPRODUCER.md). The following code shows one possible collision, when the mocking for function `daysToNextNewYear` overrides previous mock for function `nextMonday`:

```koltin
        "demo for mockkStatic - mocking on one thread overrides mocking on another thread".config(enabled = true) {
            RaceConditionReproducer.runInParallel({ runner: RaceConditionReproducer ->
                    mockkStatic(LocalDate::class)
                    val today = LocalDate.of(2022, 4, 27)
                    every { LocalDate.now(any<Clock>()) } returns today
                    timedPrint("After mock on first thread: ${LocalDate.now().toString()}")
                //Time: 2024-04-05T14:08:01.920182, Thread: 50, After mock on first thread: 2022-04-27
                    runner.await()
                    runner.await()
                    timedPrint(
                        "After mock on first thread was overridden on second thread: ${
                            LocalDate.now().toString()
                        }"
                    )
                //Time: 2024-04-05T14:08:01.991910, Thread: 50, After mock on first thread was overridden on second thread: 2024-04-04
                },
                { runner: RaceConditionReproducer ->
                    runner.await()
                    timedPrint(
                        "Before mock on second thread, LocalDate.now() was mocked on first thread: ${
                            LocalDate.now().toString()
                        }"
                    )
                    //Time: 2024-04-05T14:08:01.974678, Thread: 51, Before mock on second thread, LocalDate.now() was mocked on first thread: 2022-04-27
                    mockkStatic(LocalDate::class)
                    val today = LocalDate.of(2024, 4, 4)
                    every { LocalDate.now(any<Clock>()) } returns today
                    timedPrint(
                        "After mock on second thread has overridden previous mocking: ${
                            LocalDate.now().toString()
                        }"
                    )
                    //Time: 2024-04-05T14:08:01.988905, Thread: 51, After mock on second thread has overridden previous mocking: 2024-04-04
                    runner.await()
                }
            )
            /* The full output from this test:
Time: 2024-04-05T14:08:01.920182, Thread: 50, After mock on first thread: 2022-04-27
Time: 2024-04-05T14:08:01.974678, Thread: 51, Before mock on second thread, LocalDate.now() was mocked on first thread: 2022-04-27
Time: 2024-04-05T14:08:01.988905, Thread: 51, After mock on second thread has overridden previous mocking: 2024-04-04
Time: 2024-04-05T14:08:01.991910, Thread: 50, After mock on first thread was overridden on second thread: 2024-04-04
             */
        }

```

## Fixing the Problem

There are several ways to deal with this issue.

### Pass A Parameter

Instead of 

```kotlin
fun daysToNextNewYear(): Long {
    val today = LocalDate.now()
    val nextNewYear = LocalDate.of(today.year + 1, 1, 1)
    return ChronoUnit.DAYS.between(today, nextNewYear)
}
```
we can pull variable named `today` up into a parameter as follows:

```kotlin
fun daysToNextNewYear(today: LocalDate): Long {
    val nextNewYear = LocalDate.of(today.year + 1, 1, 1)
    return ChronoUnit.DAYS.between(today, nextNewYear)
}
```
This removes the need to mock `LocalDate.now()`.

### Use An Injectable Class

Instead of
```kotlin
class MyService {
    fun daysToNextNewYear(): Long {
        val today = LocalDate.now()
        val nextNewYear = LocalDate.of(today.year + 1, 1, 1)
        return ChronoUnit.DAYS.between(today, nextNewYear)
    }
}
```

wrap static function in a class named `DateFactory`, instances of which can be easily mocked:

```kotlin
class DateFactory {
    fun today() = LocalDate.now()
}
class MyService(
    private val dateFactory: DateFactory
) {
    fun daysToNextNewYear(): Long {
        val today = dateFactory.today()
        val nextNewYear = LocalDate.of(today.year + 1, 1, 1)
        return ChronoUnit.DAYS.between(today, nextNewYear)
    }
}
```

### Make Sure Tests Do Not Run In Parallel

For example:
```kotlin
@DoNotParallelize
class DaysToNextNewYearTest: StringSpec() {
    init {
        "daysToNextNewYear" {
            daysToNextNewYear() shouldBe 271L
        }
    }
}
```
We need to use this annotation sparingly, as it slows down testing.


