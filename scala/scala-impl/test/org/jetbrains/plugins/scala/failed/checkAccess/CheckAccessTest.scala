package org.jetbrains.plugins.scala.failed.checkAccess

import com.intellij.testFramework.fixtures.CodeInsightTestFixture.CARET_MARKER
import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.failed.annotator.BadCodeGreenTestBase
import org.junit.experimental.categories.Category

/**
  * User: Dmitry.Naydanov
  * Date: 22.03.16.
  */
@Category(Array(classOf[PerfCycleTests]))
class CheckAccessTest extends BadCodeGreenTestBase {
  override def shouldPass: Boolean = false

  def testSCL9212() = {
    val text =
      s"""
         |class A(name:String) {
         |  def f(a: A) = a.${CARET_MARKER}name
         |}
      """.stripMargin
    doTest(text)
  }
}
