package app

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class LogRegModelSpec extends AnyFlatSpec with Matchers {

  private val testModel = LogRegModel(Array(1F, 1F, 1F), 0)

  it should "calculate score" in {
    val testVector = Array(1F, 1F, 1F)
    assert(testModel.getScore(testVector) - 0.95257413 < 0.0001)

  }

  it should "throw exception if size not match" in {
    val testVector = Array(3F)

    the[IllegalArgumentException] thrownBy {
      testModel.getScore(testVector)
    } should have message "requirement failed: Sample and weights size not match"

  }


}
