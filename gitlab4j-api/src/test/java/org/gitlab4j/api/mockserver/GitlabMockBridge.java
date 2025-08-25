package org.gitlab4j.api.mockserver;

import org.gitlab4j.api.GitLabApi;

import software.xdev.mockserver.client.MockServerClient;

public class GitlabMockBridge {

    public static GitLabApi createClient(MockServerClient mockServer, String personalAccessToken) {
        return new GitLabApi("http://localhost:" + mockServer.getPort(), personalAccessToken);
    }
}
