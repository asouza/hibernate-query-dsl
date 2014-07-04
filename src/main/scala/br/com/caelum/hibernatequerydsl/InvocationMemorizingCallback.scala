package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy.InvocationHandler
import java.lang.reflect.Method
import java.lang.{ThreadLocal, Boolean}
import org.hibernate.criterion.{Projections, Order, MatchMode, Restrictions}

object Pig{
   val tl = new ThreadLocal[InvocationMemorizingCallback]
}

class StringWithRubyPowers(str: String) {
    def withFirstCharLowered = {
      str.substring(0, 1).toLowerCase + str.substring(1, str.length)
    }
  }


class InvocationMemorizingCallback(val prefix:String = "") extends InvocationHandler {
  
  Pig.tl.set(this)

  implicit def string2WithRubyPowers(str: String) = new StringWithRubyPowers(str)
  private var _invoked: String = ""
  var properties = List[String]()

  def invokedPath = {
    val path = properties.mkString(".")
    properties = List()
    path
  }

  def invoke(proxy: AnyRef, method: java.lang.reflect.Method, args: Array[AnyRef]):AnyRef = {
    if (method.getDeclaringClass == classOf[Object]) {
      return null
    }

    _invoked = method.getName
    val GetterExpression = """(get|is)?(\w*){1}""".r
    _invoked match {
      case GetterExpression(_, part2) => {
        properties = part2.withFirstCharLowered :: properties
      }
    }
    if(method.getReturnType.getName.eq("boolean"))
      new Boolean("false") 
    else if(method.getReturnType==Char.getClass) {
      ' '.asInstanceOf[AnyRef]
    } else if(method.getReturnType.isPrimitive) {
      0.asInstanceOf[AnyRef]
    } else {
      null
    }
  }
}
