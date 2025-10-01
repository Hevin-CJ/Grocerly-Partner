package com.example.grocerlypartners.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerlypartners.adaptor.OfferAdaptor
import com.example.grocerlypartners.databinding.FragmentOffersBinding
import com.example.grocerlypartners.model.OfferItem
import com.example.grocerlypartners.utils.LoadingDialogue
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.viewmodel.AddOfferViewModel
import com.example.grocerlypartners.viewmodel.OfferViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class Offers : Fragment() {
    private var offers:FragmentOffersBinding?=null
    private val binding get() = offers!!

    private val offerViewModel by viewModels<OfferViewModel>()

    private val addOfferViewModel by viewModels<AddOfferViewModel>()

    private lateinit var loadingDialogue: LoadingDialogue

    private val offerAdaptor:OfferAdaptor by lazy { OfferAdaptor() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        offers = FragmentOffersBinding.inflate(inflater,container,false)
        loadingDialogue = LoadingDialogue(requireContext())
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeOffersFromFirebase()
        setDeleteOffer()
        observeOfferDeletion()
    }

    private fun observeOfferDeletion() {
      viewLifecycleOwner.lifecycleScope.launch {
          offerViewModel.deletedOffer.collectLatest{ result ->
              when (result) {
                  is NetworkResult.Error -> {
                      loadingDialogue.dismiss()
                  }

                  is NetworkResult.Loading -> {
                      loadingDialogue.show()
                  }

                  is NetworkResult.Success -> {
                      loadingDialogue.dismiss()
                      result.data?.let {
                          showSnackBar(it)
                      }
                  }

                  is NetworkResult.UnSpecified -> {
                      loadingDialogue.dismiss()
                  }
              }
          }
      }
    }

    private fun showSnackBar(data: OfferItem) {
        Snackbar.make(requireView(),"Deleted Offer",Snackbar.LENGTH_LONG).setAction("Undo"){
            addOfferViewModel.insertOfferIntoIntoFirebase(data)
            addOfferViewModel.offerAdded.observe(viewLifecycleOwner) { result ->
                if (result is NetworkResult.Success) {
                    offerViewModel.getOfferFromFirebase()
                    loadingDialogue.dismiss()
                }
            }

        }.show()
    }

    private fun setDeleteOffer() {
        val itemTouchCallback = object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val offer = offerAdaptor.getOffer(position)
                offerViewModel.deleteOfferFromFirebase(offer)
            }

        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rcviewoffers)
    }

    private fun observeOffersFromFirebase() {
        viewLifecycleOwner.lifecycleScope.launch {
            offerViewModel.offerItems.collectLatest{result->
                when(result){
                    is NetworkResult.Error -> {
                        loadingDialogue.dismiss()
                        Toast.makeText(requireContext(),result.message,Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Loading -> {
                        loadingDialogue.show()
                    }
                    is NetworkResult.Success ->{
                        loadingDialogue.dismiss()
                        if (result.data.isNullOrEmpty()) {
                            binding.apply {
                                txtviewnooffers.visibility = View.VISIBLE
                                txtviewnoofferboost.visibility = View.VISIBLE
                            }
                        } else {
                            binding.apply {
                                txtviewnooffers.visibility = View.INVISIBLE
                                txtviewnoofferboost.visibility = View.INVISIBLE
                            }
                            setOfferAdaptor(result.data)
                        }
                    }
                    is NetworkResult.UnSpecified -> {
                        loadingDialogue.dismiss()
                    }
                }
            }
        }
    }

    private fun setOfferAdaptor(it: List<OfferItem>) {
        binding.apply {
            rcviewoffers.adapter = offerAdaptor
            rcviewoffers.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            offerAdaptor.setOffers(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offers=null
    }


}