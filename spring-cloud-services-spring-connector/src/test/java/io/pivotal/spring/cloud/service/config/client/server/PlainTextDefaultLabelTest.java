package io.pivotal.spring.cloud.service.config.client.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import io.pivotal.spring.cloud.service.config.ConfigClientOAuth2ResourceDetails;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClient;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClientAutoConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigServerTestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
		"spring.profiles.active=plaintext,native", "spring.cloud.config.enabled=true",
		"spring.cloud.config.server.defaultL"
		+ "abel=default-label-test" })
public class PlainTextDefaultLabelTest {
	// @formatter:off
	private static final String nginxConfig = "server {\n"
			+ "    listen              80;\n"
			+ "    server_name         default-label.example.com;\n"
			+ "}";
	// @formatter:on

	@Autowired
	private ConfigClientProperties configClientProperties;

	@Autowired
	private ConfigClientOAuth2ResourceDetails resource;

	@LocalServerPort
	private int port;

	private PlainTextConfigClient configClient;

	@Before
	public void setup() {
		resource.setAccessTokenUri("http://localhost:" + port + "/oauth/token");
		configClientProperties.setName("app");
		configClientProperties.setProfile(null);
		configClientProperties.setUri("http://localhost:" + port);
		configClient = new PlainTextConfigClientAutoConfiguration()
				.plainTextConfigClient(resource, configClientProperties);
	}

	@Test
	public void shouldFindFileWithDefaultLabel() {
		Assert.assertEquals(nginxConfig,
				read(configClient.getConfigFile("default-label-nginx.conf")));
	}

	public String read(Resource resource) {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(resource.getInputStream()))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
