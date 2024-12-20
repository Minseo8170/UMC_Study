package com.example.flo.ui.main.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.flo.AlbumRVAdapter
import com.example.flo.BannerFragment
import com.example.flo.BannerVPAdapter
import com.example.flo.PannelFragment
import com.example.flo.PannelVPAdapter
import com.example.flo.R
import com.example.flo.SongDatabase
import com.example.flo.data.entities.Album
import com.example.flo.databinding.FragmentHomeBinding
import com.example.flo.ui.main.MainActivity
import com.example.flo.ui.main.album.AlbumFragment
import com.google.gson.Gson
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class HomeFragment : Fragment() {
    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    lateinit var binding: FragmentHomeBinding
    private var albumDatas = ArrayList<Album>()
    private lateinit var songDB: SongDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        songDB = SongDatabase.getInstance(requireContext())!!
        albumDatas.addAll(songDB.albumDao().getAlbums())
        Log.d("albumlist", albumDatas.toString())

        val albumRVAdapter = AlbumRVAdapter(albumDatas)
        binding.homeTodayMusicAlbumRv.adapter = albumRVAdapter

        albumRVAdapter.setMyItemClickListener(object: AlbumRVAdapter.MyItemClickListener {
            override fun onItemClick(album: Album) {
                changeAlbumFragment(album)
            }

            override fun onIVClick(album: Album) {
                changeMainSong(album)
            }

            override fun onRemoveAlbum(position: Int) {
                albumRVAdapter.removeItem(position)
            }
        })
        binding.homeTodayMusicAlbumRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

//        binding.homeAlbumImgIv1.setOnClickListener {
//            (context as MainActivity).supportFragmentManager.beginTransaction()
//                .replace(R.id.main_frm,AlbumFragment())
//                .commitAllowingStateLoss()
//        }

        val bannerAdapter = BannerVPAdapter(this)
        bannerAdapter.addFragment(BannerFragment(R.drawable.img_home_viewpager_exp))
        bannerAdapter.addFragment(BannerFragment(R.drawable.img_home_viewpager_exp2))
        binding.homeBannerVp.adapter = bannerAdapter
        binding.homeBannerVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        //ViewPager가 좌우로 스크롤 될 수 있게 해줌

        val pannelAdapter = PannelVPAdapter(this)
        pannelAdapter.addFragment(PannelFragment(R.layout.fragment_pannel))
        pannelAdapter.addFragment(PannelFragment(R.layout.fragment_pannel))
        binding.homePannelVp.adapter = pannelAdapter
        binding.homePannelVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.homeIndicator.setViewPager(binding.homePannelVp)

        startAutoSlide(pannelAdapter)


        return binding.root
    }

    private fun changeAlbumFragment(album: Album) {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, AlbumFragment().apply {
                arguments = Bundle().apply {
                    val gson = Gson()
                    val albumJson = gson.toJson(album)
                    putString("album", albumJson)
                }
            })
            .commitAllowingStateLoss()
    }

    private fun changeMainSong(album: Album) {
//        val gson = Gson()
//        val albumJson = gson.toJson(album)
//        (context as MainActivity).receiveAlbumData(albumJson)
    }

    private fun startAutoSlide(adapter: PannelVPAdapter) {
        timer.scheduleAtFixedRate(3000, 3000) {
            handler.post {
                val nextItem = binding.homePannelVp.currentItem+1
                if(nextItem < adapter.itemCount) {
                    binding.homePannelVp.currentItem = nextItem
                }
                else {
                    binding.homePannelVp.currentItem = 0
                }
            }
        }
    }
}

