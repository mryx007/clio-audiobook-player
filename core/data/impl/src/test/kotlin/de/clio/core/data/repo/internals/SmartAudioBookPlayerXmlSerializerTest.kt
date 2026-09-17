package de.clio.core.data.repo.internals

import de.clio.core.data.ListeningStatistic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartAudioBookPlayerXmlSerializerTest {

  @Test
  fun `serializes statistics to valid xml`() {
    val stats = listOf(
      ListeningStatistic(
        bookTitle = "Meteor",
        yearMonth = "2025-06",
        durationSeconds = 87423L,
      ),
      ListeningStatistic(
        bookTitle = "Meteor",
        yearMonth = "2025-07",
        durationSeconds = 5000L,
      ),
    )

    val xml = SmartAudioBookPlayerXmlSerializer.serialize(stats)
    assertTrue(xml.contains("<path>Meteor</path>"))
    assertTrue(xml.contains("<time>2025-06 87423</time>"))
    assertTrue(xml.contains("<time>2025-07 5000</time>"))

    // Roundtrip parse check
    val parsed = SmartAudioBookPlayerXmlParser.parse(xml)
    assertEquals(2, parsed.size)
    assertEquals("Meteor", parsed[0].bookTitle)
    assertEquals("2025-06", parsed[0].yearMonth)
    assertEquals(87423L, parsed[0].durationSeconds)
  }
}
