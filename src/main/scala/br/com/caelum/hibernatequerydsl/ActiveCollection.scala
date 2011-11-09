package br.com.caelum.hibernatequerydsl

import conditions.Cond
import org.hibernate.criterion.Restrictions

class ActiveCollection[T](var elements:List[T], query:PimpedCriteria[T,T])(implicit entityType:Manifest[T]) {

  import Cond.applyRule
  private type Myself = ActiveCollection[T]
  private type Condition = (T) => Cond
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
    query.using(_.setMaxResults(k))
  }

  def drop(k:Int): Myself = {
    query.using(_.setFirstResult(k))
  }

  def dropWhile(f:(T) => Boolean) = {
    grabThem.dropWhile(f)
  }

  def exists(f: Condition) = find(f).isDefined

  def filter(f: Condition):Myself = {
    query.and(applyRule(f).crit)
  }

  def withFilter(f: Condition):Myself = filter(f)

  def filterNot(f: Condition):Myself = query.and(Restrictions.not(applyRule(f).crit))

  def find(f: Condition): Option[T] = {
    query.and(applyRule(f).crit).using(_.setMaxResults(1)).headOption
  }

  def count(f: Condition) = {
    filter(f)
    query.count
  }

  def head = query.headOption.get
  def tail:List[T] = drop(1).grabThem


  def map[B](f: T => B)(implicit m:Manifest[B]):ActiveCollection[B] = {
    new ActiveCollection[B](null, query.asInstanceOf[PimpedCriteria[B,B]])
  }
}