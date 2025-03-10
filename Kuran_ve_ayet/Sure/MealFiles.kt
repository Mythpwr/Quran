package com.example.namaz.ui.Kuran_ve_ayet.Sure

// MealFiles.java
class MealFiles(val meal1: String, val meal2: String, val meal3: String) {
    fun getMeal(index: Int): String {
        return when (index) {
            0 -> meal1
            1 -> meal2
            2 -> meal3
            else -> meal1
        }
    }
}
