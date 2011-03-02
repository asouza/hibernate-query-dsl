package br.com.caelum.hibernatequerydsl


import scala.reflect.{ Method, Function, Apply, Select, Literal, Tree,This }
import scala.reflect.Code
import scala.reflect.Code._
import PimpedSession._

object CrazyTests {


	var user = new User
	var endereco = new Address
  def main(args: Array[String]): Unit = {
    var user2 = new User
//Apply(Select(Select(This(Class(br.com.caelum.hibernatequerydsl.PimpedClassTest)),Method(br.com.caelum.hibernatequerydsl.PimpedClassTest.userToQuery,PolyType(List(),List(),PrefixedType(ThisType(Class(br.com.caelum.hibernatequerydsl)),Class(br.com.caelum.hibernatequerydsl.User))))),Method(br.com.caelum.hibernatequerydsl.User.getName,MethodType(List(),PrefixedType(ThisType(Class(java.lang)),Class(java.lang.String))))),List())
//    println(lift(user2.getName).tree)
//	println(lift(user.getName).tree)
//    println(lift(user.getName).tree.asInstanceOf[Apply].fun.asInstanceOf[Select].qual.asInstanceOf[Select].qual.asInstanceOf[This].sym)
    println(lift(user2.getName).desc)
    println(lift(endereco.getUser.getName).desc)
  }

}


