# Kuran Projesi
Bu projede Kur'an-ı Kerim'i diyanetin sitesine göre sayfa sayfa olarak 'Android' uygulumasını olarak gösterimini sağlar. Veriler Json formatında sayfa sayfa olarak düzenlendi ve 'recyclerview ve adapter' aracılığı ile yüklenir.


# Verilerin yüklenmesi
Düzenli olması açısından verileri ayrı ayrı aldım.
1. 'Page' için;
```kotlin

    fun parsePagesFromJson(jsonString: String): List<Page> {
        val pageList: MutableList<Page> = ArrayList()

        try {
            val jsonObject = JSONObject(jsonString)
            val pagesArray = jsonObject.getJSONArray("pages")

            for (i in 0 until pagesArray.length()) {
                val pageObject = pagesArray.getJSONObject(i)
                val pageNumber = pageObject.getInt("pageNumber")
                val surahsArray = pageObject.getJSONArray("surahs")

                val surahList = parseSurahs(surahsArray) // Surahları bir metodla çözebiliriz
                pageList.add(Page(pageNumber, surahList))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return pageList
    }
 
2.'Surah' için;

 @Throws(JSONException::class)
    private fun parseSurahs(surahsArray: JSONArray): List<Surah> {
        val surahList: MutableList<Surah> = ArrayList()
        for (j in 0 until surahsArray.length()) {
            val surahObject = surahsArray.getJSONObject(j)

            val sureId = surahObject.getInt("sureId")
            val sureAdiArapca = surahObject.getString("sureAdiArapca")
            val sureAdiTr = surahObject.getString("sureAdiTr")
            val yeniSure = surahObject.getBoolean("yeniSure")
            val ayetList = parseAyetler(surahObject.getJSONArray("ayetler"))

            surahList.add(Surah(sureId, sureAdiArapca, sureAdiTr, ayetList, yeniSure))
        }
        return surahList
    } 

3.'Ayet' için; 

    @Throws(JSONException::class)
    private fun parseAyetler(ayetlerArray: JSONArray): List<Ayet> {
        val ayetList: MutableList<Ayet> = ArrayList()
        for (k in 0 until ayetlerArray.length()) {
            val ayetObject = ayetlerArray.getJSONObject(k)

            val secdeAyeti = ayetObject.getBoolean("secdeAyeti")
            val ayetIds: MutableList<Int> = ArrayList()


            val ayetIdsArray = ayetObject.getJSONArray("ayetId")

            for (i in 0 until ayetIdsArray.length()) {
                ayetIds.add(ayetIdsArray.getInt(i))

            }


            val ayetText = ayetObject.getString("ayetText")
            val meal = ayetObject.getString("meal")

            // Eğer `ayetIds` tamamen boşsa, bu ayeti listeye ekleme
            ayetList.add(Ayet(ayetIds, ayetText, meal, secdeAyeti))

        }
        return ayetList
    }
 

4.Adapter Sınıfı
Adapter için 'besmele, ayet ve secde ayeti' için 3 farklı viewholder kullanılmıştır.


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
    /*
    fun getItems(): List<Ayet>
    {
        return items.filterIsInstance<Ayet>()
    }*/
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
 

