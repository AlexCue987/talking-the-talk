package io.alexcue987.konkurrensy

import com.tgt.trans.common.testhelpers.synkronizer.RaceConditionReproducer.Companion.runInParallel
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime

class RaceConditionReproducerTest: StringSpec() {
    init {
        "two tasks wait on each other" {
            runInParallel({ runner: RaceConditionReproducer ->
                timedPrint("a1")
                runner.await()
                timedPrint("a2")
            },
                { runner: RaceConditionReproducer ->
                    timedPrint("b1")
                    runner.await()
                    timedPrint("b2")
                }
            )
        }

        "two tasks wait on each other, twice" {
            runInParallel({ runner: RaceConditionReproducer ->
                timedPrint("-a1")
                runner.await()
                timedPrint("-a2")
                runner.await()
                timedPrint("-a3")
            },
                { runner: RaceConditionReproducer ->
                    timedPrint("-b1")
                    runner.await()
                    runner.await()
                    timedPrint("-b2")
                }
            )
        }

        "one thread blows up, another times out" {
            runInParallel({ runner: RaceConditionReproducer ->
                runner.await()
                timedPrint("first task")
                runner.await()
            },
                { runner: RaceConditionReproducer ->
                    runner.await()
                    timedPrint("second task")
                    throw RuntimeException("Oops")
                }
            )
            timedPrint("All done")
        }
    }

    fun timedPrint(message: String) =
        println("Time: ${LocalDateTime.now()}, Thread: ${Thread.currentThread().id}, $message")
}
