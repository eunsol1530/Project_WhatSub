package com.example.whatsub.model

data class Transfer(
    val fromStation: Int,
    val toStation: Int,
    val lineNumber: Int,
    val timeOnLine: String,
    val costOnLine: String
)

data class PathData(
    val startStation: Int,
    val endStation: Int,
    val totalTime: String,
    val totalCost: String,
    val transfers: List<Transfer>
)