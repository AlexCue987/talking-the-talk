## Making Tests Less Fragile With Kotest

A test used to pass, and all of a sudden it's failing? Sounds familiar? There are many reasons why a test can be fragile, and quite often `kotest` has nice features that allow us to fix fragile tests easily.
<br/>
<br/>
Let's refactor a few unreliable tests into more robust ones, using `kotest`'s matchers.

### Name/Value Pairs in Json Are Unordered - use `shouldMatchJson`

According to Json standard, [An object is an unordered set of name/value pairs](https://www.json.org/json-en.html).
<br/>
<br/>
This is why the following test is fragile:
<br/>
`getSomeJson() shouldBe """{"name":"apple","color":"red"}"""`
<br/>
Because next time this function can return name/value pairs in a different order, like this: <br/>
`{"color":"red","name":"apple"}`, and the test will fail.
<br/>
But logically it is the same json, even though it is a different `String`. So a well written test should still pass even when the order of name/value pairs has changed, like the following.
<br/>
`getSomeJson() shouldMatchJson """{"name":"apple","color":"red"}"""`
<br/>
<br/>
