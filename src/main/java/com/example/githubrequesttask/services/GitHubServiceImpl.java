package com.example.githubrequesttask.services;

import com.example.githubrequesttask.models.Branch;
import com.example.githubrequesttask.models.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class GitHubServiceImpl implements GitHubService {

    private final WebClient webClient;

    public GitHubServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    @Override
    public Flux<Repository> getRepositories(String username) {
        return this.webClient.get().uri("/users/{username}/repos", username)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response ->
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .onStatus(HttpStatus.FORBIDDEN::equals, response ->
                        Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden access")))
                .bodyToFlux(JsonNode.class)
                .flatMap(repositoryJson -> {
                    String repoName = repositoryJson.get("name").asText();
                    String owner = repositoryJson.get("owner").get("login").asText();
                    return getBranches(owner, repoName)
                            .collectList()
                            .map(branches -> {
                                Repository repository = new Repository();
                                repository.setName(repoName);
                                repository.setOwner(owner);
                                repository.setBranches(branches);
                                return repository;
                            });
                });
    }

    private Flux<Branch> getBranches(String owner, String repoName) {
        return this.webClient.get()
                .uri("/repos/{owner}/{repoName}/branches", owner, repoName)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Branch.class);
    }
}
