package com.example.camunda2.registration_process.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.CamundaOperateClientConfiguration;
import io.camunda.operate.auth.JwtAuthentication;
import io.camunda.operate.auth.JwtCredential;
import io.camunda.operate.auth.TokenResponseMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;

@Configuration
public class CamundaClientConfig {

    @Value("${identity.clientId}")
    private String clientId;

    @Value("${identity.clientSecret}")
    private String clientSecret;

    @Value("${tasklistUrl}")
    private String tasklistUrl;

    @Value("${operateUrl}")
    private String operateUrl;

    @Value("${keycloakUrl}")
    private String keycloakUrl;

    @Bean
    public CamundaTaskListClient camundaTaskListClient() throws TaskListException {

        return CamundaTaskListClient.builder()
                .taskListUrl(tasklistUrl)
                .selfManagedAuthentication(clientId, clientSecret, keycloakUrl)
                .build();
    }

    @Bean
    public CamundaOperateClient camundaOperateClient() throws MalformedURLException {
        URL operateApiUrl = new URL(operateUrl);
        URL authUrl = new URL(keycloakUrl);

        JwtCredential credentials = new JwtCredential(
                clientId,
                clientSecret,
                clientId,
                authUrl,
                null
        );

        ObjectMapper objectMapper = new ObjectMapper();
        TokenResponseMapper tokenResponseMapper = new TokenResponseMapper.JacksonTokenResponseMapper(objectMapper);

        JwtAuthentication authentication = new JwtAuthentication(credentials, tokenResponseMapper);

        CamundaOperateClientConfiguration configuration = new CamundaOperateClientConfiguration(
                authentication,
                operateApiUrl,
                objectMapper,
                HttpClients.createDefault()
        );

        return new CamundaOperateClient(configuration);
    }
}
