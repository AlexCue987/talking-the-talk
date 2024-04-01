import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import com.tgt.trans.common.testhelpers.synkronizer.ParallelRunner.Companion.runInParallel
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockkStatic
import java.time.Clock
import java.time.LocalDate


class ParallelRunner2Test: StringSpec() {
    init {
        "mockkStatic affects code running on other thread".config(enabled = true) {
            runInParallel(
                { runner: ParallelRunner ->
                    timedPrint("Before mock on same thread: ${LocalDate.now().toString()}")
                    mockkStatic(LocalDate::class)
                    val localTime = LocalDate.of(2022, 4, 27)
                    every { LocalDate.now(any<Clock>()) } returns localTime
                    timedPrint("After mock on same thread: ${LocalDate.now().toString()}")
                    runner.await()
                },
                { runner: ParallelRunner ->
                    timedPrint("Before mock on other thread: ${LocalDate.now().toString()}")
                    runner.await()
                    timedPrint("After mock on other thread: ${LocalDate.now().toString()}")
                }
            )
            /*
Time: 2024-04-01, Thread: 50, Before mock on same thread: 2024-04-01
Time: 2024-04-01, Thread: 51, Before mock on other thread: 2024-04-01
Time: 2022-04-27, Thread: 50, After mock on same thread: 2022-04-27
Time: 2022-04-27, Thread: 51, After mock on other thread: 2022-04-27
             */
        }

        "demo for mockkStatic - mocking on second thread overrides previous mocking on first one".config(enabled = true) {
            runInParallel({ runner: ParallelRunner ->
                    mockkStatic(LocalDate::class)
                    val localTime = LocalDate.of(2022, 4, 27)
                    every { LocalDate.now(any<Clock>()) } returns localTime
                    timedPrint("First thread - after mocking: ${LocalDate.now().toString()}")
                    runner.await()
                    runner.await()
                    timedPrint("First thread - after another thread re-mockked same function: ${LocalDate.now().toString()}")
                },
                { runner: ParallelRunner ->
                    runner.await()
                    mockkStatic(LocalDate::class)
                    val localTime = LocalDate.of(2023, 1, 2)
                    every { LocalDate.now(any<Clock>()) } returns localTime
                    timedPrint("Second thread - after mocking: ${LocalDate.now().toString()}")
                    runner.await()
                }
            )
            /*
Time: 2022-04-27, Thread: 50, First thread - after mocking: 2022-04-27
Time: 2023-01-02, Thread: 51, Second thread - after mocking: 2023-01-02
Time: 2023-01-02, Thread: 50, First thread - after another thread re-mockked same function: 2023-01-02
             */
        }

    }

    fun timedPrint(message: String) =
        println("Time: ${LocalDate.now()}, Thread: ${Thread.currentThread().id}, $message")
}

class ParallelRunner(
    private val timeoutInMs: Long,
    vararg tasks: (runner: ParallelRunner) -> Unit) {
    private val tasks = tasks.toList()
    private val barrier = CyclicBarrier(tasks.size)

    constructor(vararg tasks: (runner: ParallelRunner) -> Unit): this(10000L, *(tasks))

    fun run() {
        val threads = tasks.map { task ->
            thread(start = false) {
                task(this)
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }

    fun await() = barrier.await(timeoutInMs, TimeUnit.MILLISECONDS)

    companion object {
        fun runInParallel(vararg tasks: (runner: ParallelRunner) -> Unit) {
            ParallelRunner(*(tasks)).run()
        }
    }
}
