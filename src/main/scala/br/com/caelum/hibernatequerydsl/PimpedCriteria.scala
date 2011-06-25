package br.com.caelum.hibernatequerydsl

import org.hibernate.criterion._
import org.hibernate.criterion.Projections._
import org.hibernate.impl.CriteriaImpl
import org.hibernate.transform.Transformers
import org.hibernate.{Session, Criteria}
import net.sf.cglib.proxy.Enhancer

class PimpedCriteria[T](val criteria: Criteria) {

  import PimpedSession._
  type Myself = PimpedCriteria[T]
  implicit def criteriaToPimped(partial:Criteria) = new PimpedCriteria[T](partial)

  val projections = projectionList
  val criteriaImpl = criteria.asInstanceOf[CriteriaImpl]

  if (criteriaImpl.getProjection != null) {
    projections.add(criteriaImpl.getProjection)
  }

  def unique[Y]: Y = {
    criteria.uniqueResult.asInstanceOf[Y]
  }

  def asList[Y]: java.util.List[Y] = criteria.list.asInstanceOf[java.util.List[Y]]

  def using(f:(Criteria) => Criteria):Myself = f(criteria)

  def list:java.util.List[T] = asList[T]

  def orderBy(order: Order):Myself = criteria.addOrder(order)

  // TODO use only one class per entity
	def orderBy2(f:(T) => Unit)(implicit entityType:Manifest[T]) = {
		val handler = new InvocationMemorizingCallback
    val proxy = Enhancer.create(entityType.erasure, handler).asInstanceOf[T]
		f(proxy)
		new OrderThis[T](handler.invokedPath, this)
	}

  def join(field: String):Myself = {
    criteria.createAlias(field, field)
  }

  def has(toManyField: String):Myself = {
    criteria.add(Restrictions.isNotEmpty(toManyField))
  }

  def includes(toManyField: String):Myself = {
    join(toManyField).has(toManyField)
  }

  def where(condition: Criterion):Myself = {
    criteria.add(condition)
  }

  def where:Myself = { this }

  def and(condition: Criterion):Myself = {
    criteria.add(condition)
  }

  def count = criteria.setProjection(rowCount).uniqueResult.asInstanceOf[Long].intValue

  def first[Y] = criteria.setFirstResult(0).setMaxResults(1).unique[Y]

  def last[Y](implicit manifest: Manifest[Y]) = {
    val dirtySession = criteria.asInstanceOf[CriteriaImpl].getSession.asInstanceOf[Session]
    val size = dirtySession.from[Y].count
    criteria.setFirstResult(size.intValue - 1).unique[Y]
  }

  def groupBy(fields: String*):Myself = {
    fields.foreach(field => {
      projections.add(Projections.groupProperty(field))
    })
    criteria.setProjection(projections)
  }

  def select(fields: String*):Myself = {
    fields.foreach(field => {
      projections.add(Projections.property(field))
    })
    criteria.setProjection(projections)
  }

  def selectWithAliases(fields: Projection*):Myself = {
    fields.foreach(projections.add(_))
    criteria.setProjection(projections)
  }

  def avg(field: String):Myself = {
    projections.add(Projections.avg(field))
    criteria.setProjection(projections)
  }

  def sum(field: String):Myself = {
    projections.add(Projections.sum(field))
    criteria.setProjection(projections)
  }

  def count(field: String):Myself = {
    projections.add(Projections.count(field))
    criteria.setProjection(projections)
  }

  def transformToBean[Y](implicit manifest: Manifest[Y]) = {
    new Transformer[Y](criteria.setResultTransformer(Transformers.aliasToBean(manifest.erasure)))
  }
}
