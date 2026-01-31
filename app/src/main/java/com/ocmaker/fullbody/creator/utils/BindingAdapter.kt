package com.ocmaker.fullbody.creator.utils


import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import androidx.core.graphics.toColorInt
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.data.model.LanguageModel

@BindingAdapter("setBGCV")
fun ConstraintLayout.setBGCV(check: LanguageModel) {
        this.setBackgroundResource(R.drawable.bg_card_border_100_false)

}
//@BindingAdapter("setCard")
//fun ImageView.setCard(model: LanguageModel) {
//    if (model.active) {
//        this.setBackgroundResource(R.color.showdown_olive)
//    } else {
//        this.background = null  // Hoặc setBackgroundResource(0)
//    }
//}

@BindingAdapter("setSrcCheckLanguage")
fun AppCompatImageView.setSrcCheckLanguage(check: Boolean) {
    if (check) {
        this.setImageResource(R.drawable.img_radio_language_select)
    } else {
        this.setImageResource(R.drawable.img_radio_language_unselect)
    }
}
@BindingAdapter("setTextColor")
fun TextView.setTextColor(check: Boolean) {
    if (check) {
        this.setTextColor("#6B1B10".toColorInt())
    } else {
        this.setTextColor("#6B1B10".toColorInt())
    }
}
@BindingAdapter("setBG")
fun AppCompatImageView.setBG(id: Int) {
    Glide.with(this).load(id).into(this)
}
@BindingAdapter("setImg")
fun AppCompatImageView.setImg(data : Int){
    Glide.with(this).load(data).into(this)
}