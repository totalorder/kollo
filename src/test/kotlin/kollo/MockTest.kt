package kollo

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

data class Box(val contents: String)
class BoxFactory(val contents: String) {
    fun createBox(): Box {
        return Box(contents)
    }
}

class MockTest {
    @Test
    fun boxFactory() {
        val korvBoxFactory = BoxFactory("korv")
        val box = korvBoxFactory.createBox()
        assertEquals("korv", box.contents)
    }

    @Test
    fun mockedBoxFactory() {
        // Kotlin classes are final by default, which means mockito can't usually mock it
        // This only works because this file exists, which enables mocking of final:
        // - src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
        val mockedBoxFactory = mock(BoxFactory::class.java)
        `when`(mockedBoxFactory.createBox()).thenReturn(Box("korv"))
        val box = mockedBoxFactory.createBox()
        assertEquals("korv", box.contents)
    }
}
