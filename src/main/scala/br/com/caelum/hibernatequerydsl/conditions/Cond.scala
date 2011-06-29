package br.com.caelum.hibernatequerydsl.conditions

import net.sf.cglib.proxy.Enhancer
import org.hibernate.criterion.{Criterion, Restrictions}
import br.com.caelum.hibernatequerydsl.ComparisonCallback

object Cond {
  def applyRule[T](f: (T) => Cond)(implicit entityType:Manifest[T]):Cond = {
    val handler = new ComparisonCallback
    val proxy = Enhancer.create(entityType.erasure, handler).asInstanceOf[T]
    f(proxy)
  }
}

trait Cond {
  def crit:Criterion
  def content:String
  def params:Pair[String, Any]
}

class EqCond(field:String, value:Any) extends Cond {
  def crit = Restrictions.eq(field, value)
  def content = field + " = :" + field
  def params = field -> value
}




