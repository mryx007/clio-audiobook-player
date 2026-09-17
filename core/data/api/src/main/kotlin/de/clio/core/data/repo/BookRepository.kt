package de.clio.core.data.repo

import kotlinx.coroutines.flow.Flow
import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.BookId

public interface BookRepository {

  public fun flow(): Flow<List<Book>>

  public suspend fun all(): List<Book>

  public fun flow(id: BookId): Flow<Book?>

  public suspend fun get(id: BookId): Book?

  public suspend fun updateBook(
    id: BookId,
    update: (BookContent) -> BookContent,
  )
}
