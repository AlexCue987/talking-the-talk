## New In Kotest: More Support For Ranges

### TL;DR:

Add the following matchers for both `ClosedRange` and `OpenEndRange`:

* `shouldBeIn/shouldNotBeIn`
* `shouldBeEmpty/shouldNotBeEmpty`
* `shouldIntersect/shouldNotIntersect`

The following sections provide examples for all these matchers.

### `shouldBeIn/shouldNotBeIn`

```kotlin
1 shouldBeIn (0..2)
1 shouldBeIn (0 until 2)
2 shouldNotBeIn (0 until 2)
```

### `shouldBeEmpty/shouldNotBeEmpty`

```kotlin
(1..0).shouldBeEmpty()
(1..1).shouldNotBeEmpty()
```

### `shouldIntersect/shouldNotIntersect`

```kotlin
(1..3) shouldIntersect (3..4)
(1 until 3) shouldNotIntersect (3..4)
(1..5) shouldIntersect (3..4)
```
