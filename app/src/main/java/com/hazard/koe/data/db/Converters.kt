package com.hazard.koe.data.db

import androidx.room.TypeConverter
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.BudgetPeriodType
import com.hazard.koe.data.enums.CardNetwork
import com.hazard.koe.data.enums.CasualLoanTxnType
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.FrequencyType
import com.hazard.koe.data.enums.LoanDirection
import com.hazard.koe.data.enums.PaymentStatus
import com.hazard.koe.data.enums.RecurringType
import com.hazard.koe.data.enums.TransactionType

class Converters {

    @TypeConverter
    fun fromTransactionType(v: TransactionType): String = v.name

    @TypeConverter
    fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)

    @TypeConverter
    fun fromAccountType(v: AccountType): String = v.name

    @TypeConverter
    fun toAccountType(v: String): AccountType = AccountType.valueOf(v)

    @TypeConverter
    fun fromCardNetwork(v: CardNetwork?): String? = v?.name

    @TypeConverter
    fun toCardNetwork(v: String?): CardNetwork? = v?.let { CardNetwork.valueOf(it) }

    @TypeConverter
    fun fromCategoryType(v: CategoryType): String = v.name

    @TypeConverter
    fun toCategoryType(v: String): CategoryType = CategoryType.valueOf(v)

    @TypeConverter
    fun fromRecurringType(v: RecurringType): String = v.name

    @TypeConverter
    fun toRecurringType(v: String): RecurringType = RecurringType.valueOf(v)

    @TypeConverter
    fun fromFrequencyType(v: FrequencyType): String = v.name

    @TypeConverter
    fun toFrequencyType(v: String): FrequencyType = FrequencyType.valueOf(v)

    @TypeConverter
    fun fromBudgetPeriodType(v: BudgetPeriodType): String = v.name

    @TypeConverter
    fun toBudgetPeriodType(v: String): BudgetPeriodType = BudgetPeriodType.valueOf(v)

    @TypeConverter
    fun fromLoanDirection(v: LoanDirection): String = v.name

    @TypeConverter
    fun toLoanDirection(v: String): LoanDirection = LoanDirection.valueOf(v)

    @TypeConverter
    fun fromCasualLoanTxnType(v: CasualLoanTxnType): String = v.name

    @TypeConverter
    fun toCasualLoanTxnType(v: String): CasualLoanTxnType = CasualLoanTxnType.valueOf(v)

    @TypeConverter
    fun fromPaymentStatus(v: PaymentStatus): String = v.name

    @TypeConverter
    fun toPaymentStatus(v: String): PaymentStatus = PaymentStatus.valueOf(v)
}
