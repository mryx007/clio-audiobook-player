package de.clio.core.scanner

import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.clio.core.logging.api.LogWriter
import de.clio.core.logging.api.Logger
import de.clio.core.scanner.matroska.MatroskaCoverExtractor
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MatroskaCoverExtractorTest {

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @Test
  fun mka() {
    val coverExtractor = MatroskaCoverExtractor(ApplicationProvider.getApplicationContext())
    val testFile = File(javaClass.classLoader!!.getResource("mka_with_cover.mka")!!.file)
    val coverFile = temporaryFolder.newFile("cover.jpg")
    val extracted = coverExtractor.extract(
      input = testFile.toUri(),
      outputFile = coverFile,
    )
    assertTrue(extracted)
    assertTrue(coverFile.isFile)
  }

  companion object {

    @BeforeClass
    @JvmStatic
    fun setup() {
      Logger.install(
        object : LogWriter {
          override fun log(
            severity: Logger.Severity,
            message: String,
            throwable: Throwable?,
          ) {
            println(
              buildString {
                append("${severity.name}: ")
                append(message)
                if (throwable != null) {
                  append(", $throwable")
                }
              },
            )
          }
        },
      )
    }
  }
}
