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
    //a little "mangue". I tried to test instanceOf[java.lang.Boolean] and scala.Boolean and the return was false...
    if(method.getReturnType.getName.eq("boolean")) new Boolean("false") else null
  }
}



