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
 ```

2.'Surah' için;
```kotlin
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
```
3.'Ayet' için; 
```kotlin
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
 
```
4.Adapter Sınıfı
Adapter için 'besmele, ayet ve secde ayeti' için 3 farklı viewholder kullanılmıştır.

```kotlin
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
 
```
5. Sayfa numarasını işleme ve sayfayı bulma
```kotlin
 fun getAyetlerByPageNumber(pageList: List<Page>, pageNumber: Int): MutableList<Any> {
        val items: MutableList<Any> = ArrayList()

        for (page in pageList) {
            if (page.pageNumber == pageNumber) {
                for (surah in page.surahList) {
                    if (surah.isNewSurah) {
                        // Önce sure başlığı için boş bir ayet nesnesi ekleyelim
                        items.add(surah)
                    }

                    // Suredeki tüm ayetleri ekle
                    items.addAll(surah.ayetList)
                }
                break // Sayfayı bulduk, döngüden çıkabiliriz
            }
        }

        return items
    }
    ```

6. Sure ve ayet spinner güncelleme.
Burada sure ve ayetleri spinner a ekliyiyoruz ve seçilen surenin ayetlerini 'ayetspinner'da gösteriyoruz.
 Ayrıca surespinner veya 'goToAyet' metoduyla seçilen ayete otomatik gidiyor ve recyclerview i o ayete kaydırma yapıyor. Sure seçildiğinde ise yine o surenin olduğu kısma gidiyor.
 
 
 ```kotlin
 //Bu KISMI oncreate metoduna yazın
  var ilkacilis = true
        val firstSureOnPage = quranItems
            ?.find { it.pageNumber == enteredPageNumber }
            ?.surahList
            ?.firstOrNull() // İlk sureyi al
// Null güvenli şekilde sureAdlari listesini al
        firstSureOnPage?.let { firstSure ->
            sureAdlari?.let { sureList ->
                // Spinner için adapter oluştur
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, sureList).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }

                sureSpinner?.adapter = adapter

                // Eğer daha önce seçilmiş bir sure varsa, onu seçili hale getir
                sureAdi?.let { seciliSure ->
                    val position =
                        sureList.indexOfFirst { it.contains(seciliSure) } // İçeren değeri bul
                    if (position >= 0) sureSpinner?.setSelection(position)
                }

                // Spinner seçim dinleyicisi
                sureSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (ilkacilis) {
                            ilkacilis = false
                            return
                        }
                        val selectedSure = parent?.getItemAtPosition(position).toString()


                        // Örneğin "1. Fatiha Suresi" -> "Fatiha Suresi" olacak
                        val sureAdiTemiz = selectedSure.substringAfter(". ").trim()

                        val pageNumber =
                            sureMap[sureAdiTemiz] // Temizlenmiş sure adı ile eşleşme yap
                        pageNumber?.let {
                            // selectedSureId = sureAdlari!![position].sureId
                            showPage(it) // İlgili sayfaya git
                            recyclerView!!.smoothScrollToPosition(it)
                            selected_Surah = quranItems
                                ?.find { it.pageNumber == pageNumber }
                                ?.surahList
                                ?.find { it.trName == sureAdiTemiz } // Türkçe ismine göre eşleşme yap

                            selected_Surah?.let { updateAyetSpinner(it) }
                        }

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Seçim yapılmazsa bir şey yapmaya gerek yok
                    }
                }
            }
        }
  ```

 ```kotlin
