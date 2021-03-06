package org.jetbrains.plugins.scala.failed.typeInference

import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.junit.experimental.categories.Category

/**
  * @author Roman.Shein
  * @since 31.03.2016.
  */
@Category(Array(classOf[PerfCycleTests]))
class MixinTypeTest extends ScalaLightCodeInsightFixtureTestAdapter{

  override protected def shouldPass: Boolean = false

  def testSCL13112() = {
    val text =
      """
        |trait A
        |trait B
        |trait C[T]
        |
        |object X {
        |  def f(x : C[A with B]) : C[B with A] = x
        |}
      """.stripMargin
    checkTextHasNoErrors(text)
  }
}
