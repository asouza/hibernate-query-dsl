package br.com.caelum.hibernatequerydsl

import scala.reflect.{Method,Function,Apply,Select}
object TesteDoido {
  def main(args: Array[String]): Unit = {
    def codeOf[T](c: scala.reflect.Code[() => T]): scala.reflect.Tree = c.tree
    var user = new User()
    var endereco = new Address
    var function = codeOf((() => endereco.getUser)).asInstanceOf[Function]
    println(function)
    var apply = function.copy$default$2.asInstanceOf[Apply]
    var select = apply.copy$default$1.asInstanceOf[Select]
    println(select)
    println(select.sym.name)
  }
}
