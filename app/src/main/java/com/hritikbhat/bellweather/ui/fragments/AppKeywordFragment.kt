package com.hritikbhat.bellweather.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.data.adapters.KeywordAdapter
import com.hritikbhat.bellweather.data.db.AppDatabase
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.data.db.tables.KeywordTable
import com.hritikbhat.bellweather.data.viewmodel.MainViewModel
import com.hritikbhat.bellweather.data.viewmodel.MainViewModelFactory
import com.hritikbhat.bellweather.databinding.DialogAddKeywordBinding
import com.hritikbhat.bellweather.databinding.FragmentAppKeywordBinding
import kotlinx.coroutines.launch

class AppKeywordFragment : Fragment(),KeywordAdapter.OnKeywordItemClickListener {
    private lateinit var binding: FragmentAppKeywordBinding
    private var aId:Int = -1
    private lateinit var keywordArrayList: ArrayList<KeywordTable>
    private lateinit var keyAdapter: KeywordAdapter
    private lateinit var mainViewModel: MainViewModel

    enum class KeywordLayoutType{
        KEYWORD_LAYOUT,
        NO_KEYWORD_LAYOUT
    }

    private fun setLayoutTo(keywordLayoutType: KeywordLayoutType){
        when(keywordLayoutType){
            KeywordLayoutType.KEYWORD_LAYOUT -> {
                binding.keywordLayout.visibility = View.VISIBLE
                binding.noKeywordLayout.visibility = View.GONE
            }
            KeywordLayoutType.NO_KEYWORD_LAYOUT -> {
                binding.keywordLayout.visibility = View.GONE
                binding.noKeywordLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun checkKeywordSize(){
        if (keywordArrayList.isEmpty()) {
            setLayoutTo(KeywordLayoutType.NO_KEYWORD_LAYOUT)
        } else {
            setLayoutTo(KeywordLayoutType.KEYWORD_LAYOUT)
        }
    }

    // Inside your activity or fragment
    private fun showCustomDialog() {
        val dialogAddKeywordBinding: DialogAddKeywordBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_add_keyword,
            null,
            false
        )



        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddKeywordBinding.root)
            .create()

        dialogAddKeywordBinding.dialogAddKeywordButton.setOnClickListener {
            val inputText = dialogAddKeywordBinding.dialogKeywordEditText.text
            if (inputText.isNotEmpty()) {

                if (!keywordArrayList.any { it.kName.lowercase() == inputText.toString().lowercase() }){
                    val keywordTable = KeywordTable(aId,inputText.toString())
                    mainViewModel.viewModelScope.launch {
                        mainViewModel.addAppKeyword(keywordTable)
                        dialog.dismiss()
                        Toast.makeText(requireContext(),"Keyword Added!", Toast.LENGTH_LONG).show()
                        keywordArrayList.add(keywordTable)
                        checkKeywordSize()
                        keyAdapter.updateInsertedList(keywordArrayList)
                    }

                }
                else{
                    Toast.makeText(requireContext(),"Keyword Already Exist!", Toast.LENGTH_LONG).show()
                }


            }
        }

        dialog.show()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_keyword, container, false)

        val viewModelFactory = MainViewModelFactory(AppDatabase(binding.root.context).getAppDao())
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        val appKeywordFragmentArgs = AppKeywordFragmentArgs.fromBundle(requireArguments())
        aId = appKeywordFragmentArgs.aid
        val keywordAdapterListener = this

        mainViewModel.viewModelScope.launch {
            mainViewModel.getAllAppKeywords(aId).collect { appKeywords ->
                keywordArrayList = appKeywords as ArrayList<KeywordTable>
                checkKeywordSize()
                keyAdapter = KeywordAdapter(keywordArrayList)
                keyAdapter.setOnItemClickListener(keywordAdapterListener)
                val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                binding.keywordRV.layoutManager = layoutManager

                binding.keywordRV.adapter = keyAdapter

                binding.addKeywordBtn.setOnClickListener {
                    showCustomDialog()
                }
            }}



        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Do nothing - we don't want to support moving items up/down
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                mainViewModel.viewModelScope.launch {
                    val position = viewHolder.adapterPosition
                    mainViewModel.deleteAppKeyword(keywordArrayList[position])
                    keywordArrayList.removeAt(position)
                    checkKeywordSize()
                    keyAdapter.updateDeletedItemList(position, keywordArrayList)
                    Toast.makeText(requireContext(), "Keyword Removed!", Toast.LENGTH_LONG).show()
                    Log.d("Position::: ","$position")
                    Log.d("Array::: ", keywordArrayList.toString())
                    Log.d("AdapterArray::: ", keyAdapter.getItems().toString())
                }

            }
        }).attachToRecyclerView(binding.keywordRV)


        return binding.root
    }

    override fun onAppItemClick(keyword: KeywordTable) {

    }
}