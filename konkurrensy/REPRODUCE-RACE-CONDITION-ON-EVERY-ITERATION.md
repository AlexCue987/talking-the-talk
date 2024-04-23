## Reproducing Race Condition on Every Iteration
### Why Is That A Problem
Whenever we are building a robust system, we need to consider concurrency and race conditions. 
Otherwise we can end up with a system that passes all unit tests, it can have up to 100% test coverage, and yet have subtle bugs related to concurrency.
But even if we do have tests involving concurrency, and our system does pass them, it might not be enough.
And the reason is simple: our tests might not produce enough race conditions to reproduce a bug. 
And once the system is running in production, eventually reality will reproduce enough race conditions, and we shall see that bug.
<br/>
<br/>
Suppose, for example, that we are building an system that sells tickets, and it passes all unit test. In other words, it works without concurrency. Suppose that we have:
* started two threads
* have those threads buy 100K or 1M tickets, concurrently
* verified the results as follows:
    * all tickets were sold once
    * no tickets were sold twice
    * there were no exceptions
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
<br/>
<br/>
### Using `CyclicBarrier` to Reproduce Race Conditions on Every Iteration

