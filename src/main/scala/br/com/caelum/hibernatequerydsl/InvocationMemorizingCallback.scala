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

  def invokedPath = properties.mkString(".")

  def invoke(proxy: AnyRef, method: java.lang.reflect.Method, args: Array[AnyRef]) = {
    _invoked = method.getName
    val GetterExpression = """(get|is)?(\w*){1}""".r
    _invoked match {
      case GetterExpression(_, part2) => {
        properties = part2.withFirstCharLowered :: properties
      }
    }
    val rest = if (_invoked.length() > 0) _invoked.substring(1,_invoked.length()) else ""
    _invoked = Character.toLowerCase(_invoked.charAt(0)) + rest
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
