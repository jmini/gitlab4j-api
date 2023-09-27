package org.gitlab4j.test;

import java.io.IOException;
import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class GitLabContainer extends GenericContainer<GitLabContainer> {

	public final String version;
	public final Version parsedVersion;
	public String rootUserToken;

	public GitLabContainer(String version) {
		super(DockerImageName.parse("gitlab/gitlab-ce:" + version));
		this.version = version;
		this.parsedVersion = new Version(version);
		
		this.withExposedPorts(8090)
			.withEnv("GITLAB_OMNIBUS_CONFIG", "gitlab_rails['initial_root_password']=\"Pass_w0rd\";gitlab_rails['lfs_enabled']=false;external_url 'http://localhost:8090';gitlab_rails['gitlab_shell_ssh_port'] = 9022")
			.waitingFor(
				Wait
				.forHttp("/")
				.forStatusCode(302)
				.forStatusCode(200)
				.withStartupTimeout(Duration.ofSeconds(300))
		);
	}

	public void initRootUserToken() {
		String now = "" + System.currentTimeMillis();
		String token = "tk-" + now;
		try {
			ExecResult result = this.execInContainer("gitlab-rails", "runner", "\"token = User.find_by_username('root').personal_access_tokens.create(scopes: ['api'], name: 'GitLab4J Token " + now + "', expires_at: 5.days.from_now); token.set_token('"+ token + "'); token.save!\"");
			if(result.getExitCode() != 0) {
				throw new IllegalStateException("Could not create personal token for the root user\n\nStd out logs:\n" + result.getStdout() + "\n\nStd err logs:\n" + result.getStderr());
			}
		} catch (UnsupportedOperationException | IOException | InterruptedException e) {
			throw new IllegalStateException("Could not create personal token for the root user", e);
		}
		this.rootUserToken = token;
	}

	public String url() {
		return "http://localhost:" + getMappedPort(8090) + "";
	}

	@Override
	public String toString() {
		return "GitLabContainer [version=" + version + ", parsedVersion=" + parsedVersion + ", url()= " + url() + " ]";
	}
}
