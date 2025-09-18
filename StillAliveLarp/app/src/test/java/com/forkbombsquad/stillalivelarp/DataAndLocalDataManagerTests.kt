package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.BaseUiTestClass
import com.forkbombsquad.stillalivelarp.utils.globalUnitTestPrint
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class DataAndLocalDataManagerTests: BaseUiTestClass {

    @Test
    fun testDataManager() = runTest {
        DataManager.shared.load(this) {
            globalUnitTestPrint("HERE BAYBEE")
            assertTrue(true)
            // TODO figure out if this is working
        }
    }

}