package com.example.namaz.ui.Kuran_ve_ayet

import android.provider.Settings

import android.Manifest
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.namaz.CustomProgressDialog
import com.example.namaz.MainActivity
import com.example.namaz.MediaPlayerHelperforQuran
import com.example.namaz.R
import com.example.namaz.databinding.ActivityQuranBinding
import com.example.namaz.ui.Kuran_ve_ayet.Sure.SureVeAyet

import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log

class Quran : AppCompatActivity() {
    private enum class secenekType(val secenekKey: String) {
        option1("secenek1"),
        option2("secenek2")
    }

    private val sureMap = mutableMapOf<String, Int>()
    private lateinit var binding: ActivityQuranBinding
    var notificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null
    var button_decrease: ImageButton? = null
    var button_increase: ImageButton? = null
    var baslat: ImageButton? = null
    var durdur: ImageButton? = null
    var geri: ImageButton? = null
    var cancel: ImageButton? = null
    var sesDosya: String? = ""
    var sureAdlari: List<String>? = null
    private var storagePermissionGranted = false
    private var mediaPlayer: MediaPlayerHelperforQuran? = null
    private var sureAdiTextView: TextView? = null
    private var arapca_adi_txt: TextView? = null
    private val sureBasligiTr: TextView? = null
    private var currentTime: TextView? = null
    private var duration: TextView? = null
    private val ayet_textView: TextView? = null
    private val mealTextView: TextView? = null
    private val tefsirText: TextView? = null
    private var sayfayaGit: EditText? = null
    private var seekBar: SeekBar? = null
    var downloadB: ImageButton? = null
    private var nextPageButton: ImageButton? = null
    private var previousPageButton: ImageButton? = null
    private var executorService: ExecutorService? = null
    private var isPanelOpen = false
    private var sidePanel: LinearLayout? = null
    private var currentDownloadIndex = 0 // İndirme işlemlerindeki mevcut dizin
    private var dimBackground: View? = null
    private var sureSpinner: Spinner? = null
    private var ayetSpinner: Spinner? = null
    private var recyclerView: RecyclerView? = null
    private val recyclerView2: RecyclerView? = null
    private var selectedMealIndex = 0 // Başlangıçta ilk meali göster
    private var loadingLayout: RelativeLayout? = null

    private var quranItems: List<Page>? = null

    private var selectedPage: Int? = null
    private var enteredPageNumber = 0 // Başlangıç sayfası (0. index)

    private var selectedAyet: Ayet? = null

    var isBookmarked = false // Başlangıçta varsayılan olarak yer işareti yok
    private var selected_Surah: Surah? = null

