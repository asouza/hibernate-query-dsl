package br.com.caelum.hibernatequerydsl

import org.junit.Ignore
import org.hibernate.cfg.Configuration
import br.com.caelum.hibernatequerydsl.PimpedSession._
import org.hibernate.Session
import org.junit.{ Test, Before, After }
import org.junit.Assert._
import scala.reflect.Code._
import br.com.caelum.hibernatequerydsl.TypeUnsafe._
import org.hibernate.criterion.Restrictions

class PimpedClassTest {

  private var session: Session = _
  private val userToQuery = new User
  private val addressToQuery = new Address

  @Before
  def setUp {
    val cfg = new Configuration();
    //cfg.configure().setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:mydvdsDB");
    session = cfg.configure().buildSessionFactory().openSession();
    session.beginTransaction();
  }

  /**
   * Undoing all changes to database
   * @throws Exception
   */
  @After
  def tearDown {
    if (session != null && session.getTransaction().isActive()) {
      session.getTransaction().rollback();
    }
  }

  private def newUser(name: String = null, age: Int = 0) = {
    val user = new User
    user setName name
    user setAge age
    session.save(user)
    user
  }

  private def newAddress(street: String, user: User) = {
    val address = new Address
    address setStreet street
    address setUser user
    session.save(address)

    address
  }

  @Test
  def shouldListAllObjects {
    newUser("alberto")
    newUser("alberto")
    val users = session.all[User]
    assertEquals(2, users size)
  }

  @Test
  def shouldVerifyIfExists {
    newUser("alberto")
    assertTrue(session.exists[User])
  }

  @Test
  def shouldVerifyIfNotExists {
    assertFalse(session.exists[User])
  }

  @Test
  def shouldCount {
    newUser("alberto")
    newUser("alberto")
    assertEquals(2, session.count[User])
  }

  @Test
  def shouldGetFirstBasedOnId {
    val alberto = newUser("alberto")
    newUser("alberto2")
    val userRetrieved = session.first[User]
    assertEquals(alberto, userRetrieved)
  }

  @Test
  def shouldGetFirstBasedOnSomeField {
    val alberto = newUser("alberto")
    val joao = newUser("joao")
    val userRetrieved = session.from[User].orderBy(lift(userToQuery.getName).desc).first[User]
    assertEquals(joao, userRetrieved)
  }

  @Test
  def shouldGetTheLastBasedOnId {
    val alberto = newUser("alberto")
    val joao = newUser("joao")
    val userRetrieved = session.last[User]
    assertEquals(joao, userRetrieved)
  }

  @Test
  def shouldGetTheLastDescOrderedOnSomeField {
    val alberto = newUser("alberto")
    val joao = newUser("joao")
    val userRetrieved = session.from[User].orderBy(lift(userToQuery.getName).desc).last[User]
    assertEquals(alberto, userRetrieved)
  }

  @Test
  def shouldGetTheLastDescOrderedOnSomeFields {
    val alberto = newUser("alberto")
    val joao = newUser("joao")
    val userRetrieved = session.from[User].orderBy(lift(userToQuery.getName).desc).last[User]
    assertEquals(alberto, userRetrieved)
  }

  @Test
  def shouldGetTheLastAscOrderedOnSomeField {
    val alberto = newUser("alberto")
    val joao = newUser("joao")
    val userRetrieved = session.from[User].orderBy(lift(userToQuery.getName).asc).last[User]
    assertEquals(joao, userRetrieved)
  }

  @Test
  def shouldGetTheLastAscAndDescOrderedOnSomeFields {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto", 20)
    val userRetrieved = session.from[User].orderBy(lift(userToQuery.getName).asc).orderBy(lift(userToQuery.getAge).desc).last[User]
    assertEquals(alberto, userRetrieved)
  }

  @Test
  def shouldDoASimpleJoin {
    val alberto = newUser("alberto")
    val address = newAddress("rua da casa de nao sei quem", alberto)
    val address2 = newAddress("rua da casa de nao sei quem", alberto)
    val list = session.from[Address].join(lift(addressToQuery.getUser)).asList[Address]
    assertEquals(2, list size)

  }

  @Test
  def shouldDoASimpleJoinBasedOnSomeField1 {
    val alberto = newUser("alberto")
    val alberto2 = newUser("alberto2")
    val address = newAddress("rua da casa de nao sei quem", alberto)
    val address2 = newAddress("rua da casa de nao sei quem", alberto2)
    val list = session.from[Address].join(lift(addressToQuery.getUser)).where(lift(addressToQuery.getUser.getName).equal("alberto2")).asList[Address]
    assertEquals(1, list size)

  }

