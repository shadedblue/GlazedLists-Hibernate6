package ca.odell.glazedlists.hibernate6.model.tests;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.hibernate6.model.Email;
import ca.odell.glazedlists.hibernate6.model.Role;
import ca.odell.glazedlists.hibernate6.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

/**
 * @author Nathan Hapke
 */
public class UserAndFieldsTest extends AbstractUserTest {

	public UserAndFieldsTest(QueryType mode) {
		super(mode);
	}

	@Test
	public void testRoles() {
		randomize();
		final String name = getName();
		final String userName = getUsername();
		final String roleName = getRoleName();

		{
			Session s1 = openSession();
			Transaction t1 = s1.beginTransaction();

			EntityManagerFactory emf1 = s1.getEntityManagerFactory();
			EntityManager em1 = emf1.createEntityManager();
			User u1;
			log("Making sure '" + name + "' doesn't exist");
			try {
				List<User> users1 = loadUser(em1, userName);
				if (users1.size() > 0) {
					fail("Found a user with the userName: '" + userName + "'");
				}
			} catch (NoResultException e) {
				// success
			}

			u1 = new User(userName);
			log("Persisting u1");
			s1.persist(u1);

			t1.commit();
			if (s1.isDirty())
				s1.flush();

			Transaction t1b = s1.beginTransaction();

			List<User> users1B = loadUser(em1, userName);
			assertEquals(1, users1B.size());
			User u1B = users1B.get(0);

			if (!Hibernate.isInitialized(u1B.getRoles())) {
				log("u1B's roles not initialized");
				Hibernate.initialize(u1B.getRoles());
				log("Hibernate initialize complete");
			}

			List<Role> roles1 = u1B.getRoles();
			assertTrue(roles1 instanceof EventList<?>);
			assertEquals(0, roles1.size());

			Role r1 = new Role(roleName);
			roles1.add(r1);
			assertEquals(1, roles1.size());

			log("Persisting r1");
			s1.persist(r1);

			log("Merging u1's r1");
			s1.merge(u1B);

			t1b.commit();
			if (s1.isDirty())
				s1.flush();
			s1.close();

			EntityTransaction txn1 = em1.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em1.close();
			log("Session 1 closed");
		}
		{
			// load user in new session
			Session s2 = openSession();
			Transaction t2 = s2.beginTransaction();

			EntityManagerFactory emf2 = s2.getEntityManagerFactory();
			EntityManager em2 = emf2.createEntityManager();

			EntityTransaction txn2 = em2.getTransaction();
			txn2.begin();

			// load saved user again
			log("Grabbing users2");
			List<User> users2 = loadUser(em2, userName);
			assertEquals(1, users2.size());
			User u2 = users2.get(0);

			assertNotNull(u2);
			String u2UserName = u2.getUserName();
			log("Found u2 '" + u2UserName + "'");
			assertEquals(userName, u2UserName);

			log("Checking roles2");
			List<Role> roles2 = u2.getRoles();
			assertTrue(roles2 instanceof EventList<?>);
			assertEquals(1, roles2.size());

			Role r2 = roles2.get(0);
			assertEquals(roleName, r2.getName());

//			log("Deleting u2");
//			em2.remove(u2);

			if (txn2.isActive())
				txn2.commit();

			s2.flush();
			s2.evict(u2);
			t2.commit();
			s2.close();
			em2.close();
			log("Session 2 closed");
		}
	}

	@Test
	public void testNicknames() {
		randomize();
		final String name = getName();
		final String userName = getUsername();

		final String nickName1 = getNickName();
		final String nickName2 = getNickName();
		final String nickName3 = getNickName();

		{
			Session s1 = openSession();
			Transaction t1 = s1.beginTransaction();

			EntityManagerFactory emf1 = s1.getEntityManagerFactory();
			EntityManager em1 = emf1.createEntityManager();
			User u1;
			log("Making sure '" + name + "' doesn't exist");
			try {
				List<User> users1 = loadUser(em1, userName);
				if (users1.size() > 0) {
					fail("Found a user with the userName: '" + userName + "'");
				}
			} catch (NoResultException e) {
				// success
			}

			u1 = new User(userName);
			log("Persisting u1");
			s1.persist(u1);

			t1.commit();
			if (s1.isDirty())
				s1.flush();

			Transaction t1b = s1.beginTransaction();

			List<User> users1B = loadUser(em1, userName);
			assertEquals(1, users1B.size());
			User u1B = users1B.get(0);

			if (!Hibernate.isInitialized(u1B.getRoles()))
				Hibernate.initialize(u1B.getRoles());

			List<String> nicks1 = u1B.getNickNames();
			assertTrue(nicks1 instanceof EventList<?>);
			assertEquals(0, nicks1.size());

			nicks1.add(nickName1);
			nicks1.add(nickName2);
			nicks1.add(nickName3);
			assertEquals(3, nicks1.size());

			log("Merging u1's nicks");
			s1.merge(u1B);

			t1b.commit();
			if (s1.isDirty())
				s1.flush();
			s1.close();

			EntityTransaction txn1 = em1.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em1.close();
			log("Session 1 closed");
		}
		{
			// load user in new session
			Session s2 = openSession();
			Transaction t2 = s2.beginTransaction();

			EntityManagerFactory emf2 = s2.getEntityManagerFactory();
			EntityManager em2 = emf2.createEntityManager();

			EntityTransaction txn2 = em2.getTransaction();
			txn2.begin();

			// load saved user again
			log("Grabbing users2");
			List<User> users2 = loadUser(em2, userName);
			assertEquals(1, users2.size());
			User u2 = users2.get(0);

			assertNotNull(u2);
			String u2UserName = u2.getUserName();
			log("Found u2 '" + u2UserName + "'");
			assertEquals(userName, u2UserName);

			log("Checking nicks2");
			List<String> nicks2 = u2.getNickNames();
			assertEquals(3, nicks2.size());
			assertTrue(nicks2.contains(nickName1));
			assertTrue(nicks2.contains(nickName2));
			assertTrue(nicks2.contains(nickName3));

//			log("Deleting u2");
//			em2.remove(u2);

			if (txn2.isActive())
				txn2.commit();

			s2.flush();
			s2.evict(u2);
			t2.commit();
			s2.close();
			em2.close();
			log("Session 2 closed");
		}
	}

