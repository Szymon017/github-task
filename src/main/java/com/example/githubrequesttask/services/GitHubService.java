package com.example.githubrequesttask.services;

import com.example.githubrequesttask.models.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface GitHubService {
    Flux<Repository> getRepositories(String username);
}
