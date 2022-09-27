package ca.odell.glazedlists.hibernate6.model.tests;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.hibernate6.model.Email;
import ca.odell.glazedlists.hibernate6.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

/**
 * @author Nathan Hapke
 */
public class UserAndEmailsTest extends AbstractUserTest {
	public UserAndEmailsTest(QueryType mode) {
		super(mode);
	}

	@Test
	public void testPersistAndDelete() {
		int n = (int) (10000 * Math.random());
		String name = "Steve";
		final String userName = name + n;
		final String emailAddr = "box" + n + "@" + name + ".ca";

		{
			log("Opening Session #1");
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
			s1.close();
			

			EntityTransaction txn1 = em1.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em1.close();
			log("Session 1 closed");
		}
		{
			log("Opening Session #2");
			Session s2 = openSession();
			Transaction t2 = s2.beginTransaction();
			EntityManagerFactory emf2 = s2.getEntityManagerFactory();
			EntityManager em2 = emf2.createEntityManager();
			
			User u2 = null;
			{
				List<User> users2 = loadUser(em2, userName);
				assertEquals(1, users2.size());
				u2 = users2.get(0);
			}
			
			List<Email> emails1 = u2.getEmailAddresses();
			assertTrue(emails1 instanceof EventList<?>);
			assertEquals(0, emails1.size());

			Email e1 = new Email(emailAddr);
			emails1.add(e1);
			assertEquals(1, emails1.size());

			log("Persisting e1");
			s2.persist(e1);

			log("Merging u2's e1");
			s2.merge(u2);

			t2.commit();
			if (s2.isDirty())
				s2.flush();
			s2.close();

			EntityTransaction txn1 = em2.getTransaction();
			if (txn1.isActive())
				txn1.commit();

			em2.close();
			log("Session 2 closed");
		}
		{
			log("Opening Session #3");
			// load user in new session
			Session s3 = openSession();
			Transaction t3 = s3.beginTransaction();

			EntityManagerFactory emf3 = s3.getEntityManagerFactory();
			EntityManager em3 = emf3.createEntityManager();

			EntityTransaction txn3 = em3.getTransaction();
			txn3.begin();

			// load saved user again
			log("Grabbing users3");
			List<User> users3 = loadUser(em3, userName);
			assertEquals(1, users3.size());
			User u3 = users3.get(0);

			assertNotNull(u3);
			String u3UserName = u3.getUserName();
			log("Found u3 '" + u3UserName + "'");
			assertEquals(userName, u3UserName);

			log("Checking emails3");
			List<Email> emails3 = u3.getEmailAddresses();
			assertTrue(emails3 instanceof EventList<?>);
			assertEquals(1, emails3.size());

			Email email3 = emails3.get(0);
			assertEquals(emailAddr, email3.getAddress());

			log("Deleting u3");
			em3.remove(u3);

			if (txn3.isActive())
				txn3.commit();

//			s3.delete(u2);
			s3.flush();
			s3.evict(u3);
			t3.commit();
			s3.close();
			em3.close();
			log("Session 3 closed");
		}
	}


}
