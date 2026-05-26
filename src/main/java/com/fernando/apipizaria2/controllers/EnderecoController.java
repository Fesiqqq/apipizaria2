package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.EnderecoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.EnderecoResponseDTO;
import com.fernando.apipizaria2.services.EnderecoService;
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
@RequestMapping("/v1/enderecos")
@Tag(name = "Endereços", description = "Endpoints para o mapeamento de endereços de entrega")
public class EnderecoController {

    private final EnderecoService service;

    public EnderecoController(EnderecoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar endereços", description = "Busca todos os endereços de entrega cadastrados no sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping
    public ResponseEntity<Page<EnderecoResponseDTO>> findAll(Pageable pageable) {
        Page<EnderecoResponseDTO> page = service.findAll(pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar endereço por ID", description = "Busca um endereço e qual cliente é dono dele.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EnderecoResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        EnderecoResponseDTO dto = service.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Procurar por CEP", description = "Busca a lista de endereços vinculados a um determinado CEP.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/cep")
    public ResponseEntity<Page<EnderecoResponseDTO>> findByCep(@RequestParam String cep, Pageable pageable) {
        Page<EnderecoResponseDTO> page = service.findByCep(cep, pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Vincular novo endereço", description = "Cria um novo endereço atrelado a um cliente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação"),
        @ApiResponse(responseCode = "404", description = "Cliente atrelado não foi encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PostMapping
    public ResponseEntity<EnderecoResponseDTO> create(@Valid @RequestBody EnderecoRequestDTO dto) {
        EnderecoResponseDTO created = service.create(dto);
        addHateoasLinks(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar endereço", description = "Altera a rua, número ou bairro de um endereço existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido ou erro de validação"),
        @ApiResponse(responseCode = "404", description = "Endereço ou Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EnderecoResponseDTO> update(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id,
            @Valid @RequestBody EnderecoRequestDTO dto) {
        EnderecoResponseDTO updated = service.update(id, dto);
        addHateoasLinks(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Remover endereço", description = "Exclui um endereço da base de dados.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(EnderecoResponseDTO dto) {
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EnderecoController.class).findById(dto.getId())).withSelfRel());
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ClienteController.class).findById(dto.getClienteId())).withRel("cliente"));
    }
}
