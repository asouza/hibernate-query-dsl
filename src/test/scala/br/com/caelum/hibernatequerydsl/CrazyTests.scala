package br.com.caelum.hibernatequerydsl

import scala.reflect.{ LocalValue,Ident,Method, Function, Apply, Select, Literal, Tree, This }
import scala.reflect.Code
import scala.reflect.Code._
import PimpedSession._

object CrazyTests {
	
  def meuwhile[T](condition: => Boolean)(block: => T) {
	  if (condition) {
	 	  block
	 	  
	 	  meuwhile(condition)(block)
	  }
  }
  
  
  var endereco = new Address
  
  def main(args: Array[String]): Unit = {
	var user = new User
	user.setName("alberto")
	
	def exp[T](fn:(User) => T) {
		println(lift(fn(new User)).tree)
	}
    
    println(exp(_.getName))
    //println(lift(user2.getAge).tree)    
    //println(exp(user2.getAge).tree)
  }

}
	

