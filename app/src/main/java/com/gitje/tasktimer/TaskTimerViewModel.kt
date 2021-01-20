package com.gitje.tasktimer

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called. Uri: $uri")
            loadTasks()
        }
    }

    private val dataBaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = dataBaseCursor

    init {
        Log.d(TAG, "TaskTimerViewModel: Created")
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true, contentObserver
        )
        loadTasks()
    }

    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )
        GlobalScope.launch {
            //Order by sort order > Name
            val sortOrder =
                "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI, projection, null, null, sortOrder
            )
            dataBaseCursor.postValue(cursor)
        }
    }

    fun deleteTask(taskId: Long) {
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(
                TasksContract.buildUriFromId(
                    taskId
                ), null, null
            )
        }
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}