package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy.Enhancer

object TestingTest {
  def main(args: Array[String]): Unit = {
    import br.com.caelum.hibernatequerydsl.Pimps._
    val handler = new InvocationMemorizingCallback

    val proxy = Enhancer.create(classOf[User], handler).asInstanceOf[User]
    println(proxy.getName.\==("alberto"))
  }
}

