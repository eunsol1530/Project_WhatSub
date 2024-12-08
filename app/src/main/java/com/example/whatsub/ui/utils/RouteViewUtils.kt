package com.example.whatsub.ui.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.whatsub.R
import com.example.whatsub.data.api.model.TransferPath

// dp 변환 확장 함수
private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

private fun createStyledLabel(label: String): SpannableString {
    val spannable = SpannableString(label)
    val ampersandIndex = label.indexOf('&')

    if (ampersandIndex != -1) {
        // "최소 시간 & 최소 환승"에서 & 이후를 굵게 설정
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            ampersandIndex, label.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}

private fun getLineBackground(lineNumber: Int): Int {
    return when (lineNumber) {
        1 -> Color.parseColor("#00af50")
        2 -> Color.parseColor("#002060")
        3 -> Color.parseColor("#973b38")
        4 -> Color.parseColor("#ff0000")
        5 -> Color.parseColor("#4a7ebc")
        6 -> Color.parseColor("#ffc00c")
        7 -> Color.parseColor("#94d055")
        8 -> Color.parseColor("#00aff0")
        9 -> Color.parseColor("#70309f")
        else -> Color.GRAY
    }
}

object RouteViewUtils {
    fun createRouteView(context: Context, path: TransferPath, label: String, onFavoriteClicked: (TransferPath, Boolean) -> Unit
    ): View {
        // 최상위 컨테이너
        val routeView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120.dp
            ).apply {
                setMargins(0, 8.dp, 0, 4.dp)
            }
            setBackgroundColor(Color.parseColor("#ffffff"))
        }

        // 구분 선 추가
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.dp // 선의 두께
            ).apply {
                setMargins(0, 0, 0, 0)
            }
            setBackgroundColor(Color.LTGRAY) // 선의 색상
        }
        routeView.addView(divider)

        // 라벨과 즐겨찾기 버튼을 포함하는 컨테이너
        val headerContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL // 수직 중앙 정렬
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dp, 0, 10.dp, 0)
            }
        }

        // 라벨 (최소 시간/최소 비용/최소 환승)
        val labelTextView = TextView(context).apply {
            id = R.id.route_label // ID 설정
            //text = label
            text = createStyledLabel(label) // 수정된 함수로 스타일 적용
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1F
            )
        }
        headerContainer.addView(labelTextView)

        // 즐겨찾기 버튼
        val favoriteButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                40.dp,
                40.dp
            ).apply {
                setMargins(16.dp, 4.dp, 0, 0)
            }
            setImageResource(R.drawable.icon_favorites_fill)
            scaleType = ImageView.ScaleType.FIT_CENTER // 버튼 안에서 축소 및 중앙 배치
            setBackgroundColor(Color.TRANSPARENT) // 배경 투명

            var isFavorite = true // 즐겨찾기 여부 상태 변수

            setOnClickListener {
                isFavorite = !isFavorite
                setImageResource(
                    if (isFavorite) R.drawable.icon_favorites_blank else R.drawable.icon_favorites_fill
                )
                onFavoriteClicked(path, isFavorite)
            }
        }

        headerContainer.addView(favoriteButton)

        routeView.addView(headerContainer)


// 총 시간, 총 비용을 포함할 컨테이너
        val infoContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 총 시간 텍스트
        val totalTimeTextView = TextView(context).apply {
            text = "${path.totalTime}"
            textSize = 16f
            setTextColor(Color.DKGRAY) // 텍스트 색상 설정
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM // 하단 정렬
            }
            setPadding(16.dp, 0, 8.dp, 8.dp)
            setTypeface(null, Typeface.BOLD) // 폰트 두껍게 설정
        }
        infoContainer.addView(totalTimeTextView)

        // 총 비용 텍스트
        val totalCostTextView = TextView(context).apply {
            text = "${path.totalCost}"
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM // 하단 정렬
            }
            setPadding(0, 4.dp, 8.dp, 8.dp)
        }
        infoContainer.addView(totalCostTextView)

        // infoContainer를 routeView에 추가
        routeView.addView(infoContainer)


        // 경로 표시 영역
        val routeItemContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0) // 상하 여백
            }
        }

        fun String.extractMinutes(): Int {
            val regex = "(\\d+)시간".toRegex()
            val hours = regex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val minutesRegex = "(\\d+)분".toRegex()
            val minutes = minutesRegex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val secondsRegex = "(\\d+)초".toRegex()
            val seconds = secondsRegex.find(this)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            return (hours * 60) + minutes + (seconds / 60)
        }

        fun String.formatTimeWithoutSeconds(): String {
            val regex = "(\\d+시간)?\\s*(\\d+분)?".toRegex()
            val matchResult = regex.find(this)
            val hours = matchResult?.groupValues?.get(1)?.trim() ?: ""
            val minutes = matchResult?.groupValues?.get(2)?.trim() ?: ""

            return listOf(hours, minutes).filter { it.isNotEmpty() }.joinToString(" ")
        }


