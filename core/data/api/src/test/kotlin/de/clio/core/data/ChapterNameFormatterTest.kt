package de.clio.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

public class ChapterNameFormatterTest {

  @Test
  public fun formatChapterName_keepsFilenameWithTrailingNumber() {
    assertEquals("Goldwäsche 05", formatChapterName("Goldwäsche - 05"))
    assertEquals("Der Bluthund 002", formatChapterName("Der Bluthund - 002"))
    assertEquals("Die Arena 001", formatChapterName("Die_Arena_001"))
    assertEquals("Candice Fox - Crimson Lake 005", formatChapterName("Candice Fox - Crimson Lake - 005"))
    assertEquals("1984 01", formatChapterName("1984 - 01"))
    assertEquals("05", formatChapterName("05"))
  }

  @Test
  public fun formatChapterName_movesLeadingNumberToTheEnd() {
    assertEquals("Dies ist keine Übung 01", formatChapterName("01 Dies ist keine Übung"))
    assertEquals("Dies ist keine Übung 01", formatChapterName("01 - Dies ist keine Übung"))
    assertEquals("Die Hyänen 001", formatChapterName("001 - Die Hyänen"))
    assertEquals("Hitzewelle 02", formatChapterName("02 Hitzewelle"))
    assertEquals("Der Einzelgänger 001", formatChapterName("001 Der Einzelgänger"))
    assertEquals("Die Verwandlung 01", formatChapterName("01_Die_Verwandlung"))
  }

  @Test
  public fun formatChapterName_handlesExplicitKapitelInRest() {
    assertEquals("Kapitel 1", formatChapterName("001 - Kapitel 1"))
  }

  @Test
  public fun resolveChapterName_usesFilenameWhenTitleMatchesAlbumOrBook() {
    assertEquals(
      "Goldwäsche 05",
      resolveChapterName(
        title = "Goldwäsche - Ein Fall für Jack Reacher und Will Trent",
        album = "Goldwäsche - Ein Fall für Jack Reacher und Will Trent",
        fileName = "Goldwäsche - 05",
      ),
    )
    assertEquals(
      "Dies ist keine Übung 01",
      resolveChapterName(
        title = "Dies ist keine Übung",
        album = "Dies ist keine Übung: Eine Jack-Reacher-Story",
        fileName = "01 Dies ist keine Übung",
      ),
    )
    assertEquals(
      "Die Arena 001",
      resolveChapterName(
        title = "Die Arena",
        album = "Die Arena (gelesen von David Nathan)",
        fileName = "Die_Arena_001",
      ),
    )
    assertEquals(
      "Candice Fox - Crimson Lake 005",
      resolveChapterName(
        title = "Crimson Lake: Crimson Lake 1",
        album = "Crimson Lake",
        fileName = "Candice Fox - Crimson Lake - 005",
      ),
    )
    assertEquals(
      "Goldwäsche 05",
      resolveChapterName(
        title = "Goldwäsche - Ein Fall für Jack Reacher und Will Trent",
        album = null,
        fileName = "Goldwäsche - 05",
      ),
    )
  }

  @Test
  public fun resolveChapterName_preservesExplicitChapterTitles() {
    assertEquals(
      "Kapitel 1",
      resolveChapterName(
        title = "Kapitel 1",
        album = "Der Sündenbock",
        fileName = "001 - Kapitel 1",
      ),
    )
    assertEquals(
      "Prolog",
      resolveChapterName(
        title = "Prolog",
        album = "Harry Potter",
        fileName = "01 - Prolog",
      ),
    )
  }

  @Test
  public fun formatChapterName_filtersAuthorPrefix() {
    assertEquals("Crimson Lake 082", formatChapterName("Candice Fox - Crimson Lake 082", "Candice Fox"))
    assertEquals("Crimson Lake 005", formatChapterName("Candice Fox - Crimson Lake - 005", "Candice Fox"))
    assertEquals("Crimson Lake 01", formatChapterName("01 - Candice Fox - Crimson Lake", "Candice Fox"))
    assertEquals("Crimson Lake 01", formatChapterName("Candice Fox - 01 - Crimson Lake", "Candice Fox"))
    assertEquals("Crimson Lake 082", formatChapterName("Candice_Fox_-_Crimson_Lake_082", "Candice Fox"))
    assertEquals("Crimson Lake 082", formatChapterName("Fox, Candice - Crimson Lake 082", "Candice Fox"))
    assertEquals("Crimson Lake 082", formatChapterName("Candice Fox - Crimson Lake 082", "Fox, Candice"))
  }

  @Test
  public fun resolveChapterName_filtersAuthorWhenProvided() {
    assertEquals(
      "Crimson Lake 082",
      resolveChapterName(
        title = "Crimson Lake 1 - Crimson Lake",
        album = "Crimson Lake",
        fileName = "Candice Fox - Crimson Lake 082",
        author = "Candice Fox",
      ),
    )
    assertEquals(
      "Crimson Lake 005",
      resolveChapterName(
        title = "Crimson Lake",
        album = "Crimson Lake",
        fileName = "Candice Fox - Crimson Lake - 005",
        author = "Candice Fox",
      ),
    )
  }
}
