package app

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LogRegModelSpec extends AnyFlatSpec with Matchers {

  private val testModel = LogRegModel(Vector(1f, 1f, 1f), 0)

  it should "calculate score" in {
    val testVector = Vector(1f, 1f, 1f)
    assert(testModel.getScore(testVector) - 0.95257413 < 0.0001)

  }

  it should "throw exception if size not match" in {
    val testVector = Vector(3f)

    the[IllegalArgumentException] thrownBy {
      testModel.getScore(testVector)
    } should have message "requirement failed: Sample and weights size not match"

  }

}
