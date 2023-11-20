package org.gitlab4j.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.gitlab4j.api.models.User;
import org.gitlab4j.api.models.Version;
import org.gitlab4j.test.GitLabContainer;
import org.gitlab4j.test.GitLabInvocationContextProvider;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GitLabInvocationContextProvider.class)
class UserApiIntegrationTest {

    private static final String TEST_USERNAME = "root";

    @TestTemplate
    public void testGetVersion(GitLabContainer container) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi(container.url(), container.rootUserToken);
        Version version = gitLabApi.getVersion();
        assertNotNull(version);
        System.out.format("version=%s, revision=%s%n", version.getVersion(), version.getRevision());
        assertEquals(container.version, version.getVersion());
        assertNotNull(version.getRevision());
    }

    @TestTemplate
    public void testGetCurrentUser(GitLabContainer container) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi(container.url(), container.rootUserToken);
        User currentUser = gitLabApi.getUserApi().getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(TEST_USERNAME, currentUser.getUsername());
    }

    @TestTemplate
    public void testLookupUser(GitLabContainer container) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi(container.url(), container.rootUserToken);
        User user = gitLabApi.getUserApi().getUser(TEST_USERNAME);
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
    }

}
