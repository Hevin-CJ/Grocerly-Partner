package com.example.grocerlypartners.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.grocerlypartners.R
import com.example.grocerlypartners.databinding.FragmentAddProductBinding
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.LoadingDialogue
import com.example.grocerlypartners.utils.NetworkUtils
import com.example.grocerlypartners.utils.PackUp
import com.example.grocerlypartners.utils.ProductCategory
import com.example.grocerlypartners.utils.ProductValidation
import com.example.grocerlypartners.utils.QuantityType
import com.example.grocerlypartners.viewmodel.AddProductViewModel
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddProduct : Fragment() {
    private var addProduct: FragmentAddProductBinding? = null
    private val binding get() = addProduct!!

    @Inject
    lateinit var db:FirebaseFirestore

    private val addProductViewModel: AddProductViewModel by viewModels()
    private var selectedImage: String? = null

    private lateinit var loadingDialogue: LoadingDialogue

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                addProductViewModel.uploadImageToFirebase(uri)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            selectedImage = null
            binding.imgviewitemimg.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.weebly_image_sample
                )
            )
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addProduct = FragmentAddProductBinding.inflate(inflater, container, false)
        loadingDialogue = LoadingDialogue(requireContext())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCategoriesSpinner()
        uploadDataToFirebase()
        getImageFromStorage()
        observeUploadState()
        observeProductValidationState()
        observeImageUploadState()
        setUpPackUpTime()
        setUpQuantityType()
    }

    private fun observeImageUploadState() {
       lifecycleScope.launch {
           addProductViewModel.uploadImageState.collectLatest {
               when(it){
                   is NetworkResult.Error<*> -> {
                       Toast.makeText(requireContext(),it.message, Toast.LENGTH_SHORT).show()
                       loadingDialogue.dismiss()
                   }
                   is NetworkResult.Loading<*> -> {
                       loadingDialogue.show()
                   }
                   is NetworkResult.Success<*> -> {
                       it.data?.let { image->
                           loadPickedImage(image)
                           selectedImage = image
                       }
                       loadingDialogue.dismiss()
                   }
                   is NetworkResult.UnSpecified<*> ->{
                       loadingDialogue.dismiss()
                   }
               }
           }
       }
    }

    private fun loadPickedImage(url: String) {
        Glide.with(requireContext())
            .load(url)
            .priority(Priority.HIGH)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imgviewitemimg)

    }

    private fun observeProductValidationState() {
        lifecycleScope.launch {
            addProductViewModel.productValState.collect {
                if (it.product is ProductValidation.failure) {
                    Toast.makeText(requireContext(),it.product.message.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun observeUploadState() {

        addProductViewModel.uploadProduct.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }

                is NetworkResult.Loading -> {
                    Toast.makeText(requireContext(), "Loading Please wait", Toast.LENGTH_SHORT)
                        .show()
                }

                is NetworkResult.Success -> {
                    findNavController().navigate(R.id.action_addProduct_to_products)
                    Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                }

                is NetworkResult.UnSpecified -> {

                }
            }

        }
    }

    private fun setUpCategoriesSpinner() {
        val categoryItems = ProductCategory.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.CategorySpinner.adapter = adapter
    }


    private fun setUpPackUpTime(){
        val packUpTime = PackUp.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, packUpTime)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.packupSpinner.adapter = adapter
    }

    private fun setUpQuantityType(){
        val packUpTime = QuantityType.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, packUpTime)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.quantityTypeSpinner.adapter = adapter
    }

    private fun uploadDataToFirebase() {
        binding.apply {
            publishbtn.setOnClickListener {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    val productKey = db.collection(PARTNERS).document().id
                    val itemName = edttextname.text.toString().trim()
                    val itemPrice = edttextprice.text.toString().trim().toIntOrNull()
                    val originalPrice = edttextpriceoriginal.text.toString().trim().toIntOrNull()
                    val maxQuantity = edttextmaxquantity.text.toString().trim().toIntOrNull()
                    val searchKeywords = generateKeywords(itemName)
                    val imageUri = selectedImage
                    val quantityTypeSelectedItems = getQuantityType(quantityTypeSpinner.selectedItem.toString())
                    val categorySelectedItem =  getCategoryType(CategorySpinner.selectedItem.toString())
                    val packingSelectedTime = getPackUpType(packupSpinner.selectedItem.toString())
                    val product = Product(productId = productKey, partnerId = "", image = imageUri, itemName = itemName,itemPrice = itemPrice, itemOriginalPrice = originalPrice, maxQuantity = maxQuantity, quantityType = quantityTypeSelectedItems, searchKeywords = searchKeywords, packUpTime = packingSelectedTime, category = categorySelectedItem)
                    addProductViewModel.uploadProductToFirebase(product)
                }else{
                    Toast.makeText(requireContext(),"Enable wifi/cellular",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getQuantityType(quantityType:String?): QuantityType{
        return QuantityType.entries.find { it.displayName == quantityType } ?: QuantityType.selectQuantity
    }


    private fun getCategoryType(categoryType:String?): ProductCategory{
        return ProductCategory.entries.find { it.displayName == categoryType } ?: ProductCategory.selectcatgory
    }

    private fun getPackUpType( packUpType: String?): PackUp{
        return PackUp.entries.find { it.displayName == packUpType } ?:PackUp.selectTime
    }

    private fun generateKeywords(itemName:String): List<String>{
        val keywords = mutableListOf<String>()

        val words = itemName.lowercase().split(" ").filter { it.isNotBlank() }
        for(word in words){
            keywords.add(word)

            var prefix= ""
            for(char in word){
                prefix+=char
                keywords.add(prefix)
            }
        }
        return keywords.distinct().sortedBy { it.length }
    }



    private fun getImageFromStorage() {
        binding.apply {
            imgviewitemimg.setOnClickListener {
                galleryLauncher.launch("image/*")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addProduct=null
    }

}