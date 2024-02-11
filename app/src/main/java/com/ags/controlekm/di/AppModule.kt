package com.ags.controlekm.di

import android.content.Context
import com.ags.controlekm.database.AppDatabase
import com.ags.controlekm.database.daos.AddressDao
import com.ags.controlekm.database.daos.AddressDao_Impl
import com.ags.controlekm.database.daos.CompanyDao
import com.ags.controlekm.database.daos.CurrentUserDao
import com.ags.controlekm.database.daos.ServiceDao
import com.ags.controlekm.database.daos.UserDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun appDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    @Provides
    @Singleton
    fun userDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
    @Provides
    @Singleton
    fun currentUserDao(appDatabase: AppDatabase): CurrentUserDao {
        return appDatabase.currentUserDao()
    }
    @Provides
    @Singleton
    fun addressDao(appDatabase: AppDatabase): AddressDao {
        return appDatabase.addressDao()
    }
    @Provides
    @Singleton
    fun serviceDao(appDatabase: AppDatabase): ServiceDao {
        return appDatabase.serviceDao()
    }
    @Provides
    @Singleton
    fun companyDao(appDatabase: AppDatabase): CompanyDao {
        return appDatabase.companyDao()
    }
}