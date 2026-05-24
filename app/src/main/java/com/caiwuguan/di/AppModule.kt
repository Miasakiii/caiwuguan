package com.caiwuguan.di

import android.content.Context
import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.data.db.dao.BudgetDao
import com.caiwuguan.data.db.dao.LedgerDao
import com.caiwuguan.data.db.dao.MerchantCategoryDao
import com.caiwuguan.data.db.dao.MonthlyStatsDao
import com.caiwuguan.data.db.database.AppDatabase
import com.caiwuguan.data.parser.CategoryClassifier
import com.caiwuguan.data.parser.ParserRegistry
import com.caiwuguan.data.parser.WechatParser
import com.caiwuguan.data.parser.AlipayParser
import com.caiwuguan.data.parser.BankAppParser
import com.caiwuguan.data.permission.PermissionManager
import com.caiwuguan.data.prefs.UserPrefs
import com.caiwuguan.data.repository.BillRepositoryImpl
import com.caiwuguan.data.repository.BudgetRepositoryImpl
import com.caiwuguan.data.repository.LedgerRepositoryImpl
import com.caiwuguan.data.repository.MerchantCategoryRepositoryImpl
import com.caiwuguan.data.repository.MonthlyStatsRepositoryImpl
import com.caiwuguan.domain.model.Deduplicator
import com.caiwuguan.domain.repository.BillRepository
import com.caiwuguan.domain.repository.BudgetRepository
import com.caiwuguan.domain.repository.LedgerRepository
import com.caiwuguan.domain.repository.MerchantCategoryRepository
import com.caiwuguan.domain.repository.MonthlyStatsRepository
import com.caiwuguan.domain.usecase.AddBillUseCase
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
    fun provideBillDao(db: AppDatabase): BillDao = db.billDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideLedgerDao(db: AppDatabase): LedgerDao = db.ledgerDao()

    @Provides
    fun provideMerchantCategoryDao(db: AppDatabase): MerchantCategoryDao = db.merchantCategoryDao()

    @Provides
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

    @Provides
    @Singleton
    fun provideUserPrefs(@ApplicationContext context: Context): UserPrefs =
        UserPrefs(context)

    @Provides
    @Singleton
    fun provideCategoryClassifier(): CategoryClassifier =
        CategoryClassifier()

    @Provides
    @Singleton
    fun provideParserRegistry(
        wechatParser: WechatParser,
        alipayParser: AlipayParser,
        bankAppParser: BankAppParser,
        categoryClassifier: CategoryClassifier
    ): ParserRegistry = ParserRegistry(wechatParser, alipayParser, bankAppParser, categoryClassifier)

    @Provides
    @Singleton
    fun provideDeduplicator(dao: BillDao): Deduplicator =
        Deduplicator(dao)

    @Provides
    @Singleton
    fun provideWechatParser(): WechatParser = WechatParser()

    @Provides
    @Singleton
    fun provideAlipayParser(): AlipayParser = AlipayParser()

    @Provides
    @Singleton
    fun provideBankAppParser(): BankAppParser = BankAppParser()
    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager =
        PermissionManager(context)

    @Provides
    @Singleton
    fun provideAddBillUseCase(
        billRepository: BillRepository,
        merchantCategoryRepository: MerchantCategoryRepository,
        categoryClassifier: CategoryClassifier,
        aiHelper: com.caiwuguan.ai.AiHelper,
        deduplicator: Deduplicator
    ): AddBillUseCase = AddBillUseCase(
        billRepository,
        merchantCategoryRepository,
        categoryClassifier,
        aiHelper,
        deduplicator
    )
}
