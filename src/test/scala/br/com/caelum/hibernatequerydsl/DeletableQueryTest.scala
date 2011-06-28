package br.com.caelum.hibernatequerydsl

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.{Test, After, Before}
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.TypeSafe._

class DeletableQueryTest {

  private var session:Session = _

  @Before
  def setUp {
    val cfg = new Configuration();
    session = cfg.configure().buildSessionFactory().openSession();
    session.beginTransaction();
  }

  @After
  def tearDown{
    if (session != null && session.getTransaction().isActive()) {
      session.getTransaction().rollback();
    }
  }

  private def withUser(name:String=null,age:Int=0, street:String=null) = {
    val user = new User
    user setName name
    user setAge  age
    session.save(user)
    if(street!=null){
      val address = new Address
      address setStreet street
      address setUser  user
      session.save(address)
    }
    this
  }

  private def and(name:String=null,age:Int=0, street:String=null) = {
    withUser(name, age, street)
  }

  def query = new DeletableQuery[User](session)

  @Test
  def shouldSupportFiltering {
    withUser("guilherme", 30).and("aniche").and("alberto").and("guilherme", 29)
    val deleted = query.filter(_.getName equal "guilherme").delete
    assertEquals(2, deleted)
  }

}