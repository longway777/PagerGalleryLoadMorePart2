package com.example.pagergalleryloadmorepart2


import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {
    private lateinit var galleryViewModel: GalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.swipeIndicator -> {
                swipeLayoutGallery.isRefreshing = true
                Handler().postDelayed(Runnable {galleryViewModel.resetQuery() },1000)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        galleryViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(GalleryViewModel::class.java)
        val galleryAdapter = GalleryAdapter(galleryViewModel)
        recyclerView.apply {
            adapter = galleryAdapter
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        }


        galleryViewModel.photoListLive.observe(this, Observer {
            if (galleryViewModel.needToScrollToTop) {
                recyclerView.scrollToPosition(0)
                galleryViewModel.needToScrollToTop = false
            }
            galleryAdapter.submitList(it)
            swipeLayoutGallery.isRefreshing = false
        })

        galleryViewModel.dataStatusLive.observe(this, Observer {
            galleryAdapter.footerViewStatus = it
            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount - 1)
            if (it == DATA_STATUS_NETWORK_ERROR) swipeLayoutGallery.isRefreshing = false
        })

        swipeLayoutGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }

        recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0 ) return
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val intArray = IntArray(2)
                layoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount - 1) {
                    galleryViewModel.fetchData()
                }
            }
        })


    }

}















