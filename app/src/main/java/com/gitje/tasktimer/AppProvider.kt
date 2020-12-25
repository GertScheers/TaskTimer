package com.gitje.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext

/**
Provider for the TaskTimer app. This is the only class that knows about [AppDatabase]
 */

private const val TAG = "AppProvider"

const val CONTENT_AUTHORITY = "com.gitje.tasktimer.provider"

private const val TASKS = 100
private const val TASKS_ID = 101

private const val TIMINGS = 200
private const val TIMINGS_ID = 201

private const val TASK_DURATIONS = 400
private const val TASK_DURATIONS_ID = 401

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")

        val matcher = UriMatcher(android.content.UriMatcher.NO_MATCH)

        //Example: content://com.gitje.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        //Example: content://com.gitje.tasktimer.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)

        //Example: content://com.gitje.tasktimer.provider/Timings
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
        //Example: content://com.gitje.tasktimer.provider/Timings/8
        matcher.addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)

/*
        //Example: content://com.gitje.tasktimer.provider/Durations
        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS)
        //Example: content://com.gitje.tasktimer.provider/Durations/8
        matcher.addURI(CONTENT_AUTHORITY, "${DurationsContract.TABLE_NAME}/#", TASK_DURATIONS_ID)
*/
        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "OnCreate starts")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "Query: query called with uri: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "Query: match is $match")
        val queryBuilder = SQLiteQueryBuilder()

        val context = requireContext(this)

        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }

            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME

            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val timingId = TimingsContract.getId(uri)
                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$timingId")
            }
/*
            TASK_DURATIONS -> queryBuilder.tables = DurationsContract.TABLE_NAME

            TASK_DURATIONS_ID -> {
                queryBuilder.tables = DurationsContract.TABLE_NAME
                val durationId = DurationsContract.getId(uri)
                queryBuilder.appendWhere("${DurationsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$durationId")

            }
*/
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query: rows in returned cursor = ${cursor.count}") // TODO remove this line

        return cursor
    }

    override fun getType(uri: Uri): String? {
        val match = uriMatcher.match(uri)

        return when (match) {
            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE
            TIMINGS -> TimingsContract.CONTENT_TYPE
            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE
            //TASK_DURATIONS -> DurationsContract.CONTENT_TYPE
            //TASK_DURATIONS_ID -> DurationsContract.CONTENT_ITEM_TYPE

            else -> throw java.lang.IllegalArgumentException("unknown uri: $uri")
        }

    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "Query: query called with uri: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "Query: match is $match")

        val recordId: Long
        val returnUri: Uri
        val context = requireContext(this)

        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                //"L" is needed to compare int to Long, since recordId = Long type
                if (recordId != -1L)
                    returnUri = TasksContract.buildUriFromId(recordId)
                else
                    throw SQLException("Failed to insert, Uri was: $uri")
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L)
                    returnUri = TimingsContract.buildUriFromId(recordId)
                else
                    throw SQLException("Failed to insert, Uri was: $uri")
            }

            else -> throw java.lang.IllegalArgumentException("unknown Uri: $uri")
        }

        Log.d(TAG, "Exiting Insert, returning $returnUri")
        return returnUri
    }

    override fun delete(uri: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        p2: String?,
        p3: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }
}