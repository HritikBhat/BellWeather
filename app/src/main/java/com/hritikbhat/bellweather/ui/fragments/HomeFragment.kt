package com.hritikbhat.bellweather.ui.fragments

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hritikbhat.bellweather.data.adapters.AppAdapter
import com.hritikbhat.bellweather.data.db.AppDatabase
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.data.viewmodel.MainViewModel
import com.hritikbhat.bellweather.data.viewmodel.MainViewModelFactory
import com.hritikbhat.bellweather.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), AppAdapter.OnAppItemClickListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var appAdapter: AppAdapter
    private lateinit var mainViewModel: MainViewModel

    companion object{
        lateinit var appArrayList: ArrayList<AppTable>
        fun isAppArrayListInitialized(): Boolean {
            return Companion::appArrayList.isInitialized
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val appAdapterListener: AppAdapter.OnAppItemClickListener = this
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        val viewModelFactory = MainViewModelFactory(AppDatabase(binding.root.context).getAppDao())
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)


        mainViewModel.viewModelScope.launch {
            mainViewModel.getAllApps().collect { apps ->
                appArrayList = apps as ArrayList<AppTable>
                setImageIcons()
                appAdapter = AppAdapter(appArrayList)
                appAdapter.setOnItemClickListener(appAdapterListener)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                binding.appRV.layoutManager = layoutManager

                binding.appRV.adapter = appAdapter
            }
        }


        binding.addAppBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addAppFragment2)
        }

        return binding.root
    }

    private fun setImageIcons() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos: List<ResolveInfo> = requireContext().packageManager.queryIntentActivities(mainIntent, 0)

        for ((position, app) in appArrayList.withIndex()) {
            resolveInfos.any {
                if(it.activityInfo.packageName == app.aPackageName){
                    val appIcon = it.loadIcon(requireContext().packageManager)
                    appArrayList[position].appImg = appIcon.toBitmap()
                    true
                }
                else{
                    false
                }
            }
        }


    }

    override fun onAppItemClick(app: AppTable) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToViewAppFragment(
                app
            )
        findNavController().navigate(action)
    }
}