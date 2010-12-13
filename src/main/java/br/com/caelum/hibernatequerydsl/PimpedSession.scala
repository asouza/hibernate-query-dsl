package br.com.caelum.hibernatequerydsl

import PimpedSession._
import org.hibernate.{Criteria, Session}
import org.hibernate.criterion.{Order, Criterion,Restrictions,MatchMode}
import org.hibernate.criterion.Projections._

object PimpedSession {
  implicit def session2PimpedSession(session: Session) = new PimpedSession(session)

  implicit def criteria2PimpedCriteria(criteria: Criteria) = new PimpedCriteria(criteria)
  
  implicit def string2PimpedStringCondition(field:String) = new PimpedStringCondition(field)
}

class PimpedStringCondition(field:String) {
	def equal(value:Object) = Restrictions.eq(field,value)
	
	def >(value:Object) = Restrictions.gt(field,value)
	
	def >=(value:Object) = Restrictions.ge(field,value)
	
	def <(value:Object) = Restrictions.lt(field,value)
	
	def <=(value:Object) = Restrictions.le(field,value)

	def like(value:String) = Restrictions.ilike(field,value,MatchMode.ANYWHERE)
}

class PimpedSession(session: Session) {
    
  def all[T](klass: Class[T]) = session.createCriteria(klass).asList[T];
  
  def from(klass:Class[_]) = session.createCriteria(klass)
 
  def count(klass:Class[_]) = session.createCriteria(klass).count

  def exists(klass: Class[_]) = count(klass) > 0

  def first[T](klass: Class[T]) = session.createCriteria(klass).first[T]

  def last[T](klass: Class[T]) = session.createCriteria(klass).last[T]
}

class PimpedCriteria(criteria: Criteria) {
  def unique[T]: T = criteria.uniqueResult.asInstanceOf[T]

  def asList[T]: java.util.List[T] = criteria.list.asInstanceOf[java.util.List[T]]
  
  def desc(property:String) = criteria.addOrder(Order.desc(property))
  
  def asc(property:String) = criteria.addOrder(Order.asc(property))
  
  def join(property:String) = {
	  criteria.createAlias(property,property)
	  criteria
  }
  
  def where(condition:Criterion) = criteria.add(condition)
  
  def and(condition:Criterion) = criteria.add(condition)
      
  def count = criteria.setProjection(rowCount).uniqueResult.asInstanceOf[Long].longValue
  
  def first[T] = criteria.setFirstResult(0).setMaxResults(1).unique[T]

  def last[T] = {
	  val list = criteria.asList[T]	  	  
	  list.get(list.size-1).asInstanceOf[T]	 	   
  }  
  
}