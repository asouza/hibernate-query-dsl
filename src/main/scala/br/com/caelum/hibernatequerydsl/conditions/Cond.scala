package br.com.caelum.hibernatequerydsl.conditions

import net.sf.cglib.proxy.Enhancer
import org.hibernate.criterion.{Criterion, Restrictions}
import scala.collection.mutable.Map

object Cond {
  def applyRule[T](f: (T) => Cond)(implicit entityType:Manifest[T]):Cond = {
    val handler = new ComparisonCallback
    val proxy = Enhancer.create(entityType.erasure, handler).asInstanceOf[T]
    f(proxy)
  }
}

trait Cond {
  def crit:Criterion
  def content(id:Int):String
  def params(id:Int):Map[String, Any]
  def ||(g:Cond) = new Or(this, g)
}

/**
 * Adds a clause or disjunction to the query.
 */
class Or(f:Cond, g:Cond) extends Cond {
  def crit = Restrictions.disjunction().add(f.crit).add(g.crit)
  def content(id:Int) = "(" + f.content(id) + ") or (" + g.content(id*100) + ")"
  def params(id:Int) = f.params(id) ++ g.params(id*100)
}

/**
 * Checks that a field is equal to a specific value,
 */
class EqCond(field:String, value:Any) extends Cond {
  def crit = Restrictions.eq(field, value)
  def content(id:Int) = field + " = :" + field + id
  def params(id:Int) = Map((field+id) -> value)

}

/**
 * Checks that a field is null.
 */
class IsNull(field:String) extends Cond {
  def crit = Restrictions.isNull(field)
  def content(id:Int) = field + " is null"
  def params(id:Int) = Map()
}

object Hacker {
  private val _field = new ThreadLocal[String]();
  def field = {
    val x = _field.get
    _field.remove
    x
  }
  def uses(x:String) {
    _field.set(x)
  }
}





