package com.gitje.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
//ID is var because it gets assigned after being put into the DB
data class Task(val name: String, val description: String, val sortOrder: Int, var id: Long = 0): Parcelable {

}