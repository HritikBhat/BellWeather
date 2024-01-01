package com.hritikbhat.bellweather.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
import com.hritikbhat.bellweather.ui.fragments.HomeFragment.Companion.appArrayList
import com.hritikbhat.bellweather.ui.fragments.HomeFragment.Companion.isAppArrayListInitialized
import com.hritikbhat.bellweather.databinding.FragmentAddAppBinding
import kotlinx.coroutines.launch


class AddAppFragment : Fragment(), AppAdapter.OnAppItemClickListener {
    private lateinit var binding: FragmentAddAppBinding
    private lateinit var appAdapter: AppAdapter
    private lateinit var mainViewModel: MainViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_app, container, false)


        val appAdapterListener: AppAdapter.OnAppItemClickListener = this

        val viewModelFactory = MainViewModelFactory(AppDatabase(binding.root.context).getAppDao())
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        mainViewModel.viewModelScope.launch {
            mainViewModel.getAllInstalledApps(requireContext().packageManager).collect { apps ->
                appAdapter = AppAdapter(apps)
                appAdapter.setOnItemClickListener(appAdapterListener)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                binding.addAppRV.layoutManager = layoutManager

                binding.addAppRV.adapter = appAdapter

                binding.loadingLayout.visibility = View.GONE
                binding.addAppRV.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    override fun onAppItemClick(app: AppTable) {
        //Add App Logic Here
        if (isAppArrayListInitialized()){
            if (!appArrayList.any { it.aPackageName == app.aPackageName }){
                mainViewModel.viewModelScope.launch {
                    mainViewModel.addApp(app)
                    Toast.makeText(requireContext(),"App Alert Added!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
            else{
                Toast.makeText(requireContext(),"App Already Exist!", Toast.LENGTH_LONG).show()
            }
        }


    }
}