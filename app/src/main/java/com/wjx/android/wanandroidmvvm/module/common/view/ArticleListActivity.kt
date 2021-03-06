package com.wjx.android.wanandroidmvvm.module.common.view

import android.graphics.Color
import androidx.lifecycle.Observer
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.module.common.model.Article
import com.wjx.android.wanandroidmvvm.module.common.viewmodel.ArticleViewModel
import com.wjx.android.wanandroidmvvm.base.view.BaseLifeCycleActivity
import com.wjx.android.wanandroidmvvm.module.common.adapter.ArticleAdapter
import com.wjx.android.wanandroidmvvm.common.state.UserInfo
import com.wjx.android.wanandroidmvvm.common.state.callback.CollectListener
import com.wjx.android.wanandroidmvvm.common.utils.*
import kotlinx.android.synthetic.main.fragment_article_list.*
import org.greenrobot.eventbus.Subscribe

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @date: 2020/03/01
 * Time: 14:00
 */
abstract class ArticleListActivity<VM : ArticleViewModel<*>> : BaseLifeCycleActivity<VM>(),
    CollectListener {
    private var mCollectState: Boolean = false

    private var mCurrentItem: Int = 0

    protected lateinit var mAdapter: ArticleAdapter

    protected var isLoadMore: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_article_list

    override fun initView() {
        super.initView()
        initRefresh()
        mRvArticle?.layoutManager = SpeedLayoutManager(this, 10f)
        mAdapter =
            ArticleAdapter(
                R.layout.article_item,
                null
            )
        mRvArticle?.adapter = mAdapter

        mAdapter.setOnItemClickListener { _, _, position ->
            val article = mAdapter.getItem(position)
            article?.let {
                mViewModel.addFootPrint(article)
                CommonUtil.startWebView(this, it.link, it.title)
            }
        }

        mAdapter.setOnItemChildClickListener { _, _, position ->
            UserInfo.instance.collect(this, position, this)
        }
        mAdapter.setEnableLoadMore(isLoadMore)
        mAdapter.setOnLoadMoreListener({ onLoadMoreData() }, mRvArticle)
    }

    private fun initRefresh() {
        // ?????????????????????loading??????
        mSrlRefresh.setProgressBackgroundColorSchemeColor(ColorUtil.getColor(this))
        mSrlRefresh.setColorSchemeColors(Color.WHITE)
        mSrlRefresh.setOnRefreshListener { onRefreshData() }
    }

    /**
     * ????????????
     */
    abstract fun onRefreshData()

    /**
     * ??????????????????
     */
    abstract fun onLoadMoreData()

    fun addData(articleList: List<Article>) {

        // ????????????????????????????????????
        if (articleList.isEmpty()) {
            mAdapter.loadMoreEnd()
            return
        }

        // ????????????????????????????????????????????????
        if (mSrlRefresh.isRefreshing) {
            mSrlRefresh.isRefreshing = false
            mAdapter.setNewData(articleList)
            mAdapter.loadMoreComplete()
            return
        }

        // ?????????????????????????????????
        mAdapter.addData(articleList)
        mAdapter.loadMoreComplete()
    }

    override fun initDataObserver() {
        mViewModel.mCollectData.observe(this, Observer {
            var article = mAdapter.getItem(mCurrentItem)
            article?.let {
                it.collect = !mCollectState
                mAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun collect(position: Int) {
        var article = mAdapter.getItem(position)

        article?.let {
            mCurrentItem = position
            mCollectState = it.collect
            if (mCollectState) mViewModel.unCollectCo(it.id) else mViewModel.collectCo(it.id)
        }
    }

    override fun reLoad() {
        showLoading()
        onRefreshData()
        super.reLoad()
    }

    @Subscribe
    open fun settingEvent(event: ChangeThemeEvent) {
        mSrlRefresh.setProgressBackgroundColorSchemeColor(ColorUtil.getColor(this))
        mAdapter.notifyDataSetChanged()
    }
}