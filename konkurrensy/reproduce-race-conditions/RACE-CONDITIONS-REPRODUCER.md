# Reproducing Race Conditions with `RaceConditionReproducer`

Sometimes we need to reproduce a race condition. Maybe we want to understand it better. Or maybe we want to test how our code handles it.
<br/>
One way to accomplish that is to just run vulnerable code from multiple threads and see what happens. Eventually that should work, but it can take some time.
<br/>
<br/>
But there are tools that make this task easier, such as the `RaceConditionReproducer` which we shall discuss in this write-up.

## Reproducing a deadlock with `RaceConditionReproducer`

For example, let us try to reproduce a deadlock with `RaceConditionReproducer`. Let's suppose we are working with Postgres. 
<br/>
The following two database updates can embrace in a deadlock, if they start at more or less the same time. Both touch the same two tables in a transaction, in opposite order. The first update touches table `apples` and then table `oranges`:

```kotlin
useTransaction {
    runSql("UPDATE apples SET weight = 3.4 WHERE id = 1")
    runSql("UPDATE oranges SET weight = 3.2 WHERE id = 1")
}
```
The first update touches table `oranges` and then table `apples`:
```kotlin
useTransaction {
    runSql("UPDATE oranges SET weight = 3.2 WHERE id = 1")
    runSql("UPDATE apples SET weight = 3.4 WHERE id = 1")
}
```
If we repeatedly run these two transactions from two different threads at the same time, we should eventually get a deadlock. But we don't know how many iterations it will take.
<br/>
<br/>
Alternatively, we can use a helper class `RaceConditionReproducer` that will reproduce a deadlock every time. With the help of this class we shall:
* In one thread, begin transaction, do the first update, and wait for the other thread to catch up.
* In another thread, do the same: begin transaction, do the first update, and wait for the first thread to catch up.
* Once both threads are waiting for each other, they both can continue to do their second updates and commit.
* Soon the database will discover the deadlock, and one fail one of these two transactions.
* We are sure one transaction will become deadlock victim, but we don't know which one - it's Postgres's decision.
<br/>
<br/>
The following code uses `RaceConditionReproducer` to reliably reproduce the deadlock. Note: when `await()` is invoked from a thread, the thread starts waiting for the other thread to catch up. Once both threads have invoked `await()`, they both proceeed.

```kotlin
runInParallel(
    { runner: ParallelRunner ->
        useTransaction {
            runSql("UPDATE apples SET weight = 3.4 WHERE id = 1")
            runner.await()
            runSql("UPDATE oranges SET weight = 3.2 WHERE id = 1")
        }
    },
    { runner: ParallelRunner ->
        useTransaction {
            runSql("UPDATE oranges SET weight = 3.2 WHERE id = 1")
            runner.await()
            runSql("UPDATE apples SET weight = 3.4 WHERE id = 1")
        }
    }
)
```
