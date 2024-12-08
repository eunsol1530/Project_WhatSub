package com.example.whatsub.data.api.model

import java.io.Serializable

data class TransferPath(
    val totalTime: String,
    val totalCost: String,
    val segments: List<Transfer>
) : Serializable

data class Transfer(
    val fromStation: Int = 0,
    val toStation: Int = 0,
    val lineNumber: Int,
    val timeOnLine: String,
    val costOnLine: String,
    val toiletCount: Int,
    val storeCount: Int
) : Serializable

data class ShortestPath(
    val startStation: Int,
    val endStation: Int,
    val totalTime: String,
    val totalCost: String,
    val transfers: List<Transfer>
)

data class CheapestPath(
    val startStation: Int,
    val endStation: Int,
    val totalTime: String,
    val totalCost: String,
    val transfers: List<Transfer>
)

data class Path(
    val startStation: Int,
    val endStation: Int,
    val totalTransfers: Int? = null,
    val paths: List<TransferPath>
)


data class PathData(
    val shortestPath: Path?,
    val cheapestPath: Path?,
    val leastTransfersPath: Path?,
    val comparisonResults: List<Int>? = null// 기존의 comparisonResult를 대체
) : Serializable
