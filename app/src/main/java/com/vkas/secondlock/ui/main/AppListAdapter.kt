package com.vkas.secondlock.ui.main

import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vkas.secondlock.R
import com.vkas.secondlock.bean.SlAppBean
import com.vkas.secondlock.utils.KLog
import com.vkas.secondlock.utils.SLUtils

class AppListAdapter (data: MutableList<SlAppBean>?) :
    BaseQuickAdapter<SlAppBean, BaseViewHolder>(
        R.layout.item_app,
        data
    ) {
    override fun convert(holder: BaseViewHolder, item: SlAppBean) {
        KLog.d("TAG", "item.appNameSl===${item}")
        if (SLUtils.isOnRight == 0) {
            //在左边
            setVisibility(item.isLocked, holder.itemView)
        } else {
            //在右边
            setVisibility(true, holder.itemView)
        }
        holder.setImageDrawable(R.id.img_app_icon, item.appIconSl)
        holder.setText(R.id.tv_app_name, item.appNameSl)
        if (item.isLocked) {
            holder.setImageDrawable(
                R.id.img_down_state,
                ContextCompat.getDrawable(context, R.drawable.ic_lock)
            )
        } else {
            holder.setImageDrawable(
                R.id.img_down_state,
                ContextCompat.getDrawable(context, R.drawable.ic_no_lock)
            )
        }
    }

    private fun setVisibility(isVisible: Boolean, itemView: View) {
        val param = itemView.layoutParams as RecyclerView.LayoutParams
        if (isVisible) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT
            param.width = LinearLayout.LayoutParams.MATCH_PARENT
            itemView.visibility = View.VISIBLE
        } else {
            itemView.visibility = View.GONE
            param.height = 0
            param.width = 0
        }
        itemView.layoutParams = param
    }
}