package com.example.northbridge.db

import androidx.room.TypeConverter
import com.example.northbridge.model.*
import java.math.BigDecimal

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole) = value.name
    @TypeConverter
    fun toUserRole(value: String) = UserRole.valueOf(value)

    @TypeConverter
    fun fromEventCategory(value: EventCategory) = value.name
    @TypeConverter
    fun toEventCategory(value: String) = EventCategory.valueOf(value)

    @TypeConverter
    fun fromEventStatus(value: EventStatus) = value.name
    @TypeConverter
    fun toEventStatus(value: String) = EventStatus.valueOf(value)

    @TypeConverter
    fun fromSourceType(value: SourceType) = value.name
    @TypeConverter
    fun toSourceType(value: String) = SourceType.valueOf(value)

    @TypeConverter
    fun fromFlagType(value: FlagType) = value.name
    @TypeConverter
    fun toFlagType(value: String) = FlagType.valueOf(value)

    @TypeConverter
    fun fromFlagStatus(value: FlagStatus) = value.name
    @TypeConverter
    fun toFlagStatus(value: String) = FlagStatus.valueOf(value)

    @TypeConverter
    fun fromInstrumentType(value: InstrumentType) = value.name
    @TypeConverter
    fun toInstrumentType(value: String) = InstrumentType.valueOf(value)

    @TypeConverter
    fun fromHedgeStatus(value: HedgeStatus) = value.name
    @TypeConverter
    fun toHedgeStatus(value: String) = HedgeStatus.valueOf(value)

    @TypeConverter
    fun fromAuditAction(value: AuditAction) = value.name
    @TypeConverter
    fun toAuditAction(value: String) = AuditAction.valueOf(value)

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toString()
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
}
