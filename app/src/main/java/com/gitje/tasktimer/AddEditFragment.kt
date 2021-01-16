package com.gitje.tasktimer

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "AddEditFragment"

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "OnCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "OnCreateView: starts")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        if (savedInstanceState == null) {
            //To work around smart-cast error because mutable
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                addEdit_name.setText(task.name)
                addEdit_description.setText(task.description)
                addEdit_sortOrder.setText(task.sortOrder.toString())
            } else {
                //No task, so the template stays empty
                Log.d(TAG, "onViewCreated: No arguments, adding new record")
            }
        }
    }

    private fun saveTask() {
        //Update the database IF a change has been made
        val sortOrder = if (addEdit_sortOrder.text.isNotEmpty())
            Integer.parseInt(addEdit_sortOrder.text.toString())
        else 0
        val values = ContentValues()
        val task = task

        if(task!=null) {
            Log.d(TAG, "saveTask: updating existing task")
            if(addEdit_name.text.toString() != task.name){
                values.put(TasksContract.Columns.TASK_NAME, addEdit_name.text.toString())
            }
            if(addEdit_description.text.toString() != task.description){
                values.put(TasksContract.Columns.TASK_DESCRIPTION, addEdit_description.text.toString())
            }
            if(sortOrder != task.sortOrder){
                values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
            }

            if(values.size() != 0) {
                Log.d(TAG, "saveTask: Updating task")
                activity?.contentResolver?.update(TasksContract.buildUriFromId(task.id),values, null,null)
            }
        } else {
            //new record
            Log.d(TAG, "saveTask: saving new task")
            if(addEdit_name.text.isNotEmpty())
                values.put(TasksContract.Columns.TASK_NAME, addEdit_name.text.toString())
            if(addEdit_description.text.isNotEmpty())
                values.put(TasksContract.Columns.TASK_DESCRIPTION, addEdit_description.text.toString())
            values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
            activity?.contentResolver?.insert(TasksContract.CONTENT_URI, values)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)

        //Home/back button for the fragment. Commented out parts is for when you want to change multiple things
        //val listener = listener
        if (listener is AppCompatActivity) {
            //val actionBar = listener.supportActionBar
            val actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addEdit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited, or null to add a new task.
         * @return A new instance of fragment BlankFragment.
         */
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }
}