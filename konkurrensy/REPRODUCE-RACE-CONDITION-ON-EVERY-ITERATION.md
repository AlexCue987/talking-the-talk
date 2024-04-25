## DRAFT - Reproducing Race Condition on Every Iteration
### Why Is That A Problem
Whenever we are building a robust system, we need to consider concurrency and race conditions. 
Otherwise we can end up with a system that passes all unit tests, it can have up to 100% test coverage, and yet have subtle bugs related to concurrency.
But even if we do have tests involving concurrency, and our system does pass them, it might not be enough.
And the reason is simple: our tests might not produce enough race conditions to reproduce a bug. 
And once the system is running in production, eventually reality will reproduce enough race conditions, and we shall see that bug.
<br/>
<br/>
Suppose, for example, that we are building an system that sells tickets, and it passes all unit tests. In other words, it works without concurrency. Suppose that we have:
* started two threads
* have those threads buy 100K or 1M tickets, concurrently
* verified the results as follows:
    * all tickets were sold once
    * no tickets were sold twice
    * there were no exceptions
<br/>
So all our checks have passed. Does it mean that the system is good to go? Not necessarily. There is still a possibility that our test did not reproduce enough race conditions.
<br/>
<br/>
As an extreme example, suppose that our test has both buyers try to buy tickets consecutively, starting from 1 and ending with 1_000_000, but one buyer has started just a little bit faster and managed to buy all the tickets, and the other buyer was too late to buy even a single one.
<br/>
<br/>
Or a more moderate example, when every buyer tries to buy tickets with random numbers, and they never try to get the same ticket at the same time, so there are no race conditions.
<br/>
<br/>
The following example shows how to reproduce race conditions easier and faster.

### Using CyclicBarrier to Reproduce Race Conditions on Every Iteration

Using a simple tool, we shall accomplish the following
 * all tickets were sold once
 * no tickets were sold twice
 * there were no exceptions
 * both buyers tried to acquire every ticket at more or less the same time. In other words, the intervals of time between requesting a ticket and getting either the ticket or rejection mostly overlap.

```kotlin
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.concurrent.thread

class SynchronizedRunnerTest: StringSpec() {
    init {
        "just buy tickets concurrently" {
            val count = 10000
            val acquiredTickets = List(2) { mutableListOf<Int>() }
            val seller = NonThreadSafeTicketsSeller(count)
            val threads = listOf(0, 1).map { runnerIndex ->
                thread {
                    (0 until count).forEach { iteration ->
                        if (seller.getTicket(iteration)) {
                            acquiredTickets[runnerIndex].add(iteration)
                        }
                    }
                }
            }
            threads.forEach { it.join() }
            assertSoftly {
                acquiredTickets.sumOf { it.size } shouldBe count
                acquiredTickets[0].filter { it in acquiredTickets[1] } shouldBe listOf()
            }
        }
        
        "invoke non-thread safe implementation" {
            val count = 10000
            val acquiredTickets = List(2) { mutableListOf<Int>() }
            val seller = NonThreadSafeTicketsSeller(count)
            val buyTicket = { runnerIndex: Int, iteration: Int ->
                if (seller.getTicket(iteration)) {
                    acquiredTickets[runnerIndex].add(iteration)
                }
            }
            SynchronizedRunner.runInSync(count, buyTicket, buyTicket)
            assertSoftly {
                acquiredTickets.sumOf { it.size } shouldBe count
                acquiredTickets[0].filter { it in acquiredTickets[1] } shouldBe listOf()
            }
        }
    }
}

class NonThreadSafeTicketsSeller(
    val count: Int
) {
    private val ticketsAvaiability = MutableList(count) { true }
    fun getTicket(index: Int): Boolean {
        require(index in 0 until count) { "Invalid index: $index" }
        return ticketsAvaiability[index].also {
            ticketsAvaiability[index] = false
        }
    }
}

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class SynchronizedRunner(
    private val timeoutInMs: Long,
    vararg tasks: (runnerIndex: Int, iteration: Int) -> Unit) {
    private val tasks = tasks.toList()
    private val barrier = CyclicBarrier(tasks.size)

    constructor(vararg tasks: (runnerIndex: Int, iteration: Int) -> Unit): this(1000L, *(tasks))

    fun run(count: Int) {
        val threads = tasks.mapIndexed { runnerIndex, task ->
            thread(start = false) {
                repeat(count) { iteration ->
                    await()
                    task(runnerIndex, iteration)
                }
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }

    fun await() = barrier.await(timeoutInMs, TimeUnit.MILLISECONDS)

    companion object {
        fun runInSync(count: Int, vararg tasks: (runnerIndex: Int, iteration: Int) -> Unit) {
            SynchronizedRunner(*(tasks)).run(count)
        }
    }
}
```
