package com.example.namaz.ui.Kuran_ve_ayet.Sure

import android.util.Log

class SureVeAyet // Yapıcı metod
    (
    val ayetNumarasi: String, var ayetMetni: String, // Birden fazla meal için liste kullanıyoruz.
    var ayetMeali: List<String>
) {
    fun getMealMetni(index: Int): String {
        if (index >= 0 && index < ayetMeali.size) {
            return ayetMeali[index] // Belirtilen indekse göre meal döndür
        } else {
            Log.e(
                "SureVeAyet",
                "Geçersiz index: " + index + ", Meallerin sayısı: " + ayetMeali.size
            )
            return "" // Geçersiz index durumunda boş bir string döndür
        }
    }
}
