package com.wjx.android.wanandroidmvvm.module.todo.view

import android.app.Dialog
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.base.view.BaseLifeCycleActivity
import com.wjx.android.wanandroidmvvm.common.state.UserInfo
import com.wjx.android.wanandroidmvvm.common.utils.*
import com.wjx.android.wanandroidmvvm.module.todo.adapter.TodoAdapter
import com.wjx.android.wanandroidmvvm.module.todo.model.TodoResponse
import com.wjx.android.wanandroidmvvm.module.todo.viewmodel.TodoViewModel
import kotlinx.android.synthetic.main.custom_bar.view.*
import kotlinx.android.synthetic.main.fragment_article_list.*
import org.greenrobot.eventbus.Subscribe

class TodoActivity : BaseLifeCycleActivity<TodoViewModel>() {

    private lateinit var mAdapter: TodoAdapter

    private var mCurrentPageNum: Int = 1

    private lateinit var headerView: View

    override fun initDataObserver() {
        mViewModel.mTodoListData.observe(this, Observer { response ->
            response?.let {
                setTodoList(it.data.datas)
            }
        })
    }

    override fun initData() {
        mViewModel.loadTodoList(mCurrentPageNum)
    }

    override fun showDestroyReveal(): Boolean = true

    override fun initView() {
        super.initView()
        mAdapter = TodoAdapter(R.layout.todo_item, null)
        initHeadView()
        initRefresh()
        mRvArticle?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mRvArticle?.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
        mAdapter.setOnLoadMoreListener(
            { mViewModel.loadTodoList(++mCurrentPageNum) },
            mRvArticle
        )
        mAdapter.setOnItemClickListener { adapter, view, position ->
            var todo = mAdapter.getItem(position)
            startActivity<EditTodoActivity>(this) {
                putExtra(Constant.KEY_TODO_HANDLE_TYPE, Constant.EDIT_TODO)
                putExtra(Constant.KEY_TODO_TITLE, todo!!.title)
                putExtra(Constant.KEY_TODO_CONTENT, todo!!.content)
                putExtra(Constant.KEY_TODO_DATE, todo!!.dateStr)
                putExtra(Constant.KEY_TODO_PRIORITY, todo!!.priority.toString() + "")
                putExtra(Constant.KEY_TODO_ID, todo!!.id.toString() + "")
                putExtra(Constant.KEY_TODO_TYPE, todo!!.type.toString() + "")
            }
        }

        mAdapter.setOnItemChildClickListener { _, _, position ->
            // ?????????????????????
            val bottomDialog =
                Dialog(this, R.style.BottomDialog)
            val contentView =
                LayoutInflater.from(this).inflate(R.layout.dialog_handle_todo, null)
            bottomDialog.setContentView(contentView)
            val params = contentView.layoutParams as MarginLayoutParams
            params.width =
                getResources().getDisplayMetrics().widthPixels - DisplayUtil.dp2Px(this, 16)
            params.bottomMargin = DisplayUtil.dp2Px(this, 8)
            contentView.layoutParams = params
            bottomDialog.window!!.setGravity(Gravity.BOTTOM)
            bottomDialog.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
            var todo = mAdapter.getItem(position)
            if (todo?.status == 0) {
                contentView.findViewById<View>(R.id.done_todo).visibility = View.VISIBLE
            }
            bottomDialog.show()
            // ????????????????????????
            contentView.findViewById<View>(R.id.edit_todo)
                .setOnClickListener { v1: View? ->
                    bottomDialog.dismiss()
                    startActivity<EditTodoActivity>(this) {
                        putExtra(Constant.KEY_TODO_HANDLE_TYPE, Constant.EDIT_TODO)
                        putExtra(Constant.KEY_TODO_TITLE, todo!!.title)
                        putExtra(Constant.KEY_TODO_CONTENT, todo!!.content)
                        putExtra(Constant.KEY_TODO_DATE, todo!!.dateStr)
                        putExtra(Constant.KEY_TODO_PRIORITY, todo!!.priority.toString() + "")
                        putExtra(Constant.KEY_TODO_ID, todo!!.id.toString() + "")
                        putExtra(Constant.KEY_TODO_TYPE, todo!!.type.toString() + "")
                    }
                }


            // ?????????????????????
            contentView.findViewById<View>(R.id.delete_todo)
                .setOnClickListener { v1: View? ->
                    mViewModel.deleteTodo(todo?.id!!)
                    mAdapter.remove(position)
                    bottomDialog.dismiss()
                }

            // ???????????????ToDo
            contentView.findViewById<View>(R.id.done_todo)
                .setOnClickListener { v1: View? ->
                    mViewModel.finishTodo(todo?.id!!, 1)
                    // ??????????????????????????????????????????
                    todo.status = 1
                    mAdapter.notifyDataSetChanged()
                    bottomDialog.dismiss()
                }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_article_list

    private fun initHeadView() {
        headerView = View.inflate(this, R.layout.custom_bar, null)
        headerView.apply {
            detail_title.text = "????????????"
            detail_back.visibility = View.VISIBLE
            detail_search.visibility = View.VISIBLE
            detail_search.setImageResource(R.drawable.ic_add)
            detail_search.setOnClickListener { onAddTodo() }
            detail_back.setOnClickListener { onBackPressed() }
        }
        mAdapter.addHeaderView(headerView)
        initColor()
    }

    private fun initColor() {
        headerView.setBackgroundColor(ColorUtil.getColor(this))
    }

    private fun initRefresh() {
        // ?????????????????????loading??????
        mSrlRefresh.setProgressBackgroundColorSchemeColor(ColorUtil.getColor(this))
        mSrlRefresh.setColorSchemeColors(Color.WHITE)
        mSrlRefresh.setOnRefreshListener {
            onRefreshData()
        }
    }

    private fun setTodoList(systemListName: List<TodoResponse>) {
        // ????????????????????????????????????
        if (systemListName.isEmpty()) {
            mAdapter.loadMoreEnd()
            return
        }

        // ????????????????????????????????????????????????
        if (mSrlRefresh.isRefreshing) {
            mSrlRefresh.isRefreshing = false
            mAdapter.setNewData(systemListName)
            mAdapter.loadMoreComplete()
            return
        }

        // ?????????????????????????????????
        mAdapter.addData(systemListName)
        mAdapter.loadMoreComplete()
    }

    private fun onRefreshData() {
        mCurrentPageNum = 1
        mViewModel.loadTodoList(mCurrentPageNum)
    }

    override fun reLoad() {
        showLoading()
        onRefreshData()
        super.reLoad()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun onAddTodo() {
        UserInfo.instance.startEditTodoActivity(this)
    }

    @Subscribe
    fun settingEvent(event: ChangeThemeEvent) {
        mSrlRefresh.setProgressBackgroundColorSchemeColor(ColorUtil.getColor(this))
        initColor()
    }
}
