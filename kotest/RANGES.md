## New In Kotest: More Support For Ranges

### TL;DR:

Add the following matchers for both `ClosedRange<T>` and `OpenEndRange<T>`:

* `shouldBeIn/shouldNotBeIn`
* `shouldBeEmpty/shouldNotBeEmpty`
* `shouldIntersect/shouldNotIntersect`
* `shouldBeWithin/shouldNotBeWithin`

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

Range A intersects with range B if at least one element is in both ranges. For example:

```kotlin
(1..3) shouldIntersect (3..4)
(1 until 3) shouldNotIntersect (3..4)
(1..5) shouldIntersect (3..4)
```

### `shouldBeWithin/shouldNotBeWithin`

Range A is within range B if every element in range A is also in range B. For example:

```kotlin
(2..3) shouldBeWithin (1..4)
(2..3) shouldBeWithin (1..3)
(2..3) shouldBeWithin (2 until 4)
(1 until 3) shouldNotBeWithin (2..4)
```

For discrete types of `Int` and `Long`, some results for instances of `OpenEndRange` inside `ClosedRange` may seem counter-intuitive:

```kotlin
(1 until 3) shouldBeWithin (1..2)
(1L until 3L) shouldBeWithin (1L..2L)
```

But it makes sense: `(1 until 3)` has just two elements 1 and 2, and both are in the range `(1..2)`. This is why `(1 until 3) shouldBeWithin (1..2)`
