package com.example.whatsub.ui.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.data.api.model.TransferPath

class FavoritesAdapter(
    private val favoritesList: MutableList<TransferPath>, // 즐겨찾기 데이터를 MutableList로 사용
    private val onFavoriteRemoved: (TransferPath) -> Unit // 삭제 콜백 함수
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorites, parent, false) // item_favorite 레이아웃 파일
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favoritesList[position]
        holder.bind(favorite)
    }

    override fun getItemCount(): Int = favoritesList.size

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalTimeTextView: TextView = itemView.findViewById(R.id.totalTimeTextView)
        private val totalCostTextView: TextView = itemView.findViewById(R.id.totalCostTextView)
        private val segmentsContainer: LinearLayout = itemView.findViewById(R.id.segmentsContainer)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

        fun bind(transferPath: TransferPath) {
            totalTimeTextView.text = transferPath.totalTime
            totalCostTextView.text = transferPath.totalCost

            // Segment 데이터 동적 추가
            segmentsContainer.removeAllViews()
            transferPath.segments.forEach { segment ->
                val segmentView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.segment_item, segmentsContainer, false)
                val stationTextView: TextView = segmentView.findViewById(R.id.fromStation)
                stationTextView.text = "${segment.fromStation} -> ${segment.toStation}"
                segmentsContainer.addView(segmentView)
            }

            // 즐겨찾기 버튼 클릭 이벤트
            favoriteButton.setOnClickListener {
                onFavoriteRemoved(transferPath) // 콜백 호출
                favoritesList.remove(transferPath) // 목록에서 제거
                notifyItemRemoved(adapterPosition) // UI 업데이트
            }
        }
    }
}
