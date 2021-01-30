package com.gitje.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked,
    MainFragment.OnTaskEdit, AppDialog.DialogEvents {
    //Whether or not the activity is in two-pane mode. Landscape / portrait
    private var mTwoPane = false

    private var aboutDialog: AlertDialog? = null

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        //Get device orientation, landscape = true, portrait = false
        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        //get fragments and set up the panes correctly
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            showEditPane()
        } else {
            task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun showEditPane() {
        //Right-hand fragment exists
        task_details_container.visibility = View.VISIBLE
        //Hide left-hand pane when in portrait
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEdit called")
        if (fragment != null) {
            /*supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()*/
            removeFragment(fragment)
        }

        //Set the visibility of the right-hand pane
        task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        //And show left-hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        removeEditPane(findFragmentById(R.id.task_details_container))
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
            R.id.menuMain_ShowAbout -> showAboutDialog()
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Home button tapped")
                val fragment = findFragmentById(R.id.task_details_container)

                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDiag_message),
                        R.string.cancelEditDiag_positiveCaption,
                        R.string.cancelEditDiag_negativeCaption
                    )
                } else removeEditPane(fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        //Calling these before the .create on line 122 is crucial
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            Log.d(TAG, "onClick: Entering messageView.onClick")
            if (aboutDialog?.isShowing == true)
                aboutDialog?.dismiss()
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true) //True is default, but since we have no cancel button, it's better to make sure

        /*Not ideal for dialogs where text is linked ( mail, websites, ... )
        messageView.setOnClickListener {
            Log.d(TAG, "Entering messageView.onClick")
            if(aboutDialog != null && aboutDialog?.isShowing == true)
                aboutDialog?.dismiss()
        }*/

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        aboutDialog?.show()
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        showEditPane()
        replaceFragment(AddEditFragment.newInstance(task), R.id.task_details_container)

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane)
            super.onBackPressed()
        else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positiveCaption,
                    R.string.cancelEditDiag_negativeCaption
                )
            } else removeEditPane(fragment)
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
        }
    }

    override fun onStop() {
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }

    override fun onNegativeDialogResult(dialogId: Int, args: Bundle) {

    }

    override fun onDialogCancelled(dialogId: Int) {

    }
}