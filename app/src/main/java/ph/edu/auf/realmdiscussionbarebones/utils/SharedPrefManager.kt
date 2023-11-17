package ph.edu.auf.realmdiscussionbarebones.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {
    private const val PREFERENCE_NAME = "MyAppPreferences"
    private lateinit var application: Application

    private val sharedPreferences : SharedPreferences by lazy {
        application.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value : String){
        sharedPreferences.edit().putString(key,value).apply()
    }

    fun getString(key:String, default: String): String{
        return sharedPreferences.getString(key,default) ?: default
    }

    fun removeString(key:String){
        sharedPreferences.edit().remove(key).apply()
    }
}