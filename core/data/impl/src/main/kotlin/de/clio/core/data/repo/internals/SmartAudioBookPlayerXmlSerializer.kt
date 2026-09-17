package de.clio.core.data.repo.internals

import de.clio.core.data.ListeningStatistic

public object SmartAudioBookPlayerXmlSerializer {

  public fun serialize(statistics: List<ListeningStatistic>): String {
    val grouped = statistics.groupBy { it.bookTitle }
    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>\n")
    for ((bookTitle, stats) in grouped) {
      sb.append("    <book>\n")
      sb.append("        <path>").append(escapeXml(bookTitle)).append("</path>\n")
      val monthlyGrouped = stats.groupBy { it.yearMonth }
      for ((yearMonth, monthStats) in monthlyGrouped.toSortedMap()) {
        val totalSec = monthStats.sumOf { it.durationSeconds }
        if (totalSec > 0) {
          sb.append("        <time>").append(yearMonth).append(" ").append(totalSec).append("</time>\n")
        }
      }
      sb.append("    </book>\n")
    }
    sb.append("</root>")
    return sb.toString()
  }

  private fun escapeXml(text: String): String {
    return text.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;")
  }
}
