package org.gitlab4j.api.mockserver;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.xdev.mockserver.client.MockServerClient;
import software.xdev.mockserver.mock.Expectation;
import software.xdev.mockserver.model.Header;
import software.xdev.mockserver.model.Headers;
import software.xdev.mockserver.model.HttpForward;
import software.xdev.mockserver.model.HttpForward.Scheme;
import software.xdev.mockserver.model.HttpRequest;
import software.xdev.mockserver.netty.MockServer;
import software.xdev.mockserver.serialization.ObjectMapperFactory;
import software.xdev.mockserver.serialization.model.ExpectationDTO;
import software.xdev.mockserver.serialization.model.HttpRequestDTO;
import software.xdev.mockserver.serialization.model.HttpResponseDTO;
import software.xdev.mockserver.serialization.model.RequestDefinitionDTO;

public class MockserverUtil {

    private static final String MOCKSERVER_FORWARD_TO_REAL_SERVER = "MOCKSERVER_FORWARD_TO_REAL_SERVER";
    private static final String MOCKDATA_FILE = "MOCKDATA_FILE";

    private static final Path MOCK_SERVER_EXPECTATIONS_ROOT = Paths.get("src/test/resources/mock-server");

    public static MockServerClient initBeforeEach(TestInfo info, String remoteHost) {
        MockServer server = new MockServer(9999);
        MockServerClient mockServerClient = new MockServerClient("localhost", server.getLocalPort());

        System.out.println("Mock server running on port " + server.getLocalPort());

        mockServerClient.reset();

        if (mockserverForwardToRealServer()) {
            mockServerClient
                    .when(HttpRequest.request())
                    .forward(HttpForward.forward()
                            .withScheme(Scheme.HTTPS)
                            .withHost("localhost")
                            .withPort(8888));
        } else {
            if (Files.isDirectory(MOCK_SERVER_EXPECTATIONS_ROOT)) {
                ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
                String prefix = getExpectationFilePrefix(info);
                try {
                    Files.list(MOCK_SERVER_EXPECTATIONS_ROOT)
                            .filter(p -> p.getFileName().toString().startsWith(prefix))
                            .sorted()
                            .map(p -> readExpectation(objectMapper, p))
                            .forEach(e -> mockServerClient.upsert(e));
                } catch (IOException e) {
                    throw new IllegalStateException("Can not read expectation files", e);
                }
            } else {
                System.out.println("No expectactions found, because " + MOCK_SERVER_EXPECTATIONS_ROOT.toAbsolutePath()
                        + " is not a directory");
            }
        }

        return mockServerClient;
    }

    public static void recordExpectationsAfterEach(TestInfo info, MockServerClient mockServer) throws IOException {
        if (mockserverForwardToRealServer()) {
            Expectation[] list = mockServer.retrieveRecordedExpectations(HttpRequest.request());

            String prefix = getExpectationFilePrefix(info);
            Files.createDirectories(MOCK_SERVER_EXPECTATIONS_ROOT);
            for (int i = 0; i < list.length; i++) {
                String id = prefix + String.format("%03d", i + 1);
                ExpectationDTO item = new ExpectationDTO(list[i]);
                item.setId(id);
                RequestDefinitionDTO request = item.getHttpRequest();
                if (request instanceof HttpRequestDTO) {
                    HttpRequestDTO httpRequest = (HttpRequestDTO) request;
                    httpRequest.setHeaders(new Headers());
                    httpRequest.setKeepAlive(null);
                    httpRequest.setProtocol(null);
                }
                HttpResponseDTO httpResponse = item.getHttpResponse();
                List<String> contentType = httpResponse.getHeaders().getValues("Content-Type");
                httpResponse.setHeaders(new Headers(new Header("Content-Type", contentType)));
                Path file = MOCK_SERVER_EXPECTATIONS_ROOT.resolve(id + ".json");
                Files.writeString(file, item.toString() + "\n");
            }
        }
    }

    private static boolean mockserverForwardToRealServer() {
        String value = System.getenv(MOCKSERVER_FORWARD_TO_REAL_SERVER);
        return value != null && value.toLowerCase().equals("true");
    }

    private static Expectation readExpectation(ObjectMapper objectMapper, Path file) {
        try {
            String content = Files.readString(file);
            ExpectationDTO dto = objectMapper.readValue(content, ExpectationDTO.class);
            return dto.buildObject();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the expectation file " + file, e);
        }
    }

    private static String getExpectationFilePrefix(TestInfo info) {
        String testMethodName = getTestMethodName(info);
        String testMethodClass = getTestMethodClass(info);
        String prefix = testMethodClass + "_" + testMethodName + "_";
        return prefix;
    }

    private static String getTestMethodClass(TestInfo info) {
        String testMethodName = info.getTestClass()
                .map(Class::getSimpleName)
                .orElseThrow(() -> new IllegalStateException("Could not find the test method class"));
        return testMethodName;
    }

    private static String getTestMethodName(TestInfo info) {
        String testMethodName = info.getTestMethod()
                .map(Method::getName)
                .orElseThrow(() -> new IllegalStateException("Could not find the test method name"));
        return testMethodName;
    }

    public static GitlabMockData loadMockData() {
        String value = System.getenv(MOCKDATA_FILE);
        Path file;
        if (value != null) {
            file = Paths.get(value);
            if (!Files.isReadable(file)) {
                throw new IllegalStateException("Can not load mock data file: " + file.toAbsolutePath() + ", check the "
                        + MOCKDATA_FILE + " environement value");
            }
        } else {
            file = Paths.get("src/test/resources/mock-config.properties");
        }
        return new GitlabMockData(file);
    }
}
