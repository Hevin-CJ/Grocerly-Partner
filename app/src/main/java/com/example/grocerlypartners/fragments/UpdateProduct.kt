package com.example.grocerlypartners.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.grocerlypartners.R
import com.example.grocerlypartners.databinding.FragmentUpdateProductBinding
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.utils.Constants.PARTNERS
import com.example.grocerlypartners.utils.LoadingDialogue
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.PackUp
import com.example.grocerlypartners.utils.ProductCategory
import com.example.grocerlypartners.utils.ProductValidation
import com.example.grocerlypartners.utils.QuantityType
import com.example.grocerlypartners.viewmodel.UpdateProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UpdateProduct : Fragment() {
    private var updateProduct:FragmentUpdateProductBinding?=null
    private val binding get() = updateProduct!!


    private val updateNavArgs by navArgs<UpdateProductArgs>()

    private val updateProductViewModel: UpdateProductViewModel by viewModels()

    private lateinit var loadingDialogue: LoadingDialogue

    private var selectedImage:String?=null

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        if (uri!=null){
            updateProductViewModel.uploadImageToFirebase(uri)
            Log.d("imageurigot",uri.toString())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       updateProduct = FragmentUpdateProductBinding.inflate(inflater,container,false)
        loadingDialogue = LoadingDialogue(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCategoriesSpinner()
        updateProduct()
        getImageFromStorage()
        observeUpdatingProduct()
        observeUpdatingProductValidation()
        observeImageUploadState()
        setUpPackUpTime()
        setUpQuantityType()
        setDefaultDataToView()
    }

    private fun observeUpdatingProductValidation() {
       lifecycleScope.launch {
           updateProductViewModel.productValidationState.collect{state->
               if (state.product is ProductValidation.failure){
                  Toast.makeText(requireContext(),state.product.message, Toast.LENGTH_SHORT).show()
               }
           }
       }
    }

    private fun observeImageUploadState() {
        lifecycleScope.launch {
            updateProductViewModel.uploadImageState.collectLatest {
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

    private fun observeUpdatingProduct() {
        updateProductViewModel.updateProduct.observe(viewLifecycleOwner){result->
            when(result){
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(),result.message,Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading ->{
                    loadingDialogue.show()
                }
                is NetworkResult.Success -> {
                   findNavController().navigate(R.id.action_updateProduct_to_products)
                    loadingDialogue.dismiss()
                    Toast.makeText(requireContext(),result.data,Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.UnSpecified -> {


                }
            }
        }
    }

    private fun updateProduct() {
        binding.apply {
            updatebtn.setOnClickListener {
                val itemName = edttextname.text.toString().trim()
                val itemPrice = edttextprice.text.toString().trim().toIntOrNull()
                val originalPrice = edttextpriceoriginal.text.toString().trim().toIntOrNull()
                val maxQuantity = edttextmaxquantity.text.toString().trim().toIntOrNull()
                val imageUri = selectedImage
                val quantityTypeSelectedItems = getQuantityType(quantityTypeSpinner.selectedItem.toString())
                val categorySelectedItem =  getCategoryType(CategorySpinner.selectedItem.toString())
                val packingSelectedTime = getPackUpType(packupSpinner.selectedItem.toString())
               val product = Product(productId = updateNavArgs.updateProduct.productId, partnerId = updateNavArgs.updateProduct.partnerId, image = imageUri, itemName = itemName, itemPrice = itemPrice, itemOriginalPrice = originalPrice, maxQuantity = maxQuantity, category = categorySelectedItem, quantityType = quantityTypeSelectedItems, packUpTime = packingSelectedTime)
                updateProductViewModel.updateDataIntoFirebase(product)

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

    private fun setDefaultDataToView() {
        binding.apply {
            val product = updateNavArgs.updateProduct
            edttextname.setText(product.itemName)
            edttextprice.setText(product.itemPrice.toString())
            edttextpriceoriginal.setText(product.itemOriginalPrice.toString())
            edttextmaxquantity.setText(product.maxQuantity.toString())
            Glide.with(imgviewitemimg.context).load(product.image).placeholder(R.drawable.weebly_image_sample).into(imgviewitemimg)
            selectedImage = product.image
            val categoryIndex = ProductCategory.entries.map { it.displayName }.indexOf(product.category.displayName)
            CategorySpinner.setSelection(categoryIndex)

            val qtyIndex = QuantityType.entries.map { it.displayName }.indexOf(product.quantityType.displayName)
            quantityTypeSpinner.setSelection(qtyIndex)
            packupSpinner.setSelection(product.packUpTime.ordinal)
            Log.d("packuptime",product.packUpTime.ordinal.toString())
        }
    }


    private fun setUpCategoriesSpinner() {
        val categoryItems = ProductCategory.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryItems)
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

    private fun getImageFromStorage() {
        binding.apply {
            imgviewitemimg.setOnClickListener {
                galleryLauncher.launch("image/*")
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        updateProduct=null
    }

}