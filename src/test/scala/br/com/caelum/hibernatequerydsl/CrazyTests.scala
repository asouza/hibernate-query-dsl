package br.com.caelum.hibernatequerydsl

import scala.reflect.{ LocalValue,Ident,Method, Function, Apply, Select, Literal, Tree, This }
import scala.reflect.Code
import scala.reflect.Code._
import PimpedSession._

object CrazyTests {
	
  def mywhile[T](condition: => Boolean)(block: => T) {
	  if (condition) {
	 	  block
	 	  
	 	  mywhile(condition)(block)
	  }
  }
  
  class X {
	  val y = ""
  }
  
  var address = new Address
  
  def main(args: Array[String]): Unit = {
	var user = new User
	var fn = (a:Any) => a 
	user.setName("alberto")

	
    //println(lift(user2.getAge).tree)    
    //println(exp(user2.getAge).tree)
  }

}
	

