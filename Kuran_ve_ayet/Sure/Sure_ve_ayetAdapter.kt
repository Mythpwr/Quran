package com.example.namaz.ui.Kuran_ve_ayet.Sure

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.namaz.R

class Sure_ve_ayetAdapter // RecyclerView'ü kaydedin
    (
    private val ayetList: List<SureVeAyet>,
    private val context: Context, // RecyclerView örneği ekleyin
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedMealIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_BESMELE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_besmele_layout, parent, false)
            return BesmeleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sure_ve_ayet, parent, false)
            return AyetViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ayet = ayetList[position]

        // İlgili ViewHolder türünü kontrol ederek bind işlemi yapın
        if (holder is BesmeleViewHolder) {
            holder.bind(ayet)
        } else if (holder is AyetViewHolder) {
            val ayetHolder = holder
            ayetHolder.ayetNumarasi.text = ayet.ayetNumarasi
            ayetHolder.ayetMetni.text = ayet.ayetMetni

            // Meali güncelle
            val ayetMeal = ayet.getMealMetni(selectedMealIndex)
            ayetHolder.mealMetni.text = ayetMeal
        }
    }

    override fun getItemViewType(position: Int): Int {
        val ayet = ayetList[position]
        return if (ayet.ayetNumarasi.isEmpty()) {
            VIEW_TYPE_BESMELE // Ayet numarası yoksa besmele
        } else {
            VIEW_TYPE_AYET // Ayet numarası varsa normal ayet
        }
    }

    override fun getItemCount(): Int {
        return ayetList.size
    }

    fun updateMeal(mealIndex: Int) {
        selectedMealIndex = mealIndex
        notifyDataSetChanged()
    }


    fun setTextSize(size: Float) {
        for (i in 0 until itemCount) {
            val holder =
                recyclerView.findViewHolderForAdapterPosition(i) as AyetViewHolder? // RecyclerView üzerinden çağırın
            if (holder != null) {
                holder.ayetMetni.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                holder.mealMetni.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            }
        }
        notifyDataSetChanged() // Tüm görünümü güncelle
    }

    internal class AyetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ayetNumarasi: TextView =
            itemView.findViewById(R.id.ayet_numara_b)
        var ayetMetni: TextView =
            itemView.findViewById(R.id.arapca_ayet_txt_b)
        var mealMetni: TextView =
            itemView.findViewById(R.id.meal_txt_b)
    }

    inner class BesmeleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var besmeleText: TextView =
            itemView.findViewById(R.id.besmele_arapca_txt)
        var besmeleMeal: TextView =
            itemView.findViewById(R.id.besmele_meal_txt)

        fun bind(sureVeAyet: SureVeAyet) {

            besmeleMeal.text = sureVeAyet.getMealMetni(0) // Sabit besmele meal
        }
    }

    companion object {
        private const val VIEW_TYPE_BESMELE = 0
        private const val VIEW_TYPE_AYET = 1
    }
}