  @Test
  def shouldDoASimpleJoinBasedOnSomeFieldsWithAnd {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val address = newAddress("rua da casa de nao sei quem", alberto)
    val address2 = newAddress("rua da casa de nao sei quem", alberto2)
    val list = session.from[Address].join(lift(addressToQuery.getUser)).where(lift(addressToQuery.getUser.getName).equal("alberto2")).and(lift(addressToQuery.getUser.getAge).equal(alberto2.getAge)).asList[Address]
    assertEquals(1, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields1 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("alberto4", 40)
    val list = session.from[User].where(lift(userToQuery.getAge) > alberto.getAge).asList[User]
    assertEquals(3, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields2 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("alberto4", 40)
    var list = session.from[User].where(lift(userToQuery.getAge) >= alberto.getAge).asList[User]
    assertEquals(4, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields3 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("alberto4", 40)
    val list = session.from[User].where(lift(userToQuery.getAge) < alberto2.getAge).asList[User]
    assertEquals(1, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields4 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("alberto4", 40)
    val list = session.from[User].where(lift(userToQuery.getAge) <= alberto2.getAge).asList[User]
    assertEquals(2, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields5 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("outrute", 40)
    val list = session.from[User].where(lift(userToQuery.getAge) >= alberto2.getAge).and(lift(userToQuery.getName) like "alberto").asList[User]
    assertEquals(2, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields6 {
    val alberto = newUser(null, 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("outrute", 40)
    val list = session.from[User].where(lift(userToQuery.getName) isNull).asList[User]
    assertEquals(1, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields7 {
    val alberto = newUser(null, 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("outrute", 40)
    val list = session.from[User].where(lift(userToQuery.getName) isNotNull).asList[User]
    assertEquals(3, list size)
  }

  @Test
  def shouldDoASimpleQueryBasedOnSomeFields8 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("alberto4", 40)
    val list = session.from[User].where("name" !== "alberto").asList[User]
    assertEquals(3, list size)
  }

  @Test
  def shouldExecuteJustASimpleHQL {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("outrute", 40)
    val list = session.query("from User").asList[User]
    assertEquals(4, list size)
  }

  @Test
  def shouldAssingParametersForHQL {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 30)
    val alberto4 = newUser("outrute", 40)
    val list = session.query("from User where name=:name and age =:age").withParams("name" -> "alberto", "age" -> alberto.getAge).asList[User]
    assertEquals(1, list size)
  }

  @Test
  def shouldGroupUserByStreet {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val address4 = newAddress("y", alberto4)
    val list = session.from[User].join(lift(userToQuery.getAddresses)).groupBy("addresses.street").asList[User]
    assertEquals(2, list size)
  }

  @Test
  def shouldGroupUserByStreetWithAvgAge {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val address4 = newAddress("y", alberto4)
    val list = session.from[User].join(lift(userToQuery.getAddresses)).groupBy("addresses.street").avg(lift(userToQuery.getAge)).asList[Array[Object]]
    assertEquals(2, list size)
    assertEquals(15.0, list.head(1))
  }

  @Test
  def shouldGroupUserByStreetWithSumAge {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val address4 = newAddress("y", alberto4)
    val list = session.from[User].join(lift(userToQuery.getAddresses)).groupBy("addresses.street").sum(lift(userToQuery.getAge)).asList[Array[Object]]
    assertEquals(2, list size)
    assertEquals(30L, list.head(1))
  }

  @Test
  def shouldGroupUserByStreetWithCountAge {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val address4 = newAddress("y", alberto4)
    val list = session.from[User].join(lift(userToQuery.getAddresses)).groupBy("addresses.street").count(lift(userToQuery.getAge)).asList[Array[Object]]
    assertEquals(2, list size)
    assertEquals(2L, list.head(1))
  }

  @Test
  def shouldListJustUsersWithAddresses {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)

    val list = session.from[User].where.has(lift(userToQuery.getAddresses)).asList[User]
    assertEquals(3, list size)
  }

  @Test
  def shouldListJustUsersWithAddressesFilteringBySomeAttribute {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)

    val list = session.from[User].includes(lift(userToQuery.getAddresses)).where("addresses.street" equal "y").asList[User]
    assertEquals(1, list size)
  }

  @Test
  def shouldListJustUsersWithAddressesFilteringBySomeAttribute2 {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val list = session.from[User].includes(lift(userToQuery.getAddresses)).where("addresses.street" equal "y").asList[User]
    assertEquals(1, list size)
  }

  @Test
  def shouldSelectByFields {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)
    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val list = session.from[User].select(lift(userToQuery.getName)).asList[String]
    assertEquals("alberto", list.head)
  }

  @Test
  def shouldSelectDistinctedObjects {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto", 20)
    val alberto3 = newUser("alberto", 15)
    val alberto4 = newUser("alberto4", 30)
    val list = session.from[User].distinct("name").asList[String]
    assertEquals(2, list.size)
  }

  @Ignore //TODO fazer o resulttransformer funcionar.
  def shouldTransformArrayToMyResultTransformer {
    val alberto = newUser("alberto", 10)
    val alberto2 = newUser("alberto2", 20)
    val alberto3 = newUser("alberto3", 15)

    val alberto4 = newUser("alberto4", 30)
    val address = newAddress("x", alberto)
    val address2 = newAddress("x", alberto2)
    val address3 = newAddress("y", alberto3)
    val list = session.from[User].join(lift(userToQuery.getAddresses)).select(lift(userToQuery.getName)).selectWithAliases("addresses.street".alias("street")).transformToBean[StreetWithName].asList
    assertEquals("alberto", list.head.getName)
  }
}
