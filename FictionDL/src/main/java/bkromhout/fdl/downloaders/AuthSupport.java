package bkromhout.fdl.downloaders;

/**
 * Interface which downloaders should implement if it is possible that some stories on their site require a user login
 * in order to download.
 */
public interface AuthSupport {

    /**
     * Login using specified username and password.
     * @param username Username.
     * @param password Password.
     */
    void addAuth(String username, String password);
}
