package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy.InvocationHandler
import org.hibernate.criterion.Restrictions
import java.lang.reflect.Method

class InvocationMemorizingCallback extends InvocationHandler {
  private var _invoked: String = ""

  def invokedPath = _invoked

  def invoke(proxy: AnyRef, method: java.lang.reflect.Method, args: Array[AnyRef]) = {
    _invoked = method.getName
    // TODO switch to case or something else
    if (_invoked.startsWith("get")) {
      _invoked = _invoked.substring(3, _invoked.length)
    } else if (_invoked.startsWith("is")) {
      _invoked = _invoked.substring(2, _invoked.length)
    }
    val rest = if (_invoked.length() > 0) _invoked.substring(1, _invoked.length()) else ""
    _invoked = Character.toLowerCase(_invoked.charAt(0)) + rest
    null
  }
}

object Pimps {
  implicit def xxx(qq:Any) = new PimpedCriteriaCondition(target)
}

class PimpedCriteriaCondition(target:Any){
  val proxy = target.asInstanceOf[InvocationMemorizingCallback]
  def \==(value:Any) = {
    println(proxy.invokedPath+"aaaa")
    Restrictions.eq(proxy.invokedPath,value)
  }
}

