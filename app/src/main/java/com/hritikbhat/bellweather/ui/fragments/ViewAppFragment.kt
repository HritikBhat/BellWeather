package com.hritikbhat.bellweather.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.hritikbhat.bellweather.data.db.AppDatabase
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.data.viewmodel.MainViewModel
import com.hritikbhat.bellweather.data.viewmodel.MainViewModelFactory
import com.hritikbhat.bellweather.databinding.FragmentViewAppBinding
import kotlinx.coroutines.launch

class ViewAppFragment : Fragment() {
    private lateinit var binding: FragmentViewAppBinding
    private lateinit var appData: AppTable
    private lateinit var appSettingTable: AppSettingTable
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_app, container, false)

        val viewModelFactory = MainViewModelFactory(AppDatabase(binding.root.context).getAppDao())
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        val secondFragmentArgs: ViewAppFragmentArgs =
            ViewAppFragmentArgs.fromBundle(requireArguments())

        appData = secondFragmentArgs.appData

        mainViewModel.viewModelScope.launch {
            mainViewModel.getAppSetting(appData.aId).collect { appSetting ->
            appSettingTable=appSetting
            binding.notificationToggleSwitch.isChecked = appSettingTable.isNotificationEnabled==1
            binding.smsToggleSwitch.isChecked = appSettingTable.isSMSEnabled==1
            binding.ringSpeechToggleBtn.isChecked = appSettingTable.ringORSpeech==1
        }
        }

        if(appData.appImg!=null){
            binding.appIconIV.setImageBitmap(appData.appImg)
        }
        binding.appNameTV.text = appData.aName

        binding.smsToggleSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.smsToggleSwitch.isChecked){
                appSettingTable.isSMSEnabled=1
            }
            else{
                appSettingTable.isSMSEnabled=0
            }
            Log.d("SMS Toggled To","${binding.smsToggleSwitch.isChecked} and ${appSettingTable.isSMSEnabled}")

            mainViewModel.viewModelScope.launch {
                mainViewModel.updateAppSetting(appSettingTable)
            }
        }

        binding.notificationToggleSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.notificationToggleSwitch.isChecked){
                appSettingTable.isNotificationEnabled=1
            }
            else{
                appSettingTable.isNotificationEnabled=0
            }
            Log.d("Notifcation Toggled To","${binding.notificationToggleSwitch.isChecked} and ${appSettingTable.isNotificationEnabled}")
            mainViewModel.viewModelScope.launch {
                mainViewModel.updateAppSetting(appSettingTable)
            }
        }

        binding.deleteAppBtn.setOnClickListener {
            mainViewModel.viewModelScope.launch {
                mainViewModel.removeApp(appData)
                Toast.makeText(requireContext(),"App Alert Removed!",Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }

        binding.editKeywordBtn.setOnClickListener {
            val action =
                ViewAppFragmentDirections.actionViewAppFragmentToAppKeywordFragment()
            action.aid = appData.aId
            findNavController().navigate(action)
        }


        binding.ringSpeechToggleBtn.setOnCheckedChangeListener { _, _ ->
            if (binding.ringSpeechToggleBtn.isChecked){
                appSettingTable.ringORSpeech=1
            }
            else{
                appSettingTable.ringORSpeech=0
            }
            Log.d("Notifcation Toggled To","${binding.notificationToggleSwitch.isChecked} and ${appSettingTable.isNotificationEnabled}")
            mainViewModel.viewModelScope.launch {
                mainViewModel.updateAppSetting(appSettingTable)
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}