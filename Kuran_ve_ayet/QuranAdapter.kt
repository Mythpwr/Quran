package com.example.namaz.ui.Kuran_ve_ayet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.namaz.R

class QuranAdapter // Constructor
    (private val items: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        val item = items!![position]

        if (item is Surah) {
            return TYPE_NEW_SURAH
        } else if (item is Ayet) {
            return if (item.secdeAyeti) TYPE_SECDE_AYET else TYPE_AYET
        } else {
            return TYPE_AYET // VARSAYILAN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_NEW_SURAH) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.sure_basligi_arapca, parent, false)
            return SureViewHolder(view)
        } else if (viewType == TYPE_SECDE_AYET) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_secde_ayet, parent, false)
            return SecdeAyetViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sure_ve_ayet, parent, false)
            return AyetViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SureViewHolder) {
            val surah = items!![position] as Surah
            holder.sureAdi.text = surah.arapcaName
        } else if (holder is SecdeAyetViewHolder) {
            val ayet = items!![position] as Ayet
            holder.ayetText.text = ayet.ayetText
            holder.meal.text = ayet.meal
            holder.ayetId.text = ayet.ayetId.toString()
        } else {
            val ayet = items!![position] as Ayet
            (holder as AyetViewHolder).ayetText.text =
                ayet.ayetText
            holder.meal.text =
                ayet.meal

            // Eğer `ayetIds` boş değilse göster, boşsa gizle
            if (ayet.ayetId.isNotEmpty()) {
                holder.ayetId.text = ayet.ayetId.toString()
                holder.ayetId.visibility = View.VISIBLE
            } else {
                holder.ayetId.visibility = View.GONE // Hiç göstermiyoruz
            }



        }
    }
    fun getItems(): List<Ayet>
    {
        return items.filterIsInstance<Ayet>()
    }
    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    // Sure başlığı için ViewHolder
    class SureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sureAdi: TextView =
            itemView.findViewById(R.id.sure_adi_arapca) // Sure başlığı için TextView
    }

    // Ayet için ViewHolder
    class AyetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ayetText: TextView =
            itemView.findViewById(R.id.arapca_ayet_txt_b)
        var meal: TextView =
            itemView.findViewById(R.id.meal_txt_b)
        var ayetId: TextView =
            itemView.findViewById(R.id.ayet_numara_b)
    }

    class SecdeAyetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ayetText: TextView =
            itemView.findViewById(R.id.arapca_ayet_txt_secde)
        var meal: TextView =
            itemView.findViewById(R.id.meal_txt_secde)
        var ayetId: TextView =
            itemView.findViewById(R.id.ayet_numara_secde)
    }

    fun updatePage(newPageItems: List<Any>?) {
        items!!.clear()
        if (newPageItems != null) {
            items.addAll(newPageItems)
        }
        notifyDataSetChanged()

    }

    companion object {
        private const val TYPE_NEW_SURAH = 0
        private const val TYPE_AYET = 1

        private const val TYPE_SECDE_AYET = 2
    }
}