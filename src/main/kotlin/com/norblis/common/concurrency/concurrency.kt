package com.norblis.common.concurrency

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.max


suspend fun suspendingParallelFor(
    threads: Int,
    iterations: Int,
    context: CoroutineContext = Dispatchers.Default,
    f: suspend (i: Int, thread: Int, iterationsOnThread: Int) -> Unit,
) = suspendingParallelFor(threads, 0 until iterations, context, f)

// Uses default dispatcher for work routines.
suspend fun suspendingParallelFor(
    threads: Int,
    range: IntRange,
    context: CoroutineContext = Dispatchers.Default,
    f: suspend (i: Int, thread: Int, iterationsOnThread: Int) -> Unit,
) {
    if (threads == 1) {
        withContext(context) {
            val total = range.last - range.first + 1
            for (i in range) {
                f(i, 0, total)
            }
        }
    } else {
        coroutineScope {
            launchParallelFor(threads, range, this, context, f)
        }
    }
}

fun launchParallelFor(
    threads: Int,
    iterations: Int,
    scope: CoroutineScope,
    context: CoroutineContext,
    f: suspend (i: Int, thread: Int, iterationsOnThread: Int) -> Unit,
): List<Job> = launchParallelFor(threads, 0 until iterations, scope, context, f)

fun launchParallelFor(
    threads: Int,
    range: IntRange,
    scope: CoroutineScope,
    context: CoroutineContext,
    f: suspend (i: Int, thread: Int, iterationsOnThread: Int) -> Unit,
): List<Job> {
    val total = range.last - range.first + 1
    val baseSize = total / threads
    val remainder = total % threads

    var offset = range.first

    val jobs = ArrayList<Job>(threads)
    for (thread in 0 until threads) {
        val chunkSize = baseSize + if (thread < remainder) 1 else 0
        val chunkStart = offset
        offset += chunkSize
        val job = scope.launch(context) {
            for (i in chunkStart until chunkStart + chunkSize) {
                f(i, thread, chunkSize)
            }
        }
        jobs.add(job)
    }
    return jobs
}

fun availableProcessors(): Int {
    return Runtime.getRuntime().availableProcessors()
}

fun defaultWorkerThreadCount(): Int {
    return max(1, (availableProcessors() * 3) / 4)
}

