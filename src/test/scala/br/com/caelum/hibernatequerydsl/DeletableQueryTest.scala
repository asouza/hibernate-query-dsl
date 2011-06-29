package br.com.caelum.hibernatequerydsl

import org.junit.Test
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.TypeSafe._

class DeletableQueryTest extends SessionBased{

  @Test
  def shouldSupportFiltering {
    def query = new DeletableQuery[User](session)
    withUser("guilherme", 30).and("aniche").and("alberto").and("guilherme", 29)
    val deleted = query.filter(_.getName equal "guilherme").delete
    assertEquals(2, deleted)
  }

  @Test
  def shouldSupportFilteringTypeSafeByType {
    def query = new DeletableQuery[Address](session)
    withUser("aniche").and("alberto")
    val guilherme = newUser("guilherme", 29, "rua vergueiro")
    val deleted = query.filter(_.getUser equal guilherme).delete
    assertEquals(1, deleted)
  }

}