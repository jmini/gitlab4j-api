package org.gitlab4j.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.gitlab4j.api.mockserver.GitlabMockBridge;
import org.gitlab4j.api.mockserver.GitlabMockData;
import org.gitlab4j.api.mockserver.MockserverUtil;
import org.gitlab4j.api.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import software.xdev.mockserver.client.MockServerClient;

class UserTest {

    private MockServerClient mockServer;
    private GitLabApi api;
    private GitlabMockData data;

    @BeforeEach
    void init(TestInfo info) throws IOException {
        data = MockserverUtil.loadMockData();
        mockServer = MockserverUtil.initBeforeEach(info, data.getHostUrl());
        api = GitlabMockBridge.createClient(mockServer, data.getPersonalAccessToken());
    }

    @AfterEach
    void recordExpectations(TestInfo info) throws IOException {
        MockserverUtil.recordExpectationsAfterEach(info, mockServer);
    }

    @Test
    void testGetUser() throws Exception {
        User result = api.getUserApi().getUser(data.getUserId());
        Assertions.assertThat(result).isNotNull();
    }
}
