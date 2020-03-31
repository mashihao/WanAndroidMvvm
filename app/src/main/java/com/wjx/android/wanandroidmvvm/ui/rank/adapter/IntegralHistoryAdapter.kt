package com.wjx.android.wanandroidmvvm.ui.rank.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wjx.android.wanandroidmvvm.R
import com.wjx.android.wanandroidmvvm.base.utils.Util
import com.wjx.android.wanandroidmvvm.ui.rank.data.IntegralHistoryResponse
import kotlinx.android.synthetic.main.integral_history_item.view.*

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2020/3/29 19:45
 */
class IntegralHistoryAdapter(layoutId: Int, listData: MutableList<IntegralHistoryResponse>?) :
    BaseQuickAdapter<IntegralHistoryResponse, BaseViewHolder>(layoutId, listData) {
    override fun convert(viewHolder: BaseViewHolder, item: IntegralHistoryResponse?) {
        viewHolder?.let { holder ->
            holder.itemView.integral_material_card.rippleColor = Util.getOneColorStateList(mContext)
            holder.itemView.integral_material_card.strokeColor = Util.getColor(mContext)
            val descStr = if (item!!.desc.contains("积分")) item.desc.subSequence(
                item.desc.indexOf("积分"),
                item.desc.length
            ) else ""
            holder.setText(R.id.item_integralhistory_title, item.reason +"" + descStr)
            holder.setText(R.id.item_integralhistory_date, Util.formatDate(item.date))
            holder.setText(R.id.item_integralhistory_count, "+(" + item.coinCount+ ")")
            holder.setTextColor(R.id.item_integralhistory_count, Util.getColor(mContext))
        }
    }
}