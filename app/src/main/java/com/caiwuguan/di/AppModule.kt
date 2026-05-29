package com.caiwuguan.di

import android.content.Context
import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.data.db.dao.BudgetDao
import com.caiwuguan.data.db.dao.ChatDao
import com.caiwuguan.data.db.dao.LedgerDao
import com.caiwuguan.data.db.dao.MerchantCategoryDao
import com.caiwuguan.data.db.dao.MonthlyStatsDao
import com.caiwuguan.data.db.database.AppDatabase
import com.caiwuguan.data.repository.BillRepositoryImpl
import com.caiwuguan.data.repository.BudgetRepositoryImpl
import com.caiwuguan.data.repository.LedgerRepositoryImpl
import com.caiwuguan.data.repository.MerchantCategoryRepositoryImpl
import com.caiwuguan.data.repository.MonthlyStatsRepositoryImpl
import com.caiwuguan.domain.repository.BillRepository
import com.caiwuguan.domain.repository.BudgetRepository
import com.caiwuguan.domain.repository.LedgerRepository
import com.caiwuguan.domain.repository.MerchantCategoryRepository
import com.caiwuguan.domain.repository.MonthlyStatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideBillDao(db: AppDatabase): BillDao = db.billDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideLedgerDao(db: AppDatabase): LedgerDao = db.ledgerDao()

    @Provides
    @Singleton
    fun provideMerchantCategoryDao(db: AppDatabase): MerchantCategoryDao = db.merchantCategoryDao()

    @Provides
    @Singleton
    fun provideMonthlyStatsDao(db: AppDatabase): MonthlyStatsDao = db.monthlyStatsDao()

    @Provides
    @Singleton
    fun provideBillRepository(dao: BillDao): BillRepository =
        BillRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideBudgetRepository(dao: BudgetDao): BudgetRepository =
        BudgetRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideLedgerRepository(dao: LedgerDao): LedgerRepository =
        LedgerRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMerchantCategoryRepository(dao: MerchantCategoryDao): MerchantCategoryRepository =
        MerchantCategoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMonthlyStatsRepository(dao: MonthlyStatsDao): MonthlyStatsRepository =
        MonthlyStatsRepositoryImpl(dao)

}
