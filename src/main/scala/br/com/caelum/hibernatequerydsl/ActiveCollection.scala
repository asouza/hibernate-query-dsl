package br.com.caelum.hibernatequerydsl

import scala.collection.JavaConversions._
import net.sf.cglib.proxy.Enhancer
import org.hibernate.criterion.{Criterion, Restrictions}

trait Cond {
  def crit:Criterion
  def filter(obj:Any):Boolean
}
class EqCond(field:String, value:Any) extends Cond {
  def crit = Restrictions.eq(field, value)
  def filter(obj:Any) = (true)
}
// TODO extract list interface so we dont need the cases,
// can simply return the list and thats it
class ActiveCollection[T](var elements:List[T], query:PimpedCriteria[T,T])(implicit entityType:Manifest[T]) {

  private type Myself = ActiveCollection[T]
  private def loaded = Option(elements).isDefined

  private implicit def listToAC(l:List[T]):Myself = new Myself(elements, null)
  private implicit def queryToAC(q:PimpedCriteria[T,T]):Myself = new Myself(null, q)

  def grabThem():List[T] = {
    if(!loaded) {
      elements = query.asList[T].toList
    }
    elements
  }

  def take(k: Int):Myself = {
    loaded match {
      case (false) => query.using(_.setMaxResults(k))
      case (true) => new Myself(elements.take(k), null)
    }
  }

  def filter(f: (T) => Cond):Myself = {
    loaded match {
      case (false) => query.and(applyRule(f).crit)
      case (true) => elements.filter(toB(f))
    }
  }

  def find(f: (T) => Cond): Option[T] = {
    loaded match {
      case (false) => query.and(applyRule(f).crit).using(_.setMaxResults(1)).headOption
      case (true) => elements.find(toB(f))
    }
  }
  def toB(f:(T) => Cond):((T) => Boolean) = {
    throw new RuntimeException("going through the list")
  }

  def applyRule(f: (T) => Cond):Cond = {
    val handler = new ComparisonCallback
    val proxy = Enhancer.create(entityType.erasure, handler).asInstanceOf[T]
    f(proxy)
  }

}