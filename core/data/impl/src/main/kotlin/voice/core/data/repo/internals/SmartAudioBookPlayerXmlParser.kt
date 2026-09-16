package voice.core.data.repo.internals

import org.w3c.dom.Element
import voice.core.data.ListeningStatistic
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

public object SmartAudioBookPlayerXmlParser {

  public fun parse(xmlContent: String): List<ListeningStatistic> {
    val results = mutableListOf<ListeningStatistic>()
    if (xmlContent.isBlank()) return results

    try {
      val factory = DocumentBuilderFactory.newInstance()
      factory.isNamespaceAware = false
      val builder = factory.newDocumentBuilder()
      val doc = builder.parse(ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8)))
      val bookNodes = doc.getElementsByTagName("book")

      for (i in 0 until bookNodes.length) {
        val bookElement = bookNodes.item(i) as? Element ?: continue
        val pathNodes = bookElement.getElementsByTagName("path")
        val path = if (pathNodes.length > 0) pathNodes.item(0).textContent?.trim() ?: "" else ""
        if (path.isEmpty()) continue

        val title = cleanTitleFromPath(path)
        val timeNodes = bookElement.getElementsByTagName("time")

        for (j in 0 until timeNodes.length) {
          val text = timeNodes.item(j).textContent?.trim() ?: ""
          if (text.isNotEmpty()) {
            val parts = text.split("\\s+".toRegex())
            if (parts.size >= 2) {
              val yearMonth = parts[0].trim()
              val seconds = parts[1].trim().toLongOrNull() ?: 0L
              if (yearMonth.isNotEmpty() && seconds > 0L) {
                results.add(
                  ListeningStatistic(
                    bookTitle = title,
                    yearMonth = yearMonth,
                    durationSeconds = seconds,
                  ),
                )
              }
            }
          }
        }
      }
    } catch (_: Exception) {
      // Ignored - return any parsed statistics so far or empty list
    }

    return results
  }

  private fun cleanTitleFromPath(rawPath: String): String {
    val normalized = rawPath.replace('\\', '/').trimEnd('/')
    val lastSegment = normalized.substringAfterLast('/')
    return lastSegment.ifBlank { rawPath }
  }
}
