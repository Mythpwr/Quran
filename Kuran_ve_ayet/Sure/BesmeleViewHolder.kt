package com.example.namaz.ui.Kuran_ve_ayet.Sure

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.namaz.R

class BesmeleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var besmeleText: TextView =
        itemView.findViewById(R.id.besmele_arapca_txt)
    var besmeleMeal: TextView =
        itemView.findViewById(R.id.besmele_meal_txt)

    fun bind(sureVeAyet: SureVeAyet) {
      //  besmeleText.text = sureVeAyet.getAyetMetni()
        besmeleMeal.text = sureVeAyet.getMealMetni(0) // Sabit besmele meal
    }
}
