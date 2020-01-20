package com.example.pagergalleryloadmorepart2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlin.math.ceil

const val DATA_STATUS_CAN_LOAD_MORE = 0
const val DATA_STATUS_NO_MORE = 1
const val DATA_STATUS_NETWORK_ERROR = 2
class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val _dataStatusLive = MutableLiveData<Int>()
    val dataStatusLive:LiveData<Int> get() = _dataStatusLive
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive

    var needToScrollToTop = true
    private val perPage = 100
    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal")
    private var currentPage = 1
    private var totalPage = 1
    private var currentKey = "cat"
    private var isNewQuery = true
    private var isLoading = false
    init {
        resetQuery()
    }

    fun resetQuery() {
        currentPage = 1
        totalPage = 1
        currentKey = keyWords.random()
        isNewQuery = true
        needToScrollToTop = true
        fetchData()
    }

    fun fetchData() {
        if (isLoading) return
        if (currentPage > totalPage) {
            _dataStatusLive.value = DATA_STATUS_NO_MORE
            return
        }
        isLoading = true
        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            Response.Listener {
                with(Gson().fromJson(it, Pixabay::class.java)) {
                    totalPage = ceil(totalHits.toDouble() / perPage).toInt()
                    if (isNewQuery) {
                        _photoListLive.value = hits.toList()
                    } else {
                        _photoListLive.value =
                            arrayListOf(_photoListLive.value!!, hits.toList()).flatten()
                    }
                }
                _dataStatusLive.value = DATA_STATUS_CAN_LOAD_MORE
                isLoading = false
                isNewQuery = false
                currentPage++
            },
            Response.ErrorListener {
                _dataStatusLive.value = DATA_STATUS_NETWORK_ERROR
                isLoading = false
            }
        )
        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getUrl(): String {
        return "https://pixabay.com/api/?key=12472743-874dc01dadd26dc44e0801d61&q=${currentKey}&per_page=${perPage}&page=${currentPage}"
    }

}












