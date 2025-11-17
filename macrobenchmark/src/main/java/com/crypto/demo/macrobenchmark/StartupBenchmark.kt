package com.crypto.demo.macrobenchmark

import android.util.Log
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupWithBaselineProfile() = runBenchmarkWithFallback(
        compilationMode = CompilationMode.Partial()
    )

    @Test
    fun startupWithoutBaselineProfile() = runBenchmarkWithFallback(
        compilationMode = CompilationMode.None()
    )

    companion object {
        private const val TAG = "StartupBenchmark"
        private const val PACKAGE_NAME = "com.crypto.demo"
        private const val BASELINE_INSTALL_ERROR =
            "baseline profile install broadcast was not received"
    }

    private fun runBenchmarkWithFallback(compilationMode: CompilationMode) {
        try {
            runStartupBenchmark(compilationMode)
        } catch (baselineError: RuntimeException) {
            val message = baselineError.message.orEmpty()
            if (compilationMode is CompilationMode.Partial &&
                message.contains(BASELINE_INSTALL_ERROR, ignoreCase = true)
            ) {
                Log.w(
                    TAG,
                    "Unable to install baseline profile on this build, retrying without it",
                    baselineError
                )
                runStartupBenchmark(CompilationMode.None())
            } else {
                throw baselineError
            }
        }
    }

    private fun runStartupBenchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = compilationMode
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
