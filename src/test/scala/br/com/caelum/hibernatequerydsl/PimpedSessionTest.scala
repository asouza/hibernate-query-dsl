package br.com.caelum.hibernatequerydsl

import scala.reflect.BeanProperty
import org.hibernate.cfg.Configuration
import br.com.caelum.hibernatequerydsl.PimpedSession._
import org.hibernate.Session
import org.hibernate.criterion.Order._
import org.junit.{Test, Before, After}
import org.junit.Assert._

class PimpedClassTest {

	private var session:Session = _
	
	@Before
	def setUp {
		val cfg = new Configuration();
		cfg.configure().setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:mydvdsDB");
		session = cfg.buildSessionFactory().openSession();
		session.beginTransaction();
	}


	/**
	 * Undoing all changes to database
	 * @throws Exception
	 */
	@After
	def tearDown{
		if (session != null && session.getTransaction().isActive()) {
			session.getTransaction().rollback();
		}
	}	
	
	private def newUser(name:String=null,age:Int=0) = {
		val user = new User
		user setName name
		user setAge  age
		session.save(user)
		user
	}
	
	private def newAddress(street:String,user:User) = {
		val address = new Address
		address setStreet street
		address setUser  user
		session.save(address)
		address
	}
	
	
	@Test
	def shouldListAllObjects {
		newUser("alberto") 
		newUser("alberto")
		val users = session.all[User]
		assertEquals(2,users size)		
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
		assertEquals(2,session.count[User])
	}
	
	@Test
	def shouldGetFirstBasedOnId {
		val alberto = newUser("alberto") 
		newUser("alberto2")
		val userRetrieved = session.first[User]
		assertEquals(alberto,userRetrieved)
	}
	
	@Test
	def shouldGetFirstBasedOnSomeField {
		val alberto = newUser("alberto") 
		val joao = newUser("joao")
		val userRetrieved = session.from[User].orderBy("name".desc).first[User]
		assertEquals(joao,userRetrieved)
	}
	
	@Test
	def shouldGetTheLastBasedOnId {
		val alberto = newUser("alberto") 
		val joao = newUser("joao")
		val userRetrieved = session.last[User]
		assertEquals(joao,userRetrieved)
	}
	
	@Test
	def shouldGetTheLastDescOrderedOnSomeField {
		val alberto = newUser("alberto") 
		val joao = newUser("joao")
		val userRetrieved = session.from[User].orderBy("name".desc).last[User]
		assertEquals(alberto,userRetrieved)
	}
	
	@Test
	def shouldGetTheLastDescOrderedOnSomeFields {
		val alberto = newUser("alberto") 
		val joao = newUser("joao")
		val userRetrieved = session.from[User].orderBy("name".desc).last[User]		
		assertEquals(alberto,userRetrieved)
	}	
	
	@Test
	def shouldGetTheLastAscOrderedOnSomeField {
		val alberto = newUser("alberto") 
		val joao = newUser("joao")
		val userRetrieved = session.from[User].orderBy("name".asc).last[User]
		assertEquals(joao,userRetrieved)
	}
	
	@Test
	def shouldGetTheLastAscAndDescOrderedOnSomeFields {
		val alberto = newUser("alberto",10) 
		val alberto2 = newUser("alberto",20)
		val userRetrieved = session.from[User].orderBy("name".asc).orderBy("age".desc).last[User]
		assertEquals(alberto,userRetrieved)
	}	
	
	@Test
	def shouldDoASimpleJoin {
		val alberto = newUser("alberto")
		val address = newAddress("rua da casa de nao sei quem",alberto)
		val address2 = newAddress("rua da casa de nao sei quem",alberto)		
		val list = session.from[Address].join("user").asList[Address]
		assertEquals(2,list size)
		
	}
	
	@Test
	def shouldDoASimpleJoinBasedOnSomeField1 {
		val alberto = newUser("alberto")
		val alberto2 = newUser("alberto2")
		val address = newAddress("rua da casa de nao sei quem",alberto)
		val address2 = newAddress("rua da casa de nao sei quem",alberto2)		
		val list = session.from[Address].join("user").where("user.name".equal("alberto2")).asList[Address]
		assertEquals(1,list size)
		
	}
	