	@Test
	public void testAllFields() {
		randomize();
		final String name = getName();
		final String userName = getUsername();

		final String emailAddr = getEmailAddr();
		final String roleName = getRoleName();

		final String nickName1 = getNickName();
		final String nickName2 = getNickName();
		final String nickName3 = getNickName();

		{
			Session s1 = openSession();
			Transaction t1 = s1.beginTransaction();

			EntityManagerFactory emf1 = s1.getEntityManagerFactory();
			EntityManager em1 = emf1.createEntityManager();
			User u1;
			log("Making sure '" + name + "' doesn't exist");
			try {
				List<User> users1 = loadUser(em1, userName);
				if (users1.size() > 0) {
					fail("Found a user with the userName: '" + userName + "'");
				}
			} catch (NoResultException e) {
				// success
			}

			u1 = new User(userName);
			log("Persisting u1");
			s1.persist(u1);

			t1.commit();
			if (s1.isDirty())
				s1.flush();

			Transaction t1b = s1.beginTransaction();

			List<User> users1B = loadUser(em1, userName);
			assertEquals(1, users1B.size());
			User u1B = users1B.get(0);

			if (!Hibernate.isInitialized(u1B.getEmailAddresses()))
				Hibernate.initialize(u1B.getEmailAddresses());
			if (!Hibernate.isInitialized(u1B.getRoles()))
				Hibernate.initialize(u1B.getRoles());
			if (!Hibernate.isInitialized(u1B.getNickNames()))
				Hibernate.initialize(u1B.getNickNames());

			List<Email> emails1 = u1B.getEmailAddresses();
			assertTrue(emails1 instanceof EventList<?>);
			assertEquals(0, emails1.size());

			Email e1 = new Email(emailAddr);
			emails1.add(e1);
			assertEquals(1, emails1.size());

			log("Persisting e1");
			s1.persist(e1);

			List<Role> roles1 = u1B.getRoles();
			assertTrue(roles1 instanceof EventList<?>);
			assertEquals(0, roles1.size());

			Role r1 = new Role(roleName);
			roles1.add(r1);
			assertEquals(1, roles1.size());

			log("Persisting r1");
			s1.persist(r1);

			List<String> nicks1 = u1B.getNickNames();
			assertTrue(nicks1 instanceof EventList<?>);
			assertEquals(0, nicks1.size());

			nicks1.add(nickName1);
			nicks1.add(nickName2);
			nicks1.add(nickName3);
			assertEquals(3, nicks1.size());

			log("Merging u1's r1");
			s1.merge(u1B);

			t1b.commit();
			if (s1.isDirty())
				s1.flush();
			s1.close();

			EntityTransaction txn1 = em1.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em1.close();
			log("Session 1 closed");
		}
		{
			// load user in new session
			Session s2 = openSession();
			Transaction t2 = s2.beginTransaction();

			EntityManagerFactory emf2 = s2.getEntityManagerFactory();
			EntityManager em2 = emf2.createEntityManager();

			EntityTransaction txn2 = em2.getTransaction();
			txn2.begin();

			// load saved user again
			log("Grabbing users2");
			List<User> users2 = loadUser(em2, userName);
			assertEquals(1, users2.size());
			User u2 = users2.get(0);

			assertNotNull(u2);
			String u2UserName = u2.getUserName();
			log("Found u2 '" + u2UserName + "'");
			assertEquals(userName, u2UserName);

			log("Checking emails2");
			List<Email> emails2 = u2.getEmailAddresses();
			assertTrue(emails2 instanceof EventList<?>);
			assertEquals(1, emails2.size());

			Email email2 = emails2.get(0);
			assertEquals(emailAddr, email2.getAddress());

			log("Checking roles2");
			List<Role> roles2 = u2.getRoles();
			assertTrue(roles2 instanceof EventList<?>);
			assertEquals(1, roles2.size());

			Role r2 = roles2.get(0);
			assertEquals(roleName, r2.getName());

			log("Checking nicks2");
			List<String> nicks2 = u2.getNickNames();
			assertEquals(3, nicks2.size());
			assertTrue(nicks2.contains(nickName1));
			assertTrue(nicks2.contains(nickName2));
			assertTrue(nicks2.contains(nickName3));

//			log("Deleting u2");
//			em2.remove(u2);

			if (txn2.isActive())
				txn2.commit();

			s2.flush();
			s2.evict(u2);
			t2.commit();
			s2.close();
			em2.close();
			log("Session 2 closed");
		}
	}
}
