package com.example.grocerlypartners.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.grocerlypartners.room.convertor.BusinessClassTypeConvertor
import com.example.grocerlypartners.room.dao.BusinessDao
import com.example.grocerlypartners.room.entity.BusinessUiStateEntity

@Database(entities = [BusinessUiStateEntity::class], version = 2, exportSchema = false)
@TypeConverters(BusinessClassTypeConvertor::class)
abstract class GrocerlyDatabase: RoomDatabase() {
    abstract fun businessDao(): BusinessDao
}