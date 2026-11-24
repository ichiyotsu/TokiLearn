package com.tokilearn.app

import com.tokilearn.app.data.addWrong
import com.tokilearn.app.data.isChoiceCorrect
import com.tokilearn.app.data.isNumericCorrect
import com.tokilearn.app.data.removeWrong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    @Test fun numericExact() {
        assertTrue(isNumericCorrect(1.0, 1.0, 0.01))
    }

    @Test fun numericWithinTolerance() {
        // db＝40×cos20°≈37.59，容差 ±0.05
        assertTrue(isNumericCorrect(37.6, 37.59, 0.05))
        assertTrue(isNumericCorrect(37.55, 37.59, 0.05))
    }

    @Test fun numericOutsideTolerance() {
        assertFalse(isNumericCorrect(37.7, 37.59, 0.05))
        assertFalse(isNumericCorrect(2.0, 1.0, 0.01))
    }

    @Test fun numericRejectsNonFinite() {
        assertFalse(isNumericCorrect(Double.NaN, 1.0, 0.01))
        assertFalse(isNumericCorrect(Double.POSITIVE_INFINITY, 1.0, 0.01))
    }

    @Test fun choiceGrading() {
        assertTrue(isChoiceCorrect(0, 0))
        assertFalse(isChoiceCorrect(1, 0))
        assertFalse(isChoiceCorrect(-1, 0))
    }

    @Test fun wrongBookAddRemove() {
        val added = addWrong(emptySet(), "md-1-1")
        assertEquals(setOf("md-1-1"), added)
        val addedAgain = addWrong(added, "md-1-1")
        assertEquals(setOf("md-1-1"), addedAgain)
        val removed = removeWrong(addWrong(added, "md-2-1"), "md-1-1")
        assertEquals(setOf("md-2-1"), removed)
    }
}
