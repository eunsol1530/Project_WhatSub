package com.example.whatsub.model

data class TransferPath(
    val totalTime: String,
    val totalCost: String,
    val segments: List<Transfer>
)

data class Transfer(
    val fromStation: Int,
    val toStation: Int,
    val lineNumber: Int,
    val timeOnLine: String,
    val costOnLine: String,
    val toiletCount: Int,
    val storeCount: Int
)

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
