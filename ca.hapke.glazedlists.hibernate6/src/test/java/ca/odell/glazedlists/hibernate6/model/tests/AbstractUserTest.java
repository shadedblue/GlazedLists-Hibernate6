package ca.odell.glazedlists.hibernate6.model.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ca.odell.glazedlists.hibernate6.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import junit.framework.TestCase;

/**
 * @author Nathan Hapke
 */
@RunWith(Parameterized.class)
public abstract class AbstractUserTest extends TestCase {

	
	private static final boolean DEBUG = true;
	private SessionFactory sessionFactory;

	protected void setUp() throws Exception {
		// A SessionFactory is set up once for an application!
		
		// configures settings from hibernate.cfg.xml
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble
			// building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy(registry);
			e.printStackTrace();
		}
	}

	public SessionFactory getSessions() {
		if (sessionFactory == null) {
			try {
				setUp();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return sessionFactory;
	}
	/*
	 * HIBERNATE TEST RULE
	 */

	private Session session;

	public Session openSession() throws HibernateException {
		if (session == null || !session.isConnected()) {
			session = getSessions().openSession();
		}
		return session;
	}

	/*
	 * ABSTRACT USER TEST
	 */

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		QueryType[] types = QueryType.values();
		List<Object[]> result = new ArrayList<>(types.length);
		for (QueryType type : types) {
			result.add(new Object[] { type });
		}
		return result;
	}

	public enum QueryType {
		Criteria, CriteriaJoined, Jpql, JpqlJoined;
	}

	private QueryType mode;
	protected final String name = "Steve";

	protected int userNumber;
	protected int roleNumber;

	public AbstractUserTest(QueryType mode) {
		this.mode = mode;
	}

	protected void randomize() {
		userNumber = randomInt();
		roleNumber = randomInt();
	}

	protected int randomInt() {
		return (int) (10000 * Math.random());
	}

//	protected String getName() {
//		return name;
//	}

	protected String getUsername() {
		return name + userNumber;
	}

	protected String getEmailAddr() {
		return "box" + userNumber + "@" + name + ".ca";
	}

	protected String getRoleName() {
		return "u" + userNumber + "r" + roleNumber;
	}

	protected String getNickName() {
		return "u" + userNumber + "nn" + randomInt();
	}

	protected List<User> loadUser(EntityManager em, String userName) {
		switch (mode) {
		case Criteria:
			return loadUserCriteria(em, userName);
		case CriteriaJoined:
			return loadUserCriteriaJoined(em, userName);
		case Jpql:
			return loadUserJpql(em, userName);
		case JpqlJoined:
			return loadUserJpqlJoined(em, userName);
		}
		return null;
	}

	/**
	 * Loads a user with the given username.
	 */
	protected List<User> loadUserCriteria(EntityManager em, String userName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);

		Predicate usernamePredicate = cb.like(root.get("userName"), userName);
		cq.where(usernamePredicate);

		TypedQuery<User> query = em.createQuery(cq);
		List<User> result = query.getResultList();
		return result;
	}

	private List<User> loadUserCriteriaJoined(EntityManager em, String userName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> root = cq.from(User.class);

		Predicate usernamePredicate = cb.like(root.get("userName"), userName);
		cq.where(usernamePredicate);

		root.fetch("emailAddresses", JoinType.LEFT);
		root.fetch("roles", JoinType.LEFT);
		root.fetch("nickNames", JoinType.LEFT);
		cq.select(root);

		TypedQuery<User> query = em.createQuery(cq);
		List<User> result = query.getResultList();
		return result;
	}

	protected List<User> loadUserJpql(EntityManager em, String userName) {
		TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.userName = :un", User.class);
		q.setParameter("un", userName);
		List<User> result = q.getResultList();
		return result;
	}

	protected List<User> loadUserJpqlJoined(EntityManager em, String userName) {
		TypedQuery<User> q = em.createQuery(
				"SELECT u FROM User u LEFT JOIN FETCH u.emailAddresses LEFT JOIN FETCH u.nickNames LEFT JOIN FETCH u.roles WHERE u.userName = :un",
				User.class);
		q.setParameter("un", userName);
		List<User> result = q.getResultList();
		return result;
	}

	protected void log(String string) {
		if (!DEBUG)
			return;

		int length = string.length();
		StringBuilder sb = new StringBuilder(length);
		sb.append("*");
		for (int i = 1; i < length - 1; i++) {
			sb.append("-");
		}
		sb.append("*");

		String line = sb.toString();
		System.out.println();
		System.out.println(line);
		System.out.println(string);
		System.out.println(line);
		System.out.println();
	}
}
