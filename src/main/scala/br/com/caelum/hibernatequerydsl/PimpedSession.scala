package br.com.caelum.hibernatequerydsl

import org.hibernate.transform.Transformers
import org.hibernate.criterion.Projection
import org.hibernate.impl.SessionImpl
import org.hibernate.impl.CriteriaImpl
import java.io.Serializable
import PimpedSession._
import org.hibernate.{ Criteria, Session, Query }
import org.hibernate.criterion.{ Order, Criterion, Restrictions, MatchMode, Projections, Property, ProjectionList }
import org.hibernate.criterion.Projections._

object PimpedSession {
  implicit def session2PimpedSession(session: Session) = new PimpedSession(session)

  implicit def criteria2PimpedCriteria(criteria: Criteria) = new PimpedCriteria(criteria)

  implicit def string2PimpedStringCondition(field: String) = new PimpedStringCondition(field)

  implicit def hibernateQuery2PimpedQuery(query: Query) = new PimpedQuery(query)
}

class PimpedQuery(query: Query) {
  def withParams(params: (String, Object)*) = {
    params.foreach(param => {
      query.setParameter(param._1, param._2)
    })
    query
  }

  def unique[T]: T = query.uniqueResult.asInstanceOf[T]

  def asList[T]: java.util.List[T] = query.list.asInstanceOf[java.util.List[T]]
}

class PimpedStringCondition(field: String) {
  def equal(value: Object) = Restrictions.eq(field, value)

  def >(value: Object) = Restrictions.gt(field, value)

  def >=(value: Object) = Restrictions.ge(field, value)

  def <(value: Object) = Restrictions.lt(field, value)

  def <=(value: Object) = Restrictions.le(field, value)

  def like(value: String) = Restrictions.ilike(field, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(field)

  def isNotNull = Restrictions.isNotNull(field)

  def desc = Order.desc(field)

  def asc = Order.asc(field)

  def alias(newName: String) = Projections.property(field).as(newName)

}

class PimpedSession(session: Session) {

  def all[T](implicit manifest: Manifest[T]) = {
    session.createCriteria(manifest.erasure).asList[T]
  }

  def from[T](implicit manifest: Manifest[T]) = new PimpedCriteria(session.createCriteria(manifest.erasure))

  def query(query: String) = session.createQuery(query)

  def count[T](implicit manifest: Manifest[T]) = session.createCriteria(manifest.erasure).count

  def exists[T](implicit manifest: Manifest[T]) = count[T] > 0

  def first[T](implicit manifest: Manifest[T]) = session.createCriteria(manifest.erasure).first[T]

  def last[T](implicit manifest: Manifest[T]) = session.createCriteria(manifest.erasure).last[T]
}

class PimpedCriteria(criteria: Criteria) {

  val projections = projectionList
  val criteriaImpl = criteria.asInstanceOf[CriteriaImpl]

  if (criteriaImpl.getProjection != null) {
    projections.add(criteriaImpl.getProjection)
    println(projections)
  }

  def unique[T]: T = {
    criteria.uniqueResult.asInstanceOf[T]
  }

  def asList[T]: java.util.List[T] = {
    criteria.list.asInstanceOf[java.util.List[T]]
  }

  def orderBy(order: Order) = criteria.addOrder(order)

  def join(field: String) = {
    criteria.createAlias(field, field)
    criteria
  }

  def has(toManyField: String) = criteria.add(Restrictions.isNotEmpty(toManyField))

  def includes(toManyField: String) = {
    join(toManyField).has(toManyField)
  }

  def where(condition: Criterion) = criteria.add(condition)

  def where = criteria

  def and(condition: Criterion) = criteria.add(condition)

  def count = criteria.setProjection(rowCount).uniqueResult.asInstanceOf[Long].intValue

  def first[T] = criteria.setFirstResult(0).setMaxResults(1).unique[T]

  def last[T](implicit manifest: Manifest[T]) = {
    val dirtySession = criteria.asInstanceOf[CriteriaImpl].getSession.asInstanceOf[Session]
    val size = dirtySession.from[T].count
    criteria.setFirstResult(size.intValue - 1).unique[T]
  }
  
  def groupBy(fields: String*) = {
    fields.foreach(field => {
      projections.add(Projections.groupProperty(field))
    })
    criteria.setProjection(projections)
  }

  def select(fields: String*) = {
    fields.foreach(field => {
      projections.add(Projections.property(field))
    })
    criteria.setProjection(projections)
  }

  def selectWithAliases(fields: Projection*) = {
    fields.foreach(field => {
      projections.add(field)
    })
    criteria.setProjection(projections)
  }

  def avg(field: String) = {
    projections.add(Projections.avg(field))
    criteria.setProjection(projections)
  }

  def sum(field: String) = {
    projections.add(Projections.sum(field))
    criteria.setProjection(projections)
  }

  def count(field: String) = {
    projections.add(Projections.count(field))
    criteria.setProjection(projections)
  }

  def transformToBean[T](implicit manifest: Manifest[T]) = {    
    new Transformer[T](criteria.setResultTransformer(Transformers.aliasToBean(manifest.erasure)))
  }
}

class Transformer[T](criteria: Criteria) {
  def asList = criteria.asList[T]
  
  def unique = criteria.unique[T]
}

