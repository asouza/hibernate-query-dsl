package br.com.caelum.hibernatequerydsl

class TestingTest {

}
object TestingTest {
  def main(args: Array[String]): Unit = {
    import br.com.caelum.hibernatequerydsl.Expression._
    import br.com.caelum.hibernatequerydsl.PimpedSession._
    //    println(exp[Address].getUser.getName)
    println(exp[Address].getUser.getAge)
  }
}

