package com.tgt.trans.dmo

import io.kotest.core.spec.style.StringSpec
import mu.KotlinLogging
import org.postgresql.util.PSQLException
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


// paste into some other repo

class KonkurrencyTest: StringSpec() {
    private val jdbi = getTestJdbi()
    private val logger = KotlinLogging.logger {}

    init {
        "create partitions" {
            val createPartition = {runnerIndex: Int, iteration: Int ->
                logger.info { "Creating partition $iteration in runner $runnerIndex" }
                try {
                jdbi.useHandle<Exception> { handle ->
                    handle.createUpdate("CREATE TABLE IF NOT EXISTS partitions_sample_$iteration PARTITION OF partitions_sample FOR VALUES IN ('$iteration')").execute()
                }
                } catch(ex: Exception) {
                    when (ex.cause) {
                        is PSQLException -> {
                            val sqlState = (ex.cause as PSQLException).sqlState
                            //42P07
                            logger.error(ex) { "Error runnerIndex=$runnerIndex iteration=$iteration sqlState=$sqlState" }
                        }
                        else ->
                            logger.error(ex) { "Error runnerIndex=$runnerIndex iteration=$iteration" }
                    }
                }
            }
            runInSync(10000, createPartition, createPartition)
        }
    }
}

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
