package androidx.test.platform.concurrent

import java.util.concurrent.Executor

/**
 * Minimal implementation to satisfy Espresso dependency when the platform
 * concurrent module is not available in the test APK.
 */
object DirectExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}
