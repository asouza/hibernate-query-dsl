package br.com.caelum.hibernatequerydsl

import conditions.{EqCond, CriterionCond}
import org.hibernate.{ Session, Query }
import scala.reflect.{Apply, Select, Literal, Tree,Code,This }
import java.io.Serializable
import org.hibernate.criterion._
import br.com.caelum.hibernatequerydsl.TypeQuerySafe.X

object PimpedSession {

  implicit def session2PimpedSession(session: Session) = new PimpedSession(session)

  implicit def pimpedCriteria2Criteria[T, P](pimped: PimpedCriteria[T, P]) = pimped.criteria

  implicit def hibernateQuery2PimpedQuery(query: Query) = new PimpedQuery(query)

  implicit def orderThisToPimped[T, P](order: OrderThis[T, P]) = order.asc

  implicit def criteriaToActive[T](criteria:PimpedCriteria[T,T])(implicit t:Manifest[T]) = new ActiveCollection[T](null, criteria)

  implicit def acToList[T](ac: ActiveCollection[T]) = ac.grabThem

  implicit def queryToList[T](query:TypeSafeQuery[T]) = query.list

  implicit def sessionToQueriable(session: Session) = new {
    def query[T](implicit manifest:Manifest[T]) = new TypeSafeQuery[T](session)(manifest)
  }

}

object TypeUnsafe {
  implicit def string2PimpedStringCondition(field: String) = new PimpedStringCondition(field)
}

object TypeSafe {
  implicit def anything2TypeSafeCondition(qq: Any) = new TypeSafeCriteriaCondition(Pig.tl.get)

  implicit def criterion2Cond(crit:Criterion) = new CriterionCond(crit)
}

object TypeQuerySafe {


  implicit def anyToEq(qq:Any) = new X(Pig.tl.get)

  class X(proxy:InvocationMemorizingCallback) {
    val field = proxy.prefix + proxy.invokedPath
    def equal(other:Any) = new EqCond(field, other)
  }
}

class TypeSafeCriteriaCondition(proxy: InvocationMemorizingCallback) {

  val field = proxy.prefix + proxy.invokedPath

  def equal(value: Any) = Restrictions.eq(field, value)

  def \==(value: Any) = Restrictions.eq(field, value)

  def \>(value: Any) = Restrictions.gt(field, value)

  def \>=(value: Any) = Restrictions.ge(field, value)

  def \<(value: Any) = Restrictions.lt(field, value)

  def \<=(value: Any) = Restrictions.le(field, value)

  def \!=(value: Any) = Restrictions.ne(field, value)

  def like(value: String) = Restrictions.ilike(field, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(field)

  def isNotNull = Restrictions.isNotNull(field)

  def alias(newName: String) = Projections.property(field).as(newName)
}

class PimpedStringCondition(field: String) {
  def equal(value: Any) = Restrictions.eq(field, value)

  def >(value: Any) = Restrictions.gt(field, value)

  def >=(value: Any) = Restrictions.ge(field, value)

  def <(value: Any) = Restrictions.lt(field, value)

  def <=(value: Any) = Restrictions.le(field, value)

  def !==(value: Any) = Restrictions.ne(field, value)

  def like(value: String) = Restrictions.ilike(field, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(field)

  def isNotNull = Restrictions.isNotNull(field)

  def desc = Order.desc(field)

  def asc = Order.asc(field)

  def alias(newName: String) = Projections.property(field).as(newName)

}

class PimpedSession(session: Session) {

  def all[T](implicit manifest: Manifest[T]) = {
    from[T].list
  }

  def from[T](implicit manifest: Manifest[T]) = {
    val criteria = session.createCriteria(manifest.erasure)
    new PimpedCriteria[T, T]("", criteria)
  }

  def query(query: String) = session.createQuery(query)

  def count[T](implicit manifest: Manifest[T]) = from[T].count

  def exists[T](implicit manifest: Manifest[T]) = count[T] > 0

  def first[T](implicit manifest: Manifest[T]) = from[T].first[T]

  def last[T](implicit manifest: Manifest[T]) = from[T].last[T]

  def load[T](implicit manifest: Manifest[T], id: Serializable) = {
    session.load(manifest.erasure, id).asInstanceOf[T]
  }

}



