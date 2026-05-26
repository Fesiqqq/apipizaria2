package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.entities.ApiKey;
import com.fernando.apipizaria2.repositories.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth/keys")
@Tag(name = "Gerenciamento de API Keys", description = "Endpoints para criar e listar chaves de acesso à API")
public class ApiKeyController {

    private final ApiKeyRepository repository;

    public ApiKeyController(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Gerar nova API Key", description = "Gera uma nova chave de acesso aleatória baseada em UUID para um dono específico.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Chave criada com sucesso")
    })
    @PostMapping
    public ResponseEntity<ApiKey> generateKey(@RequestParam String dono) {
        ApiKey newKey = new ApiKey();
        newKey.setChave("pk_" + UUID.randomUUID().toString().replace("-", ""));
        newKey.setDono(dono);
        newKey.setAtivo(true);
        newKey.setDataCriacao(LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(newKey));
    }

    @Operation(summary = "Listar todas as chaves", description = "Retorna o histórico de chaves criadas.")
    @GetMapping
    public ResponseEntity<List<ApiKey>> findAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
