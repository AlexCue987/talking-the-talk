## Making Tests Less Fragile With Kotest

A test used to pass, and all of a sudden it's failing? Sounds familiar? There are many reasons why a test can be fragile, and quite often `kotest` has nice features that allow us to fix fragile tests easily.
<br/>
<br/>
Let's refactor a few unreliable tests into more robust ones, using `kotest`'s matchers.

### Name/Value Pairs in Json Are Unordered - use `shouldEqualJson`

According to Json standard, [An object is an unordered set of name/value pairs](https://www.json.org/json-en.html).
<br/>
<br/>
This is why the following test is fragile:
<br/>
<br/>
`getSomeJson() shouldBe """{"name":"apple","color":"red"}"""`
<br/>
<br/>
Because next time this function can return name/value pairs in a different order, like this: <br/>
<br/>
`{"color":"red","name":"apple"}`, and the test will fail.
<br/>
<br/>
But logically it is the same json, even though it is a different `String`. So a well written test should still pass even when the order of name/value pairs has changed, like the following:
<br/>
<br/>
```kotlin
"""{"color":"red","name":"apple"}""" shouldEqualJson """{"name":"apple", "color":"red"}"""
```
<br/>
<br/>

### The Test Is Still Fragile - Adding New Field Breaks It

Suppose we need add one more field, named `weight`, to the data class we've serialized our `json` from, as follows:

```kotlin
data class Thing(
  val color: String,
  val name: String,
  val weight: BigDecimal,
)
```

This change will break our unit test, and we'll need to add a new field to expected json:

```kotlin
actual shouldEqualJson """{"name":"apple", "color":"red", "weight": 0.34}"""
```

But let's ask ourselves a simple question: why do we need this test at all? What exactly are we trying to test here?
<br/>
<br/>

