package de.clio.core.data

import de.clio.core.common.comparator.NaturalOrderComparator
import kotlinx.serialization.Serializable

@Serializable
public enum class BookSortOrder : Comparator<Book> {
  BY_LAST_PLAYED {
    override fun compare(a: Book, b: Book): Int =
      b.content.lastPlayedAt.compareTo(a.content.lastPlayedAt)
  },
  BY_ADDED_AT {
    override fun compare(a: Book, b: Book): Int =
      b.content.addedAt.compareTo(a.content.addedAt)
  },
  BY_NAME_ASC {
    override fun compare(a: Book, b: Book): Int =
      NaturalOrderComparator.stringComparator.compare(a.content.name, b.content.name)
  },
  BY_NAME_DESC {
    override fun compare(a: Book, b: Book): Int =
      NaturalOrderComparator.stringComparator.compare(b.content.name, a.content.name)
  },
  BY_AUTHOR {
    override fun compare(a: Book, b: Book): Int {
      val aAuth = a.content.author.orEmpty()
      val bAuth = b.content.author.orEmpty()
      val cmp = NaturalOrderComparator.stringComparator.compare(aAuth, bAuth)
      return if (cmp != 0) cmp else NaturalOrderComparator.stringComparator.compare(a.content.name, b.content.name)
    }
  },
  BY_DURATION_ASC {
    override fun compare(a: Book, b: Book): Int =
      a.duration.compareTo(b.duration)
  },
  BY_DURATION_DESC {
    override fun compare(a: Book, b: Book): Int =
      b.duration.compareTo(a.duration)
  };

  public companion object {
    public val Default: BookSortOrder = BY_LAST_PLAYED
  }
}
