package com.sb.mycriptoanalisi.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sb.mycriptoanalisi.data.PortafoglioRepositoryLocale

class SettingsViewModelFactory(
    private val app: Application,
    private val backupRepo: PortafoglioRepositoryLocale,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(app, backupRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
