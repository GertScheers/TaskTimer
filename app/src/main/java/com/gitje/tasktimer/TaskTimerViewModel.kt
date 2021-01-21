package com.gitje.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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

    fun saveTask(task: Task) : Task {
        val values = ContentValues()
        if(task.name.isNotEmpty()) {
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if(task.id == 0L) {
                //Non-existing task, make new entry
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: Creating new entry")
                    val uri = getApplication<Application>().contentResolver?.insert(TasksContract.CONTENT_URI, values)
                    if(uri != null) {
                        task.id = TasksContract.getId(uri)
                            Log.d(TAG, "saveTask: new id is ${task.id}")
                    }
                }
            } else {
                //Modify existing task
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: Modifying existing task")
                    getApplication<Application>().contentResolver?.update(TasksContract.buildUriFromId(task.id), values, null, null)
                }
            }
        }
        return task
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