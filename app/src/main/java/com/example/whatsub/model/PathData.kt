package com.example.whatsub.model

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

data class LeastTransfersPath(
    val totalTransfers: Int,
    val paths: List<TransferPath>
)


data class PathData(
    val shortestPath: ShortestPath?,
    val cheapestPath: CheapestPath?,
    val leastTransfersPath: LeastTransfersPath?
)
