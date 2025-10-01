package com.example.grocerlypartners.repository.local

import com.example.grocerlypartners.room.dao.BusinessDao
import com.example.grocerlypartners.room.entity.BusinessUiStateEntity
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class BusinessLocalRepoImpl @Inject constructor(private val businessDao: BusinessDao) {
    suspend fun clearAll() = businessDao.clearAll()

    suspend fun getBusinessDataById(id: String) = businessDao.getBusinessDataById(id)

     suspend fun getAllBusinessData() = businessDao.getAllFilter()

    suspend fun upsertBusinessEntity(entity: BusinessUiStateEntity){
        businessDao.upsertBusinessEntity(entity)
    }

}