package io.github.halilozel1903.wearkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CurvedTextLayoutTest {

    private val eps = 0.001f

    @Test
    fun `bezel positions use drawArc angles`() {
        assertEquals(270f, BezelPosition.Top.anchorDegrees)
        assertEquals(90f, BezelPosition.Bottom.anchorDegrees)
        assertFalse(BezelPosition.Bottom.readsClockwise)
        assertTrue(BezelPosition.Top.readsClockwise)
    }

    @Test
    fun `clock and dial positions`() {
        assertEquals(270f, CurvedTextLayout.clockPositionDegrees(12f))
        assertEquals(0f, CurvedTextLayout.clockPositionDegrees(3f))
        assertEquals(90f, CurvedTextLayout.clockPositionDegrees(6f))
        assertEquals(270f, CurvedTextLayout.dialDegrees(0f))
        assertEquals(0f, CurvedTextLayout.dialDegrees(15f))
        assertEquals(180f, CurvedTextLayout.dialDegrees(45f))
        assertEquals(350f, CurvedTextLayout.normalize(-10f))
        assertEquals(10f, CurvedTextLayout.normalize(730f))
    }

    @Test
    fun `glyphs are centered on the anchor`() {
        val radius = (180f / Math.PI).toFloat() // 1 unit of width = 1 degree
        val clockwise = CurvedTextLayout.glyphAngles(listOf(10f, 10f, 10f), radius, anchorDegrees = 270f)
        assertEquals(listOf(260f, 270f, 280f), clockwise.map { Math.round(it * 1000) / 1000f })
        val bottom = CurvedTextLayout.glyphAngles(listOf(10f, 20f), radius, anchorDegrees = 90f, clockwise = false)
        // Total 30 degrees centered on 90, running counter clockwise: first glyph at 90 + 10.
        assertEquals(100f, bottom[0], eps)
        assertEquals(85f, bottom[1], eps)
        assertTrue(CurvedTextLayout.glyphAngles(emptyList(), radius, 0f).isEmpty())
    }

    @Test
    fun `text sweep and fitting`() {
        val radius = (180f / Math.PI).toFloat()
        assertEquals(45f, CurvedTextLayout.sweepDegrees(45f, radius), eps)
        assertTrue(CurvedTextLayout.fits(45f, radius, maxSweepDegrees = 60f))
        assertFalse(CurvedTextLayout.fits(90f, radius, maxSweepDegrees = 60f))
    }

    @Test
    fun `ellipsize shortens until the text fits`() {
        val radius = (180f / Math.PI).toFloat()
        val measure: (String) -> Float = { it.length * 10f }
        assertEquals("SHORT", CurvedTextLayout.ellipsize("SHORT", radius, 60f, measure = measure))
        assertEquals("HELLO…", CurvedTextLayout.ellipsize("HELLO WORLD", radius, 60f, measure = measure))
        assertEquals("", CurvedTextLayout.ellipsize("HELLO", radius, 5f, measure = measure))
    }

    @Test
    fun `even angles and points on the circle`() {
        assertEquals(listOf(270f, 0f, 90f, 180f), CurvedTextLayout.evenAngles(4))
        val top = CurvedTextLayout.pointAt(270f, 100f)
        assertEquals(0f, top.x, eps)
        assertEquals(-100f, top.y, eps)
        val right = CurvedTextLayout.pointAt(0f, 50f)
        assertEquals(50f, right.x, eps)
        assertEquals(0f, right.y, eps)
    }
}
