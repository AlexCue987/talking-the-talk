package io.alexcue987.konkurrensy

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class RaceConditionReproducer(
    private val timeoutInMs: Long,
    vararg tasks: (runner: RaceConditionReproducer) -> Unit) {
    private val tasks = tasks.toList()
    private val barrier = CyclicBarrier(tasks.size)

    constructor(vararg tasks: (runner: RaceConditionReproducer) -> Unit): this(10000L, *(tasks))

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
        fun runInParallel(vararg tasks: (runner: RaceConditionReproducer) -> Unit) {
            RaceConditionReproducer(*(tasks)).run()
        }
    }
}