    // val sharedPref = getSharedPreferences("QuranApp", Context.MODE_PRIVATE)
    fun askForDownload(sureNumara: Int) {
        // İzin isteme kodu

        AlertDialog.Builder(this)
            .setTitle("İndirme Onayı")
            .setMessage("Ses dosyasını indirmek istiyor musunuz?")
            .setPositiveButton(
                "Evet"
            ) { dialog: DialogInterface?, which: Int ->
                // Kullanıcı onaylarsa Firebase'i başlatın ve ses dosyasını indirin
                showOptions(sureNumara)
            }
            .setNegativeButton(
                "Hayır"
            ) { dialog: DialogInterface?, which: Int ->
                // Kullanıcı onay vermezse, işlemi iptal edin
                Toast.makeText(
                    this, "İndirme işlemi başlatılmadı.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun showOptions(sureNumara: Int) {
        AlertDialog.Builder(this)
            .setTitle("Seçiniz.")
            .setMessage("Hangisini seçmek istiyorsunuz? Tekli indirme(Şuan bulunduğunuz sureyi indirir. Diğerini seçerseniz hepsini indirir")
            .setPositiveButton(
                "Şuankini indir"
            ) { dialog: DialogInterface?, which: Int ->
                // Kullanıcı onaylarsa Firebase'i başlatın ve ses dosyasını indirin
                //downloadAudioFilesAll();
                if (isInternetAvailable) {
                    downloadAudioFileOnebyOne(sureNumara)
                } else {
                    Toast.makeText(
                        this,
                        "İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edin ve tekrar deneyin.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Çoklu İndirme(Hepsini indirir)"
            ) { dialog: DialogInterface?, which: Int ->

                // Kullanıcı onay vermezse, işlemi iptal edin
                if (isInternetAvailable) {
                    downloadAudioFilesAll()
                } else {
                    Toast.makeText(
                        this,
                        "İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edin ve tekrar deneyin.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true
            } else if (requestCode != REQUEST_EXTERNAL_STORAGE) {
                storagePermissionGranted = false
                // İzin verilmediğini kullanıcıya bildirin
                Toast.makeText(
                    this, "Depolama izni verilmedi. Dosyaları indiremezsiniz.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (requestCode == REQUEST_POST_NOTIFICATION) {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) notificationManager!!.notify(
                    1,
                    notificationBuilder!!.build()
                )
            } else {
                // Bildirim izni reddedildiğinde yapılacak işlemler
                Toast.makeText(this, "Bildirim izni verilmedi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surahIdsFromJson: List<Int>
        get() {
            val surahIds: MutableList<Int> =
                ArrayList()


            try {
                // JSON dosyasını oku
                val inputStream =
                    assets.open("data sure/sure verileri.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                val json = String(buffer, charset("UTF-8"))

                // JSON'dan sure numaralarını çıkar
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val sureId = jsonObject.getInt("id")
                    surahIds.add(sureId)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return surahIds
        }

    private fun animatePanel(startMargin: Int, endMargin: Int) {
        val params = sidePanel!!.layoutParams as MarginLayoutParams
        val animator =
            ObjectAnimator.ofInt(MarginUpdater(params), "marginEnd", startMargin, endMargin)
        animator.setDuration(300)
        animator.start()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    // Paneli açma fonksiyonu
    private fun openPanel() {
        animatePanel(-350, 0) // Paneli göster
        dimBackground!!.elevation = 2f
        dimBackground!!.visibility = View.VISIBLE // Arka planı koyulaştır
        dimBackground!!.isClickable = true
        isPanelOpen = true
        sidePanel!!.elevation = 3f
    }

    // Paneli kapatma fonksiyonu
    private fun closePanel() {
        animatePanel(0, -1000) // Paneli gizle
        dimBackground!!.visibility = View.GONE // Arka planı normal hale getir
        dimBackground!!.isClickable = false
        isPanelOpen = false
        dimBackground!!.elevation = 0f
        sidePanel!!.elevation = 0f
        hideKeyboard()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityQuranBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        loadingLayout = binding.loadingLayout
        sidePanel = findViewById(R.id.sidePanel) // LinearLayout olarak tanımlı panel
        val openButton = findViewById<ImageButton>(R.id.open_button) // Paneli açan buton

        // progressDialogC = new CustomProgressDialog(this);
        sayfayaGit = binding.sayfaGirSpinner
        nextPageButton = binding.sonrakiSayfa
        previousPageButton = binding.oncekiSayfa


        dimBackground = binding.dimBackground






        FirebaseApp.initializeApp(this)

        val intent = intent
        val sureNo = intent!!.getIntExtra("Sure No", 0)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        sureAdlari = getSureAdlariFromJson()


        downloadB = findViewById(R.id.downloadButton)

        seekBar = findViewById(R.id.seekBar)
        currentTime = findViewById(R.id.currenttime)
        duration = findViewById(R.id.duration)

        button_increase = findViewById(R.id.button_yazı_buyut)
        button_decrease = findViewById(R.id.button_yazı_kucult)
        cancel = findViewById(R.id.cancel_Button)

        geri = binding.goBack
        baslat = findViewById(R.id.button_baslat)
        durdur = findViewById(R.id.button_durdur)
        sureAdiTextView = binding.sureAd

        ayetSpinner = findViewById(R.id.ayetSpinner)
        arapca_adi_txt = binding.sureAdiArapca

        executorService = Executors.newSingleThreadExecutor()

        recyclerView = binding.recyclerView

        recyclerView!!.layoutManager = LinearLayoutManager(this)

        sayfayaGit = findViewById(R.id.sayfa_gir_spinner)
        openButton.setOnClickListener {
            if (isPanelOpen) {
                closePanel()
            } else {
                openPanel()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isPanelOpen) {
                    closePanel()
                } else {
                    isEnabled = false
                    remove()
                    finish()
                }
            }
        })

        dimBackground!!.setOnClickListener {
            Log.d("DimBackground", "Tıklandı")
            if (isPanelOpen) {
                closePanel()
            }
        }
        val json = loadJsonFromAssets(this, "data sure/sayfa_duzenli_kuran.json")
        if (json != null) {
            quranItems = parsePagesFromJson(json)

            // pages listesi artık kullanılabilir.
        }

        enteredPageNumber = getLastPage()


        Log.d("Entered Page NUmber", "Şuanki değer:  $enteredPageNumber ")

        var adapter = QuranAdapter(mutableListOf())
        recyclerView!!.adapter = adapter
        lifecycleScope.launch {

            val firstPageItems = withContext(Dispatchers.IO) {
                getAyetlerByPageNumber(quranItems!!, enteredPageNumber)
            }
            recyclerView!!.adapter = QuranAdapter(firstPageItems)

        }



        isBookmarked = (enteredPageNumber != 0)

        if (isBookmarked) {
            updateSureSpinnerSelection(enteredPageNumber)
            findViewById<ImageButton>(R.id.savePageButton).setImageResource(R.drawable.marked_icon) // Dolu simge
        } else {
            findViewById<ImageButton>(R.id.savePageButton).setImageResource(R.drawable.unmark_icon) // Boş simge
        }

// Butona tıklama işlemi
        findViewById<ImageButton>(R.id.savePageButton).setOnClickListener {
            if (isBookmarked) {
                // Yer işaretini kaldır
                deleteLastPage()
                findViewById<ImageButton>(R.id.savePageButton).setImageResource(R.drawable.unmark_icon) // Boş simge
            } else {
                Log.d(
                    "EnteredPage",
                    "EnteredPage: $enteredPageNumber"
                ) // Sayfanın gerçekten çağrıldığını kontrol et

                // Mevcut sayfayı kaydet
                saveLastPage(enteredPageNumber)
                findViewById<ImageButton>(R.id.savePageButton).setImageResource(R.drawable.marked_icon) // Dolu simge
            }
            isBookmarked = !isBookmarked // Durumu tersine çevir
        }



        initializeUI()

        downloadB?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 (API 30) ve sonrası için özel izin kontrolü
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse("package:" + applicationContext.packageName)
                        startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        startActivity(intent)
                    }
                } else {
                    // İzin zaten varsa indirme işlemini gerçekleştir
                    storagePermissionGranted = true
                    askForDownload(sureNo)
                }
            } else {
                // Android 10 ve öncesi için klasik izin kontrolü
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_EXTERNAL_STORAGE
                    )
                } else {
                    storagePermissionGranted = true
                    askForDownload(sureNo)
                }
            }
        }


        val ayetList: MutableList<SureVeAyet> = ArrayList()
        val ayetList2: MutableList<SureVeAyet> = ArrayList()


        //   Sure_ve_ayetAdapter adapter = new Sure_ve_ayetAdapter(ayetList, this, recyclerView);
        //Sure_ve_ayetAdapter adapter2 = new Sure_ve_ayetAdapter(ayetList2, this, recyclerView);

        //recyclerView.setAdapter(adapter);
        // Ayet listesini başlat
        if (intent != null) {
            val sureBasligi = intent.getStringExtra("Sure Basligi")
            val sureBasligiArapca = intent.getStringExtra("arapca_ad")


            arapca_adi_txt!!.text = sureBasligiArapca
            sureAdiTextView!!.text = sureBasligi


            val appFolder = File(getExternalFilesDir(null), "SureVeDualar")


            val audioFile = File(appFolder, "$sureBasligi.mp3")

            if (audioFile.exists()) {
                Log.d("FilePath", "File Path: " + audioFile.absolutePath)
                sesDosya = audioFile.absolutePath // Dosya zaten varsa dosya yolunu döndür
            } else {
                Toast.makeText(
                    this, "Hata! Belirtilen konumda ses dosyası bulunamadı. Lütfen indirin.",
                    Toast.LENGTH_SHORT
                ).show()
                sesDosya = null
            }
        }


        // string-array dizisini kullanarak Spinner'a adapte etme
        val sureAdi = intent.getStringExtra("Sure Basligi")

        //  loadDataFromTxt(sureAdi,adapter,ayetList,MealType.MEAL1);
        //LoadDataFromJson(sureAdi, ayetList, adapter,MealType.MEAL1);
// Spinner'ları tanımlayın
        sureSpinner = findViewById(R.id.sureSpinner)
        val mealSpinner = findViewById<Spinner>(R.id.mealSpinner)

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


        // mealSpinner için Listener
        val adapter_ = ArrayAdapter.createFromResource(
            this,
            R.array.meal_options,
            android.R.layout.simple_spinner_item
        )
        adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealSpinner.adapter = adapter_

        mealSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedMealIndex = position
                ayetList.clear()
                ayetList2.clear()

                showLoadingScreen()

                Handler().postDelayed({
                    if (selectedMealIndex == 0) {
                        //loadDataFromTxt(sureAdi, adapter, ayetList, MealType.MEAL1, secenekType.option1);
                        //  adapter.notifyDataSetChanged();
                    } else if (selectedMealIndex == 1) {
                        // loadDataFromTxt(sureAdi, adapter2, ayetList2, MealType.MEAL2, secenekType.option2);
                        // adapter2.notifyDataSetChanged();
                    }
                    // adapter.updateMeal(selectedMealIndex);
                    // recyclerView.swapAdapter(selectedMealIndex == 0 ? adapter : adapter2, true);
                    hideLoadingScreen()
                }, 1500) // 1.5 saniye bekleme süresi

                // adapter.notifyDataSetChanged();
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Bir işlem gerekirse
            }
        }


        val mainPage = binding.sidePanel.goToMainPage
        mainPage.setOnClickListener {
            finish()
            val intent = Intent(
                this@Quran,
                MainActivity::class.java
            )
            startActivity(intent)
        }

        val uri = getAudioFile(sureNo)


        mediaPlayer = MediaPlayerHelperforQuran(this, currentTime!!, seekBar!!, uri)

        mediaPlayer!!.setTotalTimeTextView(currentTime!!)

        mediaPlayer!!.play(baslat!!)

        mediaPlayer!!.stop(durdur!!, baslat!!)
        button_increase?.setOnClickListener(View.OnClickListener {
            val scaleFactor = 1.2f // Büyültme faktörü
            val currentSize = ayet_textView!!.textSize // Mevcut metin boyutunu al
            val newSize = currentSize * scaleFactor // Yeni boyutu hesapla
            //adapter.setTextSize(newSize);
        })

        button_decrease?.setOnClickListener(View.OnClickListener {
            val scaleFactor = 0.8f // Büyültme faktörü
            val currentSize = ayet_textView!!.textSize // Mevcut metin boyutunu al
            val newSize = currentSize * scaleFactor // Yeni boyutu hesapla
            // adapter.setTextSize(newSize);
        })

        geri!!.setOnClickListener { finish() }



    }

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

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun goToAyet(sureId: Int, ayetId: Int) {
        val sure =
            quranItems?.find { it.pageNumber == selectedPage }?.surahList?.find { it.sureId == sureId }
        if (sure != null) {
            selected_Surah = sure
            updateAyetSpinner(sure)

            selectedAyet = sure.ayetList.find { it.ayetId.contains(ayetId) }
            //scrollToSelectedAyet()
        }
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
                        goToAyet2(sure.sureId, selectedAyet!!.ayetId.first())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    fun goToAyet2(sureId: Int, ayetId: Int) {
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


    // Sayfa numarasını işleme ve sayfayı bulma
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


    fun initializeUI() {
        try {
            showLoadingScreen()
            val nextPageButtons = listOf(
                findViewById<ImageButton>(R.id.sonraki_sayfa),

                )

            val previousPageButtons = listOf(
                findViewById<ImageButton>(R.id.onceki_sayfa),

                )

            nextPageButtons.forEach { button -> button?.setOnClickListener { showNextPage() } }
            previousPageButtons.forEach { button -> button?.setOnClickListener { showPreviousPage() } }


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


// Yükleme ekranını gizleme
            hideLoadingScreen()


        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Geçersiz sayfa numarası.", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun getAyetlerBySureId(sureId: Int): List<Ayet> {
        return quranItems?.flatMap { it.surahList } // Tüm sayfalardaki sureleri al
            ?.filter { it.sureId == sureId } // Sadece seçili sureyi al
            ?.flatMap { it.ayetList } // Tüm ayetleri birleştir
            ?: emptyList()
    }

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


    private fun showLoadingScreen() {
        // Yükleniyor ekranını oluştur
        loadingLayout!!.visibility = View.VISIBLE
    }

    private fun hideLoadingScreen() {
        loadingLayout!!.visibility = View.INVISIBLE
    }


    private fun downloadAudioFileOnebyOne(sureNumara: Int) {
        val progressDialog = CustomProgressDialog(this@Quran)
            .setTitle("Başlık")
            .setMessage("İndirme İlerlemesi")
            .setMaxProgress(100)
            .setIndeterminate(false)
            .setCancelable(false)
        progressDialog.show()
        val progressBar = progressDialog.progressBar
        progressBar?.visibility = View.VISIBLE


        val yuzde = progressDialog.progressTextView
        yuzde?.visibility = View.VISIBLE

        val cancel = progressDialog.cancelButton


        //  cancel.setVisibility(View.VISIBLE);
        if (storagePermissionGranted) {
            val storage = FirebaseStorage.getInstance()

            val storageRef = storage.reference
            val appFolder = File(filesDir, "SureVeDualar")
            if (!appFolder.exists()) {
                appFolder.mkdirs() // Klasörü oluşturun (eğer yoksa)
            }


            // İndirme işlemi
            val audioRef = storageRef.child("Kuran Sureleri/$sureNumara.mp3")

            val localFile = File(appFolder, "$sureNumara.mp3")
            val downloadTask: StorageTask<FileDownloadTask.TaskSnapshot> =
                audioRef.getFile(localFile)

            if (localFile.exists()) {
                Toast.makeText(this, "Dosya zaten mevcut!", Toast.LENGTH_SHORT).show()
                progressBar?.visibility = View.INVISIBLE
            } else {
                downloadTask.addOnProgressListener { taskSnapshot: FileDownloadTask.TaskSnapshot ->
                    progressBar?.visibility =
                        View.VISIBLE
                    val bytesDownloaded = taskSnapshot.bytesTransferred
                    val totalBytes = taskSnapshot.totalByteCount
                    val progress = ((100 * bytesDownloaded) / totalBytes).toInt()

                    // İlerleme durumunu ProgressBar'a güncelle
                    progressBar?.progress = progress
                    yuzde?.text = "%$progress"
                    cancel?.setOnClickListener {
                        if (downloadTask != null && !downloadTask.isComplete) {
                            downloadTask.cancel()
                            progressBar?.visibility = View.INVISIBLE
                            if (localFile.exists()) {
                                localFile.delete()
                            }
                            // İptal edildiğinde kullanıcıya geribildirim vermek isteyebilirsiniz
                            Toast.makeText(
                                applicationContext,
                                "İndirme işlemi iptal edildi",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()
                        }
                    }
                }

                // İndirme başarılı olduğunda
                downloadTask.addOnSuccessListener { taskSnapshot: FileDownloadTask.TaskSnapshot? ->
                    // İndirme işlemi başarıyla tamamlandı
                    Toast.makeText(this, "İndirme tamamlandı", Toast.LENGTH_SHORT)
                        .show()
                    progressDialog.dismiss()
                    progressBar?.visibility = View.INVISIBLE
                }

                // İndirme başarısız olduğunda
                downloadTask.addOnFailureListener { exception: Exception ->
                    // İndirme işlemi başarısız oldu
                    Toast.makeText(
                        this,
                        "İndirme işlemi başarısız oldu: " + exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.dismiss()
                }
                downloadTask.addOnCanceledListener {
                    // İndirme işlemi iptal edildi
                    progressBar?.visibility = View.INVISIBLE
                    Toast.makeText(this, "İndirme iptal edildi", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            if (progressBar != null) {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun downloadAudioFilesAll() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder = NotificationCompat.Builder(this, "download_channel")
            .setSmallIcon(R.drawable.baseline_cloud_download_24)
            .setContentTitle("İndirme İlerlemesi")
            .setContentText("İndirme başlatıldı...")
            .setPriority(NotificationCompat.PRIORITY_LOW) // Bildirimin önceliği
            .setOngoing(true)
        val progressDialog = CustomProgressDialog(this@Quran)
            .setTitle("Başlık")
            .setMessage("İndirme İlerlemesi")
            .setMaxProgress(100)
            .setIndeterminate(false)
            .setCancelable(false)
        progressDialog.show()
        val progressBar = progressDialog.progressBar
        val yuzde = progressDialog.progressTextView
        val downloadFiles = progressDialog.downloadFiles

        progressBar?.visibility = View.VISIBLE

        val cancel = progressDialog.cancelButton


        if (storagePermissionGranted) {
            val sureNumaralari = surahIdsFromJson
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val appFolder = File(filesDir, "SureVeDualar")
            if (!appFolder.exists()) {
                appFolder.mkdirs()
            }

            if (progressBar != null && yuzde != null && cancel != null) {
                downloadNextAudioFile(
                    sureNumaralari,
                    storageRef,
                    appFolder,
                    progressBar,
                    yuzde,
                    cancel,
                    progressDialog
                )

            }
            downloadFiles?.let {
                updateProgressTextView(sureNumaralari, currentDownloadIndex, it)
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // İzin isteme penceresini göster
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATION
                )
            }
            notificationBuilder?.build()?.let {
                notificationManager.notify(1, it)
            }
        }
    }

    private fun updateProgressTextView(
        sureNumaralari: List<Int>,
        currentDownloadIndex: Int,
        yuzde: TextView
    ) {
        // İndirilen dosyaların sayısını ve toplam dosya sayısını göster

        val progressText = "İndiriliyor: " + currentDownloadIndex + "/" + sureNumaralari.size
        yuzde.text = progressText
    }

    private fun downloadNextAudioFile(
        sureNumaralari: List<Int>,
        storageRef: StorageReference,
        appFolder: File,
        progressBar: ProgressBar,
        yuzde: TextView,
        cancel: ImageButton,
        progressDialog: CustomProgressDialog
    ) {
        val downloadFiles = progressDialog.downloadFiles
        if (currentDownloadIndex < sureNumaralari.size) {
            val sureNo = sureNumaralari[currentDownloadIndex]
            val localFile = File(appFolder, "$sureNo.mp3")

            if (localFile.exists()) {
                currentDownloadIndex++

                downloadNextAudioFile(
                    sureNumaralari,
                    storageRef,
                    appFolder,
                    progressBar,
                    yuzde,
                    cancel,
                    progressDialog
                )
                if (downloadFiles != null) {
                    updateProgressTextView(sureNumaralari, currentDownloadIndex, downloadFiles)
                }
                return
            }

            val audioRef = storageRef.child("Kuran Sureleri/$sureNo.mp3")
            val downloadTask: StorageTask<FileDownloadTask.TaskSnapshot> =
                audioRef.getFile(localFile)

            downloadTask.addOnProgressListener { taskSnapshot: FileDownloadTask.TaskSnapshot ->
                progressBar.visibility =
                    View.VISIBLE
                val bytesDownloaded = taskSnapshot.bytesTransferred
                val totalBytes = taskSnapshot.totalByteCount
                val progress = ((100 * bytesDownloaded) / totalBytes).toInt()
                progressBar.progress = progress
                yuzde.text = "%$progress"
                cancel.setOnClickListener { v: View? ->
                    downloadTask.pause()
                    AlertDialog.Builder(this)
                        .setTitle("İptal Onayı!!!")
                        .setMessage("Tüm indirme işlemini durdurmak istediğinize emin misiniz?")
                        .setPositiveButton(
                            "Evet"
                        ) { dialog: DialogInterface?, which: Int ->
                            // Kullanıcı onaylarsa Firebase'i başlatın ve ses dosyasını indirin
                            if (downloadTask != null && !downloadTask.isComplete) {
                                downloadTask.cancel()
                                progressBar.visibility = View.INVISIBLE
                                if (localFile.exists()) {
                                    localFile.delete()
                                }
                                Toast.makeText(
                                    applicationContext,
                                    "İndirme işlemi iptal edildi",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressDialog.dismiss()
                            }
                        }
                        .setNegativeButton("Hayır") { dialog: DialogInterface?, which: Int ->
                            Toast.makeText(
                                applicationContext,
                                "İndirme işlemi  devam ediyor.",
                                Toast.LENGTH_SHORT
                            ).show()
                            downloadTask.resume()
                        }
                        .show()
                }
            }

            downloadTask.addOnSuccessListener { taskSnapshot: FileDownloadTask.TaskSnapshot? ->
                Toast.makeText(this, "İndirme tamamlandı", Toast.LENGTH_SHORT).show()
                if (!localFile.exists()) {
                    try {
                        localFile.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                progressBar.visibility = View.INVISIBLE
                currentDownloadIndex++

                downloadNextAudioFile(
                    sureNumaralari,
                    storageRef,
                    appFolder,
                    progressBar,
                    yuzde,
                    cancel,
                    progressDialog
                )
                if (downloadFiles != null) {
                    updateProgressTextView(sureNumaralari, currentDownloadIndex, downloadFiles)
                }
                if (currentDownloadIndex == sureNumaralari.size) {
                    Toast.makeText(this, "İndirme tamamlandı. ", Toast.LENGTH_SHORT)
                        .show()
                    AlertDialog.Builder(this)
                        .setTitle("Kapat!!!")
                        .setMessage("Kapatmak için tıklayın")
                        .setPositiveButton(
                            "Evet"
                        ) { dialog: DialogInterface?, which: Int ->
                            // Kullanıcı onaylarsa Firebase'i başlatın ve ses dosyasını indirin
                            progressDialog.dismiss()
                        }
                        .show()
                }
            }

            downloadTask.addOnFailureListener { exception: Exception ->
                // İndirme işlemi başarısız oldu
                Toast.makeText(
                    this,
                    "İndirme işlemi başarısız oldu: " + exception.message,
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
                currentDownloadIndex++

                downloadNextAudioFile(
                    sureNumaralari,
                    storageRef,
                    appFolder,
                    progressBar,
                    yuzde,
                    cancel,
                    progressDialog
                )
                if (downloadFiles != null) {
                    updateProgressTextView(sureNumaralari, currentDownloadIndex, downloadFiles)
                }
            }

            downloadTask.addOnCanceledListener {
                // İndirme işlemi iptal edildi
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, "İndirme iptal edildi", Toast.LENGTH_SHORT)
                    .show()
                progressDialog.dismiss()
            }
        }
    }

    fun getAudioFile(sureNumara: Int): Uri? {
        // List<String> sureAdlari = getSurahNamesFromJson();

        val appFolder = File(filesDir, "SureVeDualar")


        val localFile = File(appFolder, "$sureNumara.mp3")

        if (localFile.exists()) {
            return Uri.fromFile(localFile) // Dosya zaten varsa dosya yolunu döndür

            // Log.d("FilePath", "File Path: " + localFile.getAbsolutePath());
        } else {
            Log.d("FilePath", "File Path: " + localFile.absolutePath)
            return null
        }
    }

    val isInternetAvailable: Boolean
        get() {
            val connectivityManager =
                getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.releaseMediaPlayer()
        executorService!!.shutdown()
    }


    // JSON'dan sure adlarını ve sayfa numaralarını al
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

    inner class MarginUpdater(private val params: MarginLayoutParams) {
        var marginEnd: Int
            get() = params.rightMargin
            set(value) {
                params.rightMargin = value
                sidePanel!!.layoutParams = params
            }
    } // JSON dosyasını okuma


    companion object {
        //  CustomProgressDialog progressDialogC;
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private const val REQUEST_POST_NOTIFICATION = 2

        fun loadJsonFromAssets(context: Context, filename: String): String? {
            var json: String? = null
            try {
                val `is` = context.assets.open(filename)
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                json = String(buffer, StandardCharsets.UTF_8)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return json
        }
    }
}