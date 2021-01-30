package com.gitje.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.AssertionError
import java.lang.RuntimeException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private const val TAG = "MainFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"

class MainFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener,
                     AppDialog.DialogEvents {
    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    private val viewModel by lazy { ViewModelProvider(this).get(TaskTimerViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)
        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, { cursor -> mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context !is OnTaskEdit)
            throw RuntimeException("${context.toString()} must implement OnTaskEdit")
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit).onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldiag_message, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        TODO("Not yet implemented")
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with $dialogId")
        if(dialogId == DIALOG_ID_DELETE) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if(BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteTask(taskId)
        }
    }

    override fun onNegativeDialogResult(dialogId: Int, args: Bundle) {

    }

    override fun onDialogCancelled(dialogId: Int) {

    }
}