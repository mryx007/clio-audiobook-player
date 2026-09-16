package voice.core.data.repo.internals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartAudioBookPlayerXmlParserTest {

  @Test
  fun `parses empty xml returns empty list`() {
    val result = SmartAudioBookPlayerXmlParser.parse("")
    assertTrue(result.isEmpty())
  }

  @Test
  fun `parses standard smart audiobook player xml`() {
    val xml = """
      <?xml version="1.0" encoding="UTF-8"?><root>
          <book>
              <path>2017 - Die Grausamen (ungekürzt)</path>
              <time>2022-10 103</time>
              <time>2022-11 4830</time>
              <time>2022-12 31728</time>
          </book>
          <book>
              <path>Audiobooks\Stephen King - Der Dunkle Turm</path>
              <time>2023-01 12000</time>
          </book>
      </root>
    """.trimIndent()

    val results = SmartAudioBookPlayerXmlParser.parse(xml)
    assertEquals(4, results.size)

    assertEquals("2017 - Die Grausamen (ungekürzt)", results[0].bookTitle)
    assertEquals("2022-10", results[0].yearMonth)
    assertEquals(103L, results[0].durationSeconds)

    assertEquals("2017 - Die Grausamen (ungekürzt)", results[1].bookTitle)
    assertEquals("2022-11", results[1].yearMonth)
    assertEquals(4830L, results[1].durationSeconds)

    assertEquals("2017 - Die Grausamen (ungekürzt)", results[2].bookTitle)
    assertEquals("2022-12", results[2].yearMonth)
    assertEquals(31728L, results[2].durationSeconds)

    assertEquals("Stephen King - Der Dunkle Turm", results[3].bookTitle)
    assertEquals("2023-01", results[3].yearMonth)
    assertEquals(12000L, results[3].durationSeconds)
  }
}
