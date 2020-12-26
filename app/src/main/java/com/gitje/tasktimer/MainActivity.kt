package com.gitje.tasktimer

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        //testInsert()
        //testUpdate()
        //testUpdateBulk()
        //testDelete()
        //testDeleteBulk()

        val projection =
            arrayOf(TasksContract.Columns.TASK_NAME, TasksContract.Columns.TASK_SORT_ORDER)
        val sortColumn = TasksContract.Columns.TASK_SORT_ORDER

        //val cursor = contentResolver.query(TasksContract.CONTENT_URI, projection, null, null, sortColumn)
        val cursor = contentResolver.query(TasksContract.CONTENT_URI, null, null, null, sortColumn)
        Log.d(TAG, "***************************")
        cursor.use {
            while (it?.moveToNext()!!) {
                //cycle through records
                with(cursor) {
                    val id = this?.getLong(0)
                    val name = this?.getString(1)
                    val description = this?.getString(2)
                    val sortOrder = this?.getString(3)
                    val result =
                        "ID: $id | Name: $name | Description: $description | Sort order: $sortOrder |"
                    Log.d(TAG, "onCreate: reading data $result")
                }
            }
        }

        Log.d(TAG, "***************************")

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun testDeleteBulk() {
        val selection = TasksContract.Columns.TASK_SORT_ORDER + " = ?"
        val selectionArgs = arrayOf("5")

        val rowsAffected = contentResolver.delete(TasksContract.CONTENT_URI, selection, selectionArgs)
        Log.d(TAG, "Rows deleted: $rowsAffected")
    }

    private fun testDelete() {
        val taskUri = TasksContract.buildUriFromId(2)
        val rowsAffected = contentResolver.delete(taskUri, null, null)
        Log.d(TAG, "Rows deleted: $rowsAffected")
    }

    private fun testUpdateBulk() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_SORT_ORDER, "5")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Testing")
        }
        val selection = TasksContract.Columns.TASK_SORT_ORDER + " = ?"
        val selectionArgs = arrayOf("1")

        //val taskUri = TasksContract.buildUriFromId(4)
        val rowsAffected = contentResolver.update(TasksContract.CONTENT_URI, values, selection, selectionArgs)
        Log.d(TAG, "Rows updated: $rowsAffected")
    }

    private fun testUpdate() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "Content Provider")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Testing Update")
        }

        val taskUri = TasksContract.buildUriFromId(4)
        val rowsAffected = contentResolver.update(taskUri, values, null, null)
        Log.d(TAG, "Rows updated: $rowsAffected")
    }

    private fun testInsert() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "New Task 1")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Description 1")
            put(TasksContract.Columns.TASK_SORT_ORDER, "2")
        }

        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
        Log.d(TAG, "New row id ( in uri ) is $uri")
        Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri!!)}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}