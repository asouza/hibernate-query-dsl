package br.com.caelum.hibernatequerydsl

import org.hibernate.criterion._
import org.hibernate.criterion.Projections._
import org.hibernate.impl.CriteriaImpl
import org.hibernate.transform.Transformers
import org.hibernate.{Session, Criteria}
import net.sf.cglib.proxy.Enhancer
import scala.collection.JavaConversions._

/**
 * A criteria that will query on objects of type T, projecting
 * on type P. This criteria is backed by a hibernate criteria.
 */
class PimpedCriteria[T,P](prefix:String, val criteria: Criteria) {

  import PimpedSession._
  type Myself = PimpedCriteria[T,P]
  implicit def criteriaToPimped(partial:Criteria) = new PimpedCriteria[T,P](prefix, partial)

  val projections = projectionList
  val criteriaImpl = criteria.asInstanceOf[CriteriaImpl]

  if (criteriaImpl.getProjection != null) {
    projections.add(criteriaImpl.getProjection)
  }

  def unique[Y]: Y = criteria.uniqueResult.asInstanceOf[Y]

  def asList[Y]: List[Y] = criteria.list.asInstanceOf[java.util.List[Y]].toList

  def using(f:(Criteria) => Criteria):Myself = f(criteria)

  def list:List[P] = asList[P]

  def orderBy(order: Order):Myself = criteria.addOrder(order)

  // TODO use only one class per entity
	def orderBy(f:(T) => Unit)(implicit entityType:Manifest[T]) = {
    val path = evaluate(f)
		new OrderThis[T,P](prefix + path, this)
	}

  def orderBy2[Proj](f:(Proj) => Unit)(implicit manifest:Manifest[Proj]) = {
    val path = evaluate(f)
		new OrderThis[T,Proj](path, new PimpedCriteria[T,Proj]("", criteria))
  }

  def headOption:Option[P] = using(_.setMaxResults(1)).list.toList.asInstanceOf[List[P]].headOption

  def join(field: String):Myself = criteria.createAlias(field, field)

  def join[Joiner](f:(T) => Joiner)(implicit entityType:Manifest[T]) = {
    val field = evaluate(f)
    new PimpedCriteria[Joiner, P](prefix + field + ".", criteria.createAlias(field, field))
  }

  private def evaluate[K,X](f:(K) => X)(implicit entityType:Manifest[K]):String = {
    val handler = new InvocationMemorizingCallback
    val proxy = Enhancer.create(entityType.erasure, handler).asInstanceOf[K]
    f(proxy)
    handler.invokedPath
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
  
  def distinct(field:String):Myself = {
    projections.add(Projections.distinct(Projections.property(field)))
    criteria.setProjection(projections)
  }

  def transformToBean[Y](implicit manifest: Manifest[Y]) = {
    new Transformer[Y,P](criteria.setResultTransformer(Transformers.aliasToBean(manifest.erasure)))
  }
}
