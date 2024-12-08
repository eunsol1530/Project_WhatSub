package com.example.whatsub.ui.news

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsub.R
import com.example.whatsub.databinding.FragmentNewsBinding
import com.example.whatsub.ui.newsDetail.NewsDetailFragment

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val newsList = listOf(
        News("비싼 월세와 고물가에 지갑 닫은 '1인 가구'... \"소비 회복 제약\"", "Economy", "https://n.news.naver.com/mnews/article/469/0000836610"),
        News("‘전원 재계약’ (여자)아이들 민니, 데뷔 첫 솔로 데뷔..“1월 발매 목표” [공식입장]", "Entertainment", "https://m.entertain.naver.com/article/109/0005206649"),
        News("통영 내리막길서 15톤 덤프트럭과 승용차 '쾅'…14중 추돌 사고", "Society", "https://n.news.naver.com/article/031/0000890030"),
        News("'충격의 대반전' SON에 손뗐다던 바르셀로나, 사실 손흥민 영입에 진심...파티+토레스 정리해 연봉 마련 계획", "Sports", "https://m.sports.naver.com/wfootball/article/076/0004222656"),
        News("'탐욕스러운 노이어, 쓸데없이 또 튀어나왔다'…19년 만의 첫 퇴장, 볼 대신 상대 선수 가격", "Sports", "https://m.sports.naver.com/wfootball/article/117/0003893679"),
        News("[단독] 韓 '양자인터넷' 시동…\"내년초 100㎞ 전송시연\"", "Technology", "https://n.news.naver.com/mnews/article/011/0004422632"),
        News("현대차·기아, 11월 美판매 2개월 연속 두자릿수 증가(종합)", "Economy", "https://n.news.naver.com/mnews/article/001/0015084009"),
        News("ETRI, 노코드 신경망 자동생성 프레임워크 공개...세미나 사전신청만 500명 넘어", "Technology", "https://n.news.naver.com/mnews/article/092/0002355064"),
        News("AWS, 공유 정책 폐지…MSP \"멀티클라우드 기조 견제\"", "Technology", "https://n.news.naver.com/mnews/article/092/0002355060"),
        News("유네스코, '한국 장 담그기 문화' 인류무형문화유산 등재", "Society", "https://n.news.naver.com/mnews/article/008/0005123173"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)

        // RecyclerView 설정
        val newsAdapter = NewsAdapter(newsList) { news ->
            //뉴스 클릭 시 뉴스 URL 전달
            val intent = Intent(context, NewsDetailFragment::class.java).apply {
                putExtra("newsTitle", news.title)
                putExtra("newsUrl", news.url) // 여기서 URL을 전달
            }
            startActivity(intent)
        }

        val spaceItemDecoration = SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.item_spacing))
        binding.recyclerView.addItemDecoration(spaceItemDecoration)

        //RecyclerView에 Adapter 연결
        binding.recyclerView.adapter = newsAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NewsAdapter(newsList) { news ->
            val bundle = Bundle().apply {
                putString("newsUrl", news.url)
            }
            findNavController().navigate(R.id.action_newsFragment_to_newsDetailFragment, bundle)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 구분선 추가
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        // 간격 추가
        val spaceItemDecoration = SpaceItemDecoration(10) // 간격 설정 (픽셀 단위)
        binding.recyclerView.addItemDecoration(spaceItemDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}