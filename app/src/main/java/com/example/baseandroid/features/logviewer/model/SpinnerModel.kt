package com.example.baseandroid.features.logviewer.model

class SpinnerModel {
    private var selected = false
    private var title: String = ""

    fun isSelected(): Boolean {
        return selected
    }

    fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

}