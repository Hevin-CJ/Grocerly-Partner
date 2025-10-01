package com.example.grocerlypartners.di

import android.content.Context
import androidx.room.Room
import com.example.grocerlypartners.room.dao.BusinessDao
import com.example.grocerlypartners.room.database.GrocerlyDatabase
import com.example.grocerlypartners.utils.Constants.BUSINESS_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGrocerlyDatabase(@ApplicationContext  context: Context): GrocerlyDatabase{
        return Room.databaseBuilder(context, GrocerlyDatabase::class.java,BUSINESS_DATABASE).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideBusinessDao(grocerlyDatabase: GrocerlyDatabase): BusinessDao{
        return grocerlyDatabase.businessDao()
    }



}