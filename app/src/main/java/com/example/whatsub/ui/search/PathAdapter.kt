package com.example.whatsub.ui.search

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.R.layout.item_path
import com.example.whatsub.model.PathData

class PathAdapter(private val paths: List<PathData>) : RecyclerView.Adapter<PathAdapter.PathViewHolder>() {

    fun updateData(newPaths: List<PathData>) {
        (this.paths as MutableList).clear()
        (this.paths as MutableList).addAll(newPaths)
        notifyDataSetChanged() // UI 갱신
    }

    class PathViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val totalTime: TextView = view.findViewById(R.id.search_total_time)
        val totalCost: TextView = view.findViewById(R.id.search_total_cost)
        val transfers: TextView = view.findViewById(R.id.search_transfers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(item_path, parent, false)
        return PathViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
        val path = paths[position]
        holder.totalTime.text = "총 시간: ${path.totalTime}"
        holder.totalCost.text = "총 비용: ${path.totalCost}"
        holder.transfers.text = "환승 정보: ${path.transfers.size}개"

        // visibility를 VISIBLE로 강제 설정
        holder.totalTime.visibility = View.VISIBLE
        holder.totalCost.visibility = View.VISIBLE
        holder.transfers.visibility = View.VISIBLE

        if (holder.totalTime.visibility != View.VISIBLE) {
            Log.w("PathAdapter", "TextView is not visible at position $position")
            holder.totalTime.visibility = View.VISIBLE // 강제로 보이도록 설정
        }

        Log.d("PathAdapter", "Binding item at position $position: $path")

        // 추가 디버깅
        Log.d("PathAdapter", "Binding position $position - Total Time: ${holder.totalTime.text}")
        Log.d("PathAdapter", "Total Time View visibility: ${holder.totalTime.visibility}")

        // 환승 정보 요약
        val transferInfo = path.transfers.joinToString(" -> ") {
            "(${it.fromStation} → ${it.toStation}, ${it.timeOnLine}, ${it.costOnLine})"
        }
        holder.transfers.text = "환승: $transferInfo"
    }

    override fun getItemCount(): Int {
        Log.d("PathAdapter", "Item Count: ${paths.size}")
        return paths.size
    }
}
