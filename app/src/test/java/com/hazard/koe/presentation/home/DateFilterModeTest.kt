package com.hazard.koe.presentation.home

import com.hazard.koe.domain.model.HomeDateFilterPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateFilterModeTest {

    @Test
    fun toPersistablePresetOrNull_returnsPreset_forTodayWeekMonth() {
        assertEquals(HomeDateFilterPreset.TODAY, DateFilterMode.Today.toPersistablePresetOrNull())
        assertEquals(HomeDateFilterPreset.WEEK, DateFilterMode.Week.toPersistablePresetOrNull())
        assertEquals(HomeDateFilterPreset.MONTH, DateFilterMode.Month.toPersistablePresetOrNull())
    }

    @Test
    fun toPersistablePresetOrNull_returnsNull_forSpecificDateAndRange() {
        assertNull(DateFilterMode.SpecificDate(LocalDate.of(2026, 1, 10)).toPersistablePresetOrNull())
        assertNull(
            DateFilterMode.DateRange(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 1, 31)
            ).toPersistablePresetOrNull()
        )
    }

    @Test
    fun fromPreset_mapsToExpectedDateFilterMode() {
        assertEquals(DateFilterMode.Today, DateFilterMode.fromPreset(HomeDateFilterPreset.TODAY))
        assertEquals(DateFilterMode.Week, DateFilterMode.fromPreset(HomeDateFilterPreset.WEEK))
        assertEquals(DateFilterMode.Month, DateFilterMode.fromPreset(HomeDateFilterPreset.MONTH))
    }
}
