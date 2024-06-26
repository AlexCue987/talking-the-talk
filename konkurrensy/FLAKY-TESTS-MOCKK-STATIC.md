## When Mocking Static Functions Causes Flake Tests

### The Problem

Sometimes our automated tests intermittently fail. For example, the following can happen:
* We've created a new class and made no other changes. Yet all of a sudden an old unchanged test fails, for a class we have not touched.
* Tests are passing locally but failing on the CI server. Yet when we rerun them on the server, they pass.

There are multiple reasons that may cause our tests to be flaky. We'll discuss just one of them.

### One Possible Cause - Mocking Static Method.

Whenever we mock a static function, such as `LocalDate.now()`, in a test, it can affect all other tests running at the same time. The reason is simple: because generally multiple tests are run in parallel, some other tests should be running at the same time with our test. If these other tests invoke the same static function, they will get mock response instead of the real thing and so they will break. If, on the other hand, some other test also mocks the same function, that other mock will override the previous one, and our original test will fail.

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
In isolation, this test will pass. But if we have another function named `daysToDeadline()`, and it also uses `LocalDate.now()` under the hood, then the unit tests for it might also mock the same `LocalDate.now()`. So when we run 
