## Cannot Find Exact Match? Kotest Does Search For Similar Elements.

### TL;DR;

When actual and expected instances are data classes with several fields, and there is no exact match, it really helps to find
similarities aka fuzzy matches, as shown in the section
"Possible matches for missing keys" of the following output, where the
matcher finds the most similar key:
```
shouldThrow<AssertionError> {
   mapOf(
      sweetGreenApple to 1
   ) should containExactly(mapOf(sweetGreenPear to 1))
}.message shouldBe """
|
|Expected:
|  mapOf(Fruit(name=apple, color=green, taste=sweet) to 1)
|should be equal to:
|  mapOf(Fruit(name=pear, color=green, taste=sweet) to 1)
|but differs by:
|  missing keys:
|    Fruit(name=pear, color=green, taste=sweet)
|  extra keys:
|    Fruit(name=apple, color=green, taste=sweet)
|
|Possible matches for missing keys:
|
| expected: Fruit(name=pear, color=green, taste=sweet),
|  but was: Fruit(name=apple, color=green, taste=sweet),
|  The following fields did not match:
|    "name" expected: <"pear">, but was: <"apple">
  """.trimMargin()
```

This is implemented for the following matchers:

* `shouldContain`
* `shouldContainAll`
* `shouldContainExactly`
* `shouldContainExactlyInAnyOrder`


Below are some examples for these matchers:


### `shouldContainAll`

```kotlin
shouldThrowAny {
   listOf(sweetGreenApple, sweetGreenPear, sourYellowLemon).shouldContainAll(
      listOf(sweetGreenApple, sweetRedApple)
   )
}.shouldHaveMessage("""
   |Collection should contain all of [Fruit(name=apple, color=green, taste=sweet), Fruit(name=apple, color=red, taste=sweet)] but was missing [Fruit(name=apple, color=red, taste=sweet)]
   |Possible matches:
   | expected: Fruit(name=apple, color=green, taste=sweet),
   |  but was: Fruit(name=apple, color=red, taste=sweet),
   |  The following fields did not match:
   |    "color" expected: <"green">, but was: <"red">
""".trimMargin())
```

### `shouldContain`

```kotlin
shouldThrowAny {
   listOf(sweetGreenApple, sweetGreenPear) shouldContain (sweetRedApple)
}.shouldHaveMessage(
   """
   |Collection should contain element Fruit(name=apple, color=red, taste=sweet) based on object equality; but the collection is [Fruit(name=apple, color=green, taste=sweet), Fruit(name=pear, color=green, taste=sweet)]
   |PossibleMatches:
   | expected: Fruit(name=apple, color=green, taste=sweet),
   |  but was: Fruit(name=apple, color=red, taste=sweet),
   |  The following fields did not match:
   |    "color" expected: <"green">, but was: <"red">
""".trimMargin()
```

### `shouldContainExactlyInAnyOrder`

```kotlin
shouldThrow<AssertionError> {
   listOf(sweetGreenApple, sweetRedApple).shouldContainExactlyInAnyOrder(listOf(sweetGreenApple, sweetGreenPear))
}.message shouldContain """
   |Possible matches for unexpected elements:
   |
   | expected: Fruit(name=apple, color=green, taste=sweet),
   |  but was: Fruit(name=apple, color=red, taste=sweet),
   |  The following fields did not match:
   |    "color" expected: <"green">, but was: <"red">
""".trimMargin()
}
```
