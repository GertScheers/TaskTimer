package com.gitje.tasktimer

import android.content.ContentValues
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked {
    //Whether or not the activity is in two-pane mode. Landscape / portrait
    private var mTwoPane = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        //Get device orientation, landscape = true, portrait = false
        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        //get fragments and set up the panes correctly
        var fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if(fragment != null) {
            showEditPane()
        } else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun showEditPane(){
        //Right-hand fragment exists
        task_details_container.visibility = View.VISIBLE
        //Hide left-hand pane when in portrait
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEdit called")
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        //Set the visibility of the right-hand pane
        task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        //And show left-hand pane
        mainFragment.view?.visibility = View.VISIBLE
    }

    override fun onSaveClicked(){
        Log.d(TAG, "onSaveClicked: called")
        var fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
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
        when (item.itemId) {
            R.id.menuMain_AddTask -> taskEditRequest(null)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        //Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }
}