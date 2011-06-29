package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy.InvocationHandler
class ComparisonCallback extends InvocationHandler {

  def invoke(proxy:AnyRef,method:java.lang.reflect.Method,args:Array[AnyRef]) = {
    // we can also use it with a SINGLE proxy per class by doing a list.an[User].getName
    // TODO switch to case or something else
    // TODO duplicated code, extract
    var _invoked = method.getName
        if(_invoked.startsWith("get")) {
      _invoked = _invoked.substring(3, _invoked.length)
    } else if(_invoked.startsWith("is")) {
      _invoked = _invoked.substring(2, _invoked.length)
    }
    val rest = if (_invoked.length() > 0) _invoked.substring(1,_invoked.length()) else ""
    _invoked = Character.toLowerCase(_invoked.charAt(0)) + rest
    Hacker.uses(_invoked)
    null
  }

}
