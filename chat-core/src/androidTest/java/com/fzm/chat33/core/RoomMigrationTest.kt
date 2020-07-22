package com.fzm.chat33.core

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.fzm.chat33.core.db.ChatDatabase

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {

    @Rule
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            ChatDatabase::class.java.canonicalName!!,
            FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun migrate_13_14() {
        helper.createDatabase(TEST_DB, 13).apply {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL("")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 14, true,
                ChatDatabase.MIGRATION_13_14)
    }

    companion object {

        private val TEST_DB = "migration-test"
    }
}