	@Test
	def shouldDoASimpleJoinBasedOnSomeFieldsWithAnd {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val address = newAddress("rua da casa de nao sei quem",alberto)
		val address2 = newAddress("rua da casa de nao sei quem",alberto2)		
		val list = session.from[Address].join("user").where("user.name".equal("alberto2")).and("user.age".equal(alberto2.getAge)).asList[Address]
	    assertEquals(1,list size)		
	}
	
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields1 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("alberto4",40)
		val list = session.from[User].where("age" > alberto.getAge).asList[User]
	    assertEquals(3,list size)		
	}
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields2 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("alberto4",40)
		var list = session.from[User].where("age" >= alberto.getAge).asList[User]
	    assertEquals(4,list size)		
	}		
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields3 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("alberto4",40)
		val list = session.from[User].where("age" < alberto2.getAge).asList[User]
	    assertEquals(1,list size)		
	}		
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields4 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("alberto4",40)
		val list = session.from[User].where("age" <= alberto2.getAge).asList[User]
	    assertEquals(2,list size)		
	}
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields5 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("outrute",40)
		val list = session.from[User].where("age" >= alberto2.getAge).and("name" like "alberto").asList[User]
        assertEquals(2,list size)		
	}
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields6 {
		val alberto = newUser(null,10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("outrute",40)
		val list = session.from[User].where("name" isNull).asList[User]
        assertEquals(1,list size)		
	}
	
	@Test
	def shouldDoASimpleQueryBasedOnSomeFields7 {
		val alberto = newUser(null,10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("outrute",40)
		val list = session.from[User].where("name" isNotNull).asList[User]
        assertEquals(3,list size)		
	}		
	
	@Test
	def shouldExecuteJustASimpleHQL {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("outrute",40)
		val list = session.query("from User").asList[User]
		assertEquals(4,list size)
	}	
	
	@Test
	def shouldAssingParametersForHQL {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",30)
		val alberto4 = newUser("outrute",40)
		val list = session.query("from User where name=:name and age =:age").withParams("name" -> "alberto","age" -> alberto.getAge).asList[User]		
		assertEquals(1,list size)
	}
			
	@Test
	def shouldGroupUserByStreet {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val address4 = newAddress("y",alberto4)
		val list = session.from[User].join("addresses").groupBy("addresses.street").asList[User]
		assertEquals(2,list size)
	}
	
	@Test
	def shouldGroupUserByStreetWithAvgAge {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val address4 = newAddress("y",alberto4)		
		val list = session.from[User].join("addresses").groupBy("addresses.street").avg("age").asList[Array[Object]]
		assertEquals(2, list size)
		assertEquals(15.0, list.get(0)(1))
	}
	
	@Test
	def shouldGroupUserByStreetWithSumAge {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val address4 = newAddress("y",alberto4)		
		val list = session.from[User].join("addresses").groupBy("addresses.street").sum("age").asList[Array[Object]]
		assertEquals(2, list size)
		assertEquals(30L, list.get(0)(1))
	}
	
	@Test
	def shouldGroupUserByStreetWithCountAge {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val address4 = newAddress("y",alberto4)		
		val list = session.from[User].join("addresses").groupBy("addresses.street").count("age").asList[Array[Object]]
		assertEquals(2, list size)
		assertEquals(2L, list.get(0)(1))
	}	
	
	@Test
	def shouldListJustUsersWithAddresses {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		
		val list = session.from[User].where.has("addresses").asList[User]
		assertEquals(3,list size)
	}		
	
	@Test
	def shouldListJustUsersWithAddressesFilteringBySomeAttribute {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		
		val list = session.from[User].includes("addresses").where("addresses.street" equal "y").asList[User]
		assertEquals(1,list size)
	}
	
	@Test
	def shouldListJustUsersWithAddressesFilteringBySomeAttribute2 {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val list = session.from[User].includes("addresses").where("addresses.street" equal "y").asList[User]
		assertEquals(1,list size)		
	}		
	
	@Test
	def shouldSelectByFields {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)		
		val list = session.from[User].select("name").asList[String]
		assertEquals("alberto",list.get(0))
	}
	
	@Test
	def shouldTransformArrayToMyResultTransformer {
		val alberto = newUser("alberto",10)
		val alberto2 = newUser("alberto2",20)
		val alberto3 = newUser("alberto3",15)
		val alberto4 = newUser("alberto4",30)
		val address = newAddress("x",alberto)
		val address2 = newAddress("x",alberto2)		
		val address3 = newAddress("y",alberto3)
		val list = session.from[User].join("addresses").select("name").selectWithAliases("addresses.street".alias("street")).transformToBean[StreetWithName].asList
		assertEquals("alberto",list.get(0).getName)
	}			
}