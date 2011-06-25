package br.com.caelum.hibernatequerydsl

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.{Test, After, Before}
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.PimpedSession._
class TypeSafeAcceptanceTest {

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

  @Test
  def shouldListAllObjects {
    withUser("guilherme").and("alberto")
    val users = session.from[User].orderBy(_.getName).asc.list
    assertEquals("alberto", users.get(0).getName)
    assertEquals("guilherme", users.get(1).getName)
  }


  @Test
  def shouldSupportComingBackToCriteriaAndAgainToPimped {
    withUser("guilherme").and("alberto")
    val users = session.from[User].orderBy(_.getName).asc.using(_.setMaxResults(1)).list
    assertEquals("alberto", users.get(0).getName)
    assertEquals(1, users.size())
  }

  @Test
  def shouldSupportOrderingByTwoElements {
    withUser("guilherme", 16).and("guilherme", 20)
    val users = session.from[User].orderBy(_.getName).asc.orderBy(_.getAge).desc.list
    assertEquals(20, users.get(0).getAge)
    assertEquals(16, users.get(1).getAge)
  }

  @Test
  def shouldSupportTypeSafeJoining {
    withUser("guilherme", 29, "street 1").and("alberto", 26, "street 2")
    val addresses = session.from[Address].join(_.getUser).where("user.name" equal "guilherme").list
    assertEquals(29, addresses.get(0).getUser.getAge)
    assertEquals(1, addresses.size())
  }


  @Test
  def shouldSupportJoiningAndProjectingOnTheBaseObject {
    withUser("guilherme", 29, "street 1").and("alberto", 26, "street 2")
    val addresses = session.from[Address].join(_.getUser).orderBy(_.getName).list
    assertEquals("alberto", addresses.get(0).getUser.getName)
  }

}