private fun updateSureSpinnerSelection(pageNumber: Int) {
        // 🔹 Sayfadaki tüm ayetleri al
        val pageItems = getAyetlerByPageNumber(quranItems!!, pageNumber)

        // 🔹 Sayfada yer alan sureleri bul
        val sureList = pageItems.filterIsInstance<Surah>() // Sadece Surah nesnelerini al

        // 🔹 Sayfadaki ilk sureyi seç (Eğer sayfada birden fazla sure varsa, ilkini al)
        val currentSure = sureList.firstOrNull() ?: return


        // 🔹 Spinner'daki pozisyonu bul ve güncelle
        val index = sureAdlari?.indexOfFirst { it.contains(currentSure.trName) } ?: -1
        if (index >= 0) {
            sureSpinner?.setSelection(index)
            updateAyetSpinner(currentSure)
        }

        //   Log.d("Current Sure", "Index: $index, Sure: ${currentSure.trName}")
    }
    private fun updateAyetSpinner(surah: Surah?) {
        if (surah == null) return

        // Seçilen surenin tüm ayetlerini al (tüm sayfalardan)
        val ayetler = quranItems?.flatMap { it.surahList }
            ?.filter { it.sureId == surah.sureId } // Aynı sureId'ye sahip sureleri al
            ?.flatMap { it.ayetList } // Tüm ayetleri birleştir
            ?: emptyList()

        if (ayetler.isEmpty()) return

        val ayetAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ayetler.map { "Âyet ${it.ayetId.joinToString(",")}" }
        )
        ayetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ayetSpinner?.adapter = ayetAdapter
        ayetSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (view == null) return // Eğer view null ise işlemi durdur
                selectedAyet = ayetler.getOrNull(position)

                if (selectedAyet != null && selectedAyet!!.ayetId.isNotEmpty()) {
                    selected_Surah?.let { sure ->
                        goToAyet(sure.sureId, selectedAyet!!.ayetId.first())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    fun goToAyet(sureId: Int, ayetId: Int) {
        val page = quranItems?.find { page ->
            page.surahList.any { it.sureId == sureId && it.ayetList.any { ayet -> ayet.ayetId.contains(ayetId) } }
        } ?: return

        val sure = page.surahList.find { it.sureId == sureId } ?: return
        val ayet = sure.ayetList.find { it.ayetId.contains(ayetId) } ?: return

        selected_Surah = sure
        selectedAyet = ayet

        updateAyetSpinner(sure)
        showPage(page.pageNumber) // Ayetin bulunduğu sayfaya git

        // Sayfa değişikliğinin tamamlanması için 300ms gecikme ekleyelim
        recyclerView?.postDelayed({
            val position = sure.ayetList.indexOf(ayet)
            if (position != -1) {
                recyclerView?.smoothScrollToPosition(position) // Yumuşak kaydırma
            }
        }, 300)
    }
  ```

  7.sayfayı gösterme
  bunlar  öncekive sonraki  sayfayı göstermek için gerekli 
  ```kotlin
      private fun showPreviousPage() {
        val newPageNumber = if (enteredPageNumber > 0) {
            enteredPageNumber - 1 // Bir önceki sayfaya git
        } else {
            quranItems!!.size - 1 // İlk sayfadaysa, son sayfaya git
        }
        // Eğer sayfa gerçekten değiştiyse işlemi yap
        if (newPageNumber != enteredPageNumber) {
            showPage(newPageNumber)
        }

    }

    private fun showNextPage() {
        val newPageNumber = if (enteredPageNumber < quranItems!!.size - 1) {
            enteredPageNumber + 1 // Bir sonraki sayfaya git
        } else {
            0 // Son sayfadaysa, başa dön
        }
        // Eğer sayfa gerçekten değiştiyse işlemi yap
        if (newPageNumber != enteredPageNumber) {
            showPage(newPageNumber)
        }
    }


    private fun showPage(pageNumber: Int) {
        if (pageNumber >= 0 && pageNumber <= quranItems!!.size) {
            if (enteredPageNumber == pageNumber) return
            enteredPageNumber = pageNumber

            // Sayfa numarasına karşılık gelen ayetler ve sure başlıkları
            val pageItems = getAyetlerByPageNumber(quranItems!!, pageNumber)

            // Adapter'ı güncelle
            if (pageItems != null) {
                (recyclerView!!.adapter as QuranAdapter).updatePage(pageItems)
                recyclerView!!.smoothScrollToPosition(0)
            }
            // Sayfa numarasını giriş kutusuna yazdır
            sayfayaGit!!.setText(pageNumber.toString())
            // 🔹 Spinner'daki seçimi güncelle
            updateSureSpinnerSelection(enteredPageNumber)
        }
    }



  ```

  8.Sure adlarını alma 
  ```kotlin
 private fun getSureAdlariFromJson(): List<String> {
        val sureAdlari = mutableListOf<String>()

        try {
            // JSON dosyasını oku
            val inputStream = assets.open("data sure/sure verileri.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, charset("UTF-8"))

            // JSON'u işle
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val sureNo = jsonObject.getInt("id")
                val sureAdi = jsonObject.getString("sureAdi")
                val pageNumber = jsonObject.getInt("pageNumber")

                sureAdlari.add("$sureNo. $sureAdi") // Sadece sure adını ekle
                sureMap[sureAdi] = pageNumber // Sayfa numarasını eşleştir
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return sureAdlari
    }
  ```

9.Burada gitmek istediğiniz sayfa numarasını yazarak o sayfaya gitmek için

  ```kotlin
   fun initializeUI() {
        try {
           
           val nextPageButton =   findViewById<ImageButton>(R.id.sonraki_sayfa)
            val prePageButton =   findViewById<ImageButton>(R.id.onceki_sayfa)

          nextPageButton?.setOnClickListener { showNextPage() }
            prePageButton?.setOnClickListener{ showPreviousPage() }

            val coroutineScope = CoroutineScope(Dispatchers.Main)


            var job: Job? = null // Önceki işlemi iptal etmek için bir Job referansı

            sayfayaGit!!.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    val pageNumber = sayfayaGit!!.text.toString().toIntOrNull()
                    if (pageNumber != null) {
                        showPage(pageNumber)
                    } else {
                        Toast.makeText(this, "Geçersiz sayfa numarası.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    sayfayaGit?.clearFocus()
                    hideKeyboard(v)
                    return@setOnEditorActionListener true
                }
                false
            }

            sayfayaGit!!.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    (view as EditText).setText("")
                }
            }

            sayfayaGit!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    job?.cancel() // Önceki bekleyen işlemi iptal et
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    job?.cancel() // Önceki işlemi iptal et (gereksiz gecikmeleri önler)
                    job = coroutineScope.launch {
                        delay(2000) // 2 saniye bekle
                        val text = editable.toString()
                        val pageNumber = text.toIntOrNull()
                        if (pageNumber != null) {
                            if (pageNumber < quranItems!!.size) {
                                showPage(pageNumber)
                                sayfayaGit?.clearFocus()
                            } else {
                                showPage(0)
                                sayfayaGit?.clearFocus()
                                hideKeyboard(sayfayaGit!!)
                            }
                        }
                    }
                }
            })
            sayfayaGit?.clearFocus()
            hideKeyboard(sayfayaGit!!)
            sayfayaGit?.setText(enteredPageNumber.toString())



        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Geçersiz sayfa numarası.", Toast.LENGTH_SHORT).show()
        }
    }

     private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
  ```
10.Sayfa numarasını kayıt etme ve yükleme 

```kotlin

    private fun saveLastPage(pageNumber: Int) {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("last_page", pageNumber)
        editor.apply()
    }

    private fun deleteLastPage() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("last_page")
        editor.apply()
    }

    private fun getLastPage(): Int {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getInt("last_page", 0) // Varsayılan olarak 0. sayfa
    }
  ```
