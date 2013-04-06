import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import scalaz._, Scalaz._

case class Person(name: String, age: Int)

class GaedsSpec extends WordSpec with MustMatchers {
  "Lenser macro" must {
    "generate Lens" in {
      val p = Person("Hoge", 13)
      val n = Lenser.lens[Person].name
      val a = Lenser.lens[Person].age
      n.set(p, "Fuga") must be === Person("Fuga", 13)
      a.get(p) must be === 13
    }
  }
}