// 총 시간의 분 단위로 비율 계산
        val totalMinutes = path.segments.sumOf { it.timeOnLine.extractMinutes() }
        path.segments.forEachIndexed { index, segment ->
            // 출발 지점 추가
            if (index == 0) {
                val startStationView = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16.dp, 0, 0, 0) // 아이콘과 라인 간격 조정
                    }
                    setBackgroundColor(Color.WHITE) // 배경 설정
                    gravity = Gravity.CENTER
                }

                val startIcon = ImageView(context).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                startStationView.addView(startIcon)

                val startStationText = TextView(context).apply {
                    text = "${segment.fromStation}"
                    textSize = 8f // 텍스트 크기
                    setTextColor(Color.BLACK) // 텍스트 색상
                    setTypeface(null, Typeface.BOLD) // 텍스트 두껍게
                    gravity = Gravity.CENTER // 중앙 정렬
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                startStationText.elevation = 10f // 우선순위 높임

                Log.d("StationDebug", "startStationText=${startStationText.text}")

                startStationView.addView(startStationText)

                routeItemContainer.addView(startStationView)
                Log.d("RouteContainer", "Child count: ${routeItemContainer.childCount}")

            }

            // 각 구간 비율 및 역 정보 추가
            val segmentView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, // 비율에 따라 동적으로 설정
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    segment.timeOnLine.extractMinutes().toFloat() / totalMinutes // 가중치로 비율 반영
                ).apply {
                    setMargins(0, 8.dp, 0, 0)
                }
            }

            // 호선 정보
            val lineTextView = TextView(context).apply {
                text = "${segment.lineNumber}"
                textSize = 8f
                setTextColor(Color.WHITE)
                setBackgroundColor(getLineBackground(segment.lineNumber)) // 호선별 배경
                setPadding(4.dp, 0, 4.dp, 0)
                gravity = Gravity.CENTER
            }
            segmentView.addView(lineTextView)

            // 소요 시간 정보
            val timeTextView = TextView(context).apply {
                text = segment.timeOnLine.formatTimeWithoutSeconds() // 초 제거된 문자열 적용
                textSize = 10f
                gravity = Gravity.CENTER
            }
            segmentView.addView(timeTextView)

            routeItemContainer.addView(segmentView)

            // 환승 아이콘 및 역 이름 추가
            if (index < path.segments.size - 1) {
                val transferView = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 0)
                    }
                    gravity = Gravity.CENTER
                }

                val transferIcon = ImageView(context).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                transferView.addView(transferIcon)

                val transferText = TextView(context).apply {
                    text = if (segment.toStation != 0) "${segment.toStation}" else "환승역 미지정"
                    textSize = 8f
                    setTextColor(Color.BLACK) // 텍스트 색상 설정
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }

                Log.d("StationDebug", "transferText=${transferText.text}")

                transferView.addView(transferText)

                routeItemContainer.addView(transferView)
            }

            // 도착 지점 추가
            if (index == path.segments.size - 1) {
                val endStationView = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        setMargins(0, 0, 16.dp, 0)
                    }
                    gravity = Gravity.CENTER
                }

                val endIcon = ImageView(context).apply {
                    setImageResource(R.drawable.icon_transfer) // 환승 아이콘
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        31.dp,
                        31.dp
                    )
                }
                endStationView.addView(endIcon)

                val endStationText = TextView(context).apply {
                    text = if (segment.toStation != 0) "${segment.toStation}" else "도착역 미지정"
                    textSize = 8f
                    setTextColor(Color.BLACK) // 텍스트 색상 설정
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }
                Log.d("StationDebug", "endStationText=${endStationText.text}")
                endStationView.addView(endStationText)

                routeItemContainer.addView(endStationView)

                routeView.addView(routeItemContainer)
            }
        }

        // 경로 데이터를 태그로 저장
        routeView.tag = path

        return routeView
    }
}