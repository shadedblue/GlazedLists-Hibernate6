/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate6.model;

import java.io.Serializable;
import java.util.List;

/**
 * Helper class for User.
 *
 * @author Holger Brands
 */
public class User implements Serializable {

    private static final long serialVersionUID = 0L;

	/** Id. */
	private Long id;

    private String userName;

    /** List of nicknames. */
    private List<String> nickNames;

    /** List of email addresses. */
    private List<Email> emailAddresses ;

    /** List of roles. */
    private List<Role> roles;

    /**
     * Default constructor for hibernate.
     */
	public User() {
    }

    /**
     * Constructor with name.
     */
    public User(String userName) {
        this.userName = userName;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    /**
     * Gets the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the email addresses.
     */
    public List<Email> getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * Sets the email addresses.
     */
    public void setEmailAddresses(List<Email> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    /**
     * Gets the nicknames.
     */
    public List<String> getNickNames() {
        return nickNames;
    }

    /**
     * Sets the nicknames.
     */
	public void setNickNames(List<String> nickNames) {
        this.nickNames = nickNames;
    }

    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }

    public void removeNickName(String nickName) {
        nickNames.remove(nickName);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        roles.add(role);
        role.addUser(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.removeUser(this);
    }

}
