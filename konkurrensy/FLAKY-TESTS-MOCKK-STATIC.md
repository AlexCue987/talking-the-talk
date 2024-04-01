## When Mocking Static Functions Causes Flake Tests

### The Problem

Sometimes our automated tests intermittently fail. For example, the following can happen:
* We've created a new class and made no other changes. Yet all of a sudden an old unchanged test fails, for a class we have not touched.
* Tests are passing locally but failing on the CI server. Yet when we rerun them on the server, they pass.

There are multiple reasons that may cause our tests to be flaky. We'll discuss just one of them.

### One Possible Cause - Mocking Static Method.

Suppose we need to test the following function, which uses static function `LocalDate.now()`:
