package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy.Enhancer

object TestingTest {
  def main(args: Array[String]): Unit = {
    var handler = new InvocationMemorizingCallback

    var proxy = Enhancer.create(classOf[User], handler).asInstanceOf[User]
  }
}

