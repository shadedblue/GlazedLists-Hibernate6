package ca.odell.glazedlists.hibernate6.model.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Nathan Hapke
 */
@RunWith(Suite.class)
@SuiteClasses({ BasicUserTest.class, UserAndEmailsTest.class, UserAndFieldsTest.class })
public class AllUserTests {

}
