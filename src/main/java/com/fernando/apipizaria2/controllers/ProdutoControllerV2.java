package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.ProdutoResponseDTO;
import com.fernando.apipizaria2.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/produtos", headers = "X-API-Version=2")
@Tag(name = "Produtos (V2)", description = "Endpoints versionados de produtos exigindo X-API-Version=2")
public class ProdutoControllerV2 {

    private final ProdutoService service;

    public ProdutoControllerV2(ProdutoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar produtos (V2)", description = "Retorna a listagem de produtos com formatação específica da versão 2.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> findAll(Pageable pageable) {
        Page<ProdutoResponseDTO> page = service.findAll(pageable);
        page.forEach(dto -> {
            dto.setDescricao(dto.getDescricao() + " (V2 API)");
            dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProdutoControllerV2.class).findAll(Pageable.unpaged())).withSelfRel());
        });
        return ResponseEntity.ok(page);
    }
}
