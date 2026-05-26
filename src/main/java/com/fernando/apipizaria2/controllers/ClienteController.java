package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.ClienteRequestDTO;
import com.fernando.apipizaria2.domain.dtos.ClienteResponseDTO;
import com.fernando.apipizaria2.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/v1/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes da pizzaria")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista paginada com todos os clientes cadastrados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado (Falta API Key)"),
        @ApiResponse(responseCode = "429", description = "Muitas requisições (Rate Limit atingido)")
    })
    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> findAll(Pageable pageable) {
        Page<ClienteResponseDTO> page = service.findAll(pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar cliente por ID", description = "Retorna os detalhes de um cliente específico pelo seu ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        ClienteResponseDTO dto = service.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Buscar clientes por nome", description = "Realiza uma busca ignorando maiúsculas e minúsculas por clientes que contenham o termo pesquisado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/busca")
    public ResponseEntity<Page<ClienteResponseDTO>> findByNome(@RequestParam String nome, Pageable pageable) {
        Page<ClienteResponseDTO> page = service.findByNomeContaining(nome, pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Cadastrar novo cliente", description = "Cria um novo cliente na base de dados.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado"),
        @ApiResponse(responseCode = "409", description = "Idempotência (Requisição duplicada já processada) ou Email já existe")
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO created = service.create(dto);
        addHateoasLinks(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente validando o ID passado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido ou erro de validação"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO updated = service.update(id, dto);
        addHateoasLinks(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Remover cliente", description = "Exclui permanentemente um cliente pelo ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso (Sem conteúdo no corpo)"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(ClienteResponseDTO dto) {
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ClienteController.class).findById(dto.getId())).withSelfRel());
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ClienteController.class).findAll(Pageable.unpaged())).withRel("clientes"));
    }
}
