package ca.odell.glazedlists.hibernate6.model.tests;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import ca.odell.glazedlists.hibernate6.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

/**
 * @author Nathan Hapke
 */
public class BasicUserTest extends AbstractUserTest {
	public BasicUserTest(QueryType mode) {
		super(mode);
	}

	@Test
	public void testBasicUserPersistAndDeleteOnly() {
		randomize();
		final String userName = getUsername();

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
			} catch (PersistenceException e) {
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
			assertEquals(userName, u1B.getUserName());

			t1b.rollback();

			s1.close();

			EntityTransaction txn1 = em1.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em1.close();
			System.out.println("Session 1 closed");
		}
		{
			// load user in new session
			Session s2 = openSession();
			Transaction t2 = s2.beginTransaction();

			EntityManagerFactory emf2 = s2.getEntityManagerFactory();
			EntityManager em2 = emf2.createEntityManager();

			// load saved user again
			List<User> users2 = loadUser(em2, userName);
			assertEquals(1, users2.size());
			User u2 = users2.get(0);

			assertNotNull(u2);
			assertEquals(userName, u2.getUserName());
			EntityTransaction txn2 = em2.getTransaction();
			txn2.begin();

			em2.remove(u2);

			if (txn2.isActive())
				txn2.commit();

			s2.flush();
			s2.evict(u2);
			t2.commit();
			s2.close();
			em2.close();
			System.out.println("Session 2 closed");
		}
	}
}
