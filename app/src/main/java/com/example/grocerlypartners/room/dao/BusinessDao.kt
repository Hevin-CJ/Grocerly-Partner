package com.example.grocerlypartners.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.grocerlypartners.room.entity.BusinessUiStateEntity
import com.example.grocerlypartners.utils.Constants.BUSINESS_TABLE
import kotlinx.coroutines.flow.Flow


@Dao
interface BusinessDao {


    @Upsert
    suspend fun upsertBusinessEntity(entity: BusinessUiStateEntity)

    @Query("SELECT * FROM $BUSINESS_TABLE ORDER BY filterId DESC LIMIT 1")
    suspend fun getAllFilter():BusinessUiStateEntity?

    @Query("SELECT * FROM $BUSINESS_TABLE WHERE filterId = :id")
   suspend fun getBusinessDataById(id: String):BusinessUiStateEntity?

    @Query("DELETE FROM $BUSINESS_TABLE")
    suspend fun clearAll()

}