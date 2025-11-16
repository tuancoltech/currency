package com.crypto.demo.di

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertSame
import org.junit.Test

class DispatchersModuleTest {

    @Test
    fun provideIoDispatcherReturnsIoDispatcher() {
        val dispatcher = DispatchersModule.provideIoDispatcher()
        assertSame(Dispatchers.IO, dispatcher)
    }
}
