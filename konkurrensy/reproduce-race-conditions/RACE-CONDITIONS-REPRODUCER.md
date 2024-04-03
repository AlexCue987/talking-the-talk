# Reproducing Race Conditions with `RaceConditionReproducer`

Sometimes we need to reproduce a race condition. Maybe we want to understand it better. Or maybe we want to test how our code handles it.
< br/>
One way to accomplish that is to just run vulnerable code from multiple threads and see what happens. Eventually that should work, but it can take some time.
<br/>
<br/>
But there are tools that make this task easier, such as the `RaceConditionReproducer` which we shall discuss in this write-up.

## Reproducing a deadlock
