package com.example.baseandroid.features.logviewer.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.baseandroid.databinding.SpinnerItemBinding
import com.example.baseandroid.features.logviewer.model.SpinnerModel


class SpinnerAdapter(
    mContext: Context,
    private val listState: List<SpinnerModel>,
    val callOnClick: (Int) -> Unit,
) : ArrayAdapter<SpinnerModel?>(mContext, 0, listState) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {

        val tag = listState[position]
        val binding = SpinnerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val view = convertView ?: binding.root

        if (convertView == null) {
            binding.spinnerText.text = tag.getTitle()
            binding.spinnerCheckbox.isChecked = tag.isSelected()
            if (position == 0) {
                binding.spinnerCheckbox.visibility = View.GONE
                binding.spinnerText.setTypeface(null, Typeface.BOLD)
            }
        }
//        if (position > 0) binding.root.setOnClickListener {
//            if (binding.spinnerCheckbox.isChecked) {
//                binding.spinnerCheckbox.isChecked = false
//                tag.setSelected(false)
//            } else {
//                binding.spinnerCheckbox.isChecked = true
//                tag.setSelected(true)
//            }
//            callOnClick(position)
//        }
//        if (position == 1) {
//            binding.spinnerCheckbox.isChecked = true
//            tag.setSelected(true)
//        }

        return view
    }
}