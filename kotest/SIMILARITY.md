## Kotest Cannot Find Exact Match? Now It Searches For Similar Elements.

### TL;DR;

If all the elements in the collection are short, such as Integers or short strings, it is usually crystal clear why the test has failed. For instance:

```kotlin
listOf(1, 2, 3, 4) shouldContainExactlyInAnyOrder listOf(2, 3, 4, 5)

Collection should contain [2, 3, 4, 5] in any order, but was [1, 2, 3, 4]
```

But if the elements are instances of data classes with multiple fields, it is not so easy to figure out what exactly is different. In the following example elements have only three fields, and all fields are short, so it is a bit simpler than some real life use cases. Yet it takes time and some scrolling left and right to determine what exactly is different: 

```kotlin
listOf(Fruit("pear", "green", "sweet"), Fruit("pear", "green", "sweet"), Fruit("apple", "green", "sweet")) shouldContainExactlyInAnyOrder
    listOf(Fruit("pear", "green", "sweet"), Fruit("apple", "red", "sweet"), Fruit("pear", "green", "sweet"))

Collection should contain [Fruit(name=pear, color=green, taste=sweet), Fruit(name=apple, color=red, taste=sweet), Fruit(name=pear, color=green, taste=sweet)] in any order, but was [Fruit(name=pear, color=green, taste=sweet), Fruit(name=pear, color=green, taste=sweet), Fruit(name=apple, color=green, taste=sweet)]
```

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
