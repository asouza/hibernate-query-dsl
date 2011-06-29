package br.com.caelum.hibernatequerydsl

import org.hibernate.{ Criteria, Session, Query }
import scala.reflect.{Apply, Select, Literal, Tree,Code,This }
import org.hibernate.criterion.{ Order, Restrictions, MatchMode, Projections }

class PimpedCode[T](code: Code[T]) {

  implicit def string2WithRubyPowers(str: String) = new StringWithRubyPowers(str)
  
  class StringWithRubyPowers(str: String) {
    def withFirstCharLowered = {
      str.substring(0, 1).toLowerCase + str.substring(1, str.length)
    }
  }

  private def evaluate: String = {
    def extractString(tree: Tree, properties: List[String] = List()): List[String] = {
      //literal is for local variables and this for instance
      if (tree.isInstanceOf[Literal] ||tree.isInstanceOf[This] || tree.isInstanceOf[Select]) {
        return properties
      }
      
      val expressao = tree.asInstanceOf[Apply].fun.asInstanceOf[Select]
      val GetterExpression = """(get)?(\w*){1}""".r
      expressao.sym.name match {
        case GetterExpression(_, part2) => {
          extractString(expressao.qual, part2.withFirstCharLowered :: properties)
        }
      }
    }
    val tree = code.tree
    extractString(tree).mkString(".")
  } 
  
  override def toString = evaluate
  
  def equal(value: Any) = Restrictions.eq(evaluate, value)

  def >(value: Any) = Restrictions.gt(evaluate, value)

  def >=(value: Any) = Restrictions.ge(evaluate, value)

  def <(value: Any) = Restrictions.lt(evaluate, value)

  def <=(value: Any) = Restrictions.le(evaluate, value)
  
  def !==(value: Any) = Restrictions.ne(evaluate,value)

  def like(value: String) = Restrictions.ilike(evaluate, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(evaluate)

  def isNotNull = Restrictions.isNotNull(evaluate)

  def desc = Order.desc(evaluate)

  def asc = Order.asc(evaluate)

  def alias(newName: String) = Projections.property(evaluate).as(newName)  
}