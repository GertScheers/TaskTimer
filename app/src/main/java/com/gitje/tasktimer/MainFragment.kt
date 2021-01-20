package com.gitje.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.RuntimeException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private const val TAG = "MainFragment"

class MainFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener {
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
        (activity as OnTaskEdit)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        viewModel.deleteTask(task.id)
    }

    override fun onTaskLongClick(task: Task) {
        TODO("Not yet implemented")
    }
}