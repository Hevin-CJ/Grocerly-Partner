package com.example.grocerlypartners.preferences

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.grocerlypartners.R
import com.example.grocerlypartners.utils.DashboardFilter
import com.google.firebase.firestore.remote.Datastore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.datastore by  preferencesDataStore("GROCERLY_PARTNERS")

class GrocerlyDataStore( @ApplicationContext context: Context) {

    private val datastore = context.datastore

    companion object{
        val isLoggedIn = booleanPreferencesKey("IsLogged")

        val BUSINESS_FILTER_STATE_KEY = stringPreferencesKey("business_filter_state")
    }

    suspend fun setLoginState(islogin:Boolean){
        datastore.edit {prefs->
            prefs[isLoggedIn] = islogin
        }
    }


    fun getBusinessFilterState(): Flow<Pair<DashboardFilter, Int>> {
        return datastore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                val savedString = prefs[BUSINESS_FILTER_STATE_KEY]
                deserializeFilterState(savedString)
            }
    }

    private fun deserializeFilterState(savedString: String?): Pair<DashboardFilter, Int> {
        if (savedString.isNullOrEmpty()) {
            return Pair(DashboardFilter.Today, R.id.todaybtn)
        }
        return try {
            val parts = savedString.split(":")
            val type = parts[0]
            val buttonId = parts.last().toInt()

            val filter = when (type) {
                "TODAY" -> DashboardFilter.Today
                "YESTERDAY" -> DashboardFilter.Yesterday
                "WEEK" -> DashboardFilter.Week
                "MONTH" -> DashboardFilter.Month
                "CUSTOM" -> DashboardFilter.Custom(parts[1].toLong(), parts[2].toLong())
                else -> DashboardFilter.Today
            }
            Pair(filter, buttonId)
        } catch (e: Exception) {
            Pair(DashboardFilter.Today, R.id.todaybtn)
        }
    }



    suspend fun setBusinessFilterState(filter: DashboardFilter, buttonId: Int) {
        datastore.edit { prefs ->
            prefs[BUSINESS_FILTER_STATE_KEY] = serializeFilterState(filter, buttonId)
        }
    }

    suspend fun resetBusinessFilter() {
        datastore.edit { prefs ->
            prefs.remove(BUSINESS_FILTER_STATE_KEY)
        }
    }

    private fun serializeFilterState(filter: DashboardFilter, buttonId: Int): String {
        return when (filter) {
            is DashboardFilter.Today -> "TODAY::$buttonId"
            is DashboardFilter.Yesterday -> "YESTERDAY::$buttonId"
            is DashboardFilter.Week -> "WEEK::$buttonId"
            is DashboardFilter.Month -> "MONTH::$buttonId"
            is DashboardFilter.Custom -> "CUSTOM:${filter.startTime}:${filter.endTime}:$buttonId"
        }
    }




    fun getLoginState():Flow<Boolean>{
        return datastore.data
            .catch {exception->
                if (exception is IOException){
                    emit(emptyPreferences())
                }else{
                    throw exception
                }
            }
            .map {prefs->
               val loginState =  prefs[isLoggedIn] ?: false
                 loginState
            }
    }


}