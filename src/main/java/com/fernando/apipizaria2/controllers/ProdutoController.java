package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.ProdutoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.ProdutoResponseDTO;
import com.fernando.apipizaria2.services.ProdutoService;
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
@RequestMapping(value = "/api/produtos", headers = "X-API-Version=1")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento do cardápio e produtos (Versão 1)")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os produtos", description = "Retorna o cardápio paginado com pizzas, bebidas e outros itens.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> findAll(Pageable pageable) {
        Page<ProdutoResponseDTO> page = service.findAll(pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar produto por ID", description = "Retorna os detalhes de um produto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        ProdutoResponseDTO dto = service.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Buscar produto pelo tamanho", description = "Filtra produtos informando o Enum de tamanho (PEQUENO, MEDIO, GRANDE, FAMILIA, UNICO).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/tamanho")
    public ResponseEntity<Page<ProdutoResponseDTO>> findByTamanho(@RequestParam String tamanho, Pageable pageable) {
        Page<ProdutoResponseDTO> page = service.findByTamanho(tamanho, pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Cadastrar novo produto", description = "Adiciona um item ao cardápio.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO created = service.create(dto);
        addHateoasLinks(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar produto", description = "Altera as propriedades de um produto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido ou erro de validação"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> update(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO updated = service.update(id, dto);
        addHateoasLinks(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Remover produto", description = "Exclui um produto do cardápio.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(ProdutoResponseDTO dto) {
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProdutoController.class).findById(dto.getId())).withSelfRel());
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProdutoController.class).findAll(Pageable.unpaged())).withRel("produtos"));
    }
}
