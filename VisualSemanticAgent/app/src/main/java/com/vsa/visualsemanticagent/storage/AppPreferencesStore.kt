package com.vsa.visualsemanticagent.storage

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vsa.visualsemanticagent.plan.AgendaCardData
import com.vsa.visualsemanticagent.plan.AgendaReminderData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferencesStore(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { appContext.preferencesDataStoreFile(DATASTORE_NAME) }
    )
    private val gson = Gson()

    val stateFlow: Flow<AppStoredState> = dataStore.data.map { preferences ->
        AppStoredState(
            agendaItems = decodeAgendas(preferences[KEY_AGENDAS_JSON]),
            reminderLeadMinutes = preferences[KEY_REMINDER_LEAD_MINUTES] ?: DEFAULT_REMINDER_LEAD_MINUTES,
            reminderDayEnabled = preferences[KEY_REMINDER_DAY_ENABLED] ?: true,
            reminderHourEnabled = preferences[KEY_REMINDER_HOUR_ENABLED] ?: true,
            blockHighRisk = preferences[KEY_BLOCK_HIGH_RISK] ?: true,
            muteLowConfidence = preferences[KEY_MUTE_LOW_CONFIDENCE] ?: false,
            autoMapLink = preferences[KEY_AUTO_MAP_LINK] ?: true
        )
    }

    suspend fun saveAgendaItems(items: List<AgendaCardData>) {
        dataStore.edit { preferences ->
            preferences[KEY_AGENDAS_JSON] = gson.toJson(items)
        }
    }

    suspend fun savePreferences(
        reminderLeadMinutes: Int,
        reminderDayEnabled: Boolean,
        reminderHourEnabled: Boolean,
        blockHighRisk: Boolean,
        muteLowConfidence: Boolean,
        autoMapLink: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_REMINDER_LEAD_MINUTES] = reminderLeadMinutes
            preferences[KEY_REMINDER_DAY_ENABLED] = reminderDayEnabled
            preferences[KEY_REMINDER_HOUR_ENABLED] = reminderHourEnabled
            preferences[KEY_BLOCK_HIGH_RISK] = blockHighRisk
            preferences[KEY_MUTE_LOW_CONFIDENCE] = muteLowConfidence
            preferences[KEY_AUTO_MAP_LINK] = autoMapLink
        }
    }

    private fun decodeAgendas(raw: String?): List<AgendaCardData> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<AgendaCardDataRaw>>() {}.type
            val parsed: List<AgendaCardDataRaw> = gson.fromJson(raw, type)
            parsed.map { it.toAgendaCardData() }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val DATASTORE_NAME = "timeweaver_prefs.preferences_pb"
        private val KEY_AGENDAS_JSON = stringPreferencesKey("agendas_json")
        private val KEY_REMINDER_LEAD_MINUTES = intPreferencesKey("reminder_lead_minutes")
        private val KEY_REMINDER_DAY_ENABLED = booleanPreferencesKey("reminder_day_enabled")
        private val KEY_REMINDER_HOUR_ENABLED = booleanPreferencesKey("reminder_hour_enabled")
        private val KEY_BLOCK_HIGH_RISK = booleanPreferencesKey("block_high_risk")
        private val KEY_MUTE_LOW_CONFIDENCE = booleanPreferencesKey("mute_low_confidence")
        private val KEY_AUTO_MAP_LINK = booleanPreferencesKey("auto_map_link")
        const val DEFAULT_REMINDER_LEAD_MINUTES = 60
    }
}

data class AppStoredState(
    val agendaItems: List<AgendaCardData> = emptyList(),
    val reminderLeadMinutes: Int = AppPreferencesStore.DEFAULT_REMINDER_LEAD_MINUTES,
    val reminderDayEnabled: Boolean = true,
    val reminderHourEnabled: Boolean = true,
    val blockHighRisk: Boolean = true,
    val muteLowConfidence: Boolean = false,
    val autoMapLink: Boolean = true
)

private data class AgendaCardDataRaw(
    val id: String,
    val title: String,
    val summary: String,
    val time: String,
    val location: String,
    val status: String,
    val isoDateTime: String? = null,
    val sourceLabel: String = "",
    val action: String = "create_event",
    val reminders: List<AgendaReminderDataRaw> = emptyList()
) {
    fun toAgendaCardData(): AgendaCardData {
        return AgendaCardData(
            id = id,
            title = title,
            summary = summary,
            time = time,
            location = location,
            status = status,
            isoDateTime = isoDateTime,
            sourceLabel = sourceLabel,
            action = action,
            reminders = reminders.map { AgendaReminderData(label = it.label, minutesBefore = it.minutesBefore) }
        )
    }
}

private data class AgendaReminderDataRaw(
    val label: String,
    val minutesBefore: Int
)
