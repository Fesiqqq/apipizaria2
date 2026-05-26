package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.IngredienteRequestDTO;
import com.fernando.apipizaria2.domain.dtos.IngredienteResponseDTO;
import com.fernando.apipizaria2.services.IngredienteService;
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
@RequestMapping("/v1/ingredientes")
@Tag(name = "Ingredientes", description = "Endpoints para configuração de estoque e ingredientes das pizzas")
public class IngredienteController {

    private final IngredienteService service;

    public IngredienteController(IngredienteService service) {
        this.service = service;
    }

    @Operation(summary = "Listar ingredientes", description = "Retorna todos os ingredientes catalogados disponíveis para as pizzas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping
    public ResponseEntity<Page<IngredienteResponseDTO>> findAll(Pageable pageable) {
        Page<IngredienteResponseDTO> page = service.findAll(pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar ingrediente por ID", description = "Encontra um ingrediente único pelo identificador.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ingrediente encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<IngredienteResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        IngredienteResponseDTO dto = service.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Procurar por nome", description = "Pesquisa no catálogo por ingredientes que contenham o termo, como 'Mussarela'.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/busca")
    public ResponseEntity<Page<IngredienteResponseDTO>> findByNome(@RequestParam String nome, Pageable pageable) {
        Page<IngredienteResponseDTO> page = service.findByNomeContaining(nome, pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Cadastrar ingrediente", description = "Insere um novo sabor/ingrediente no sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ingrediente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PostMapping
    public ResponseEntity<IngredienteResponseDTO> create(@Valid @RequestBody IngredienteRequestDTO dto) {
        IngredienteResponseDTO created = service.create(dto);
        addHateoasLinks(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar dados do ingrediente", description = "Permite modificar a descrição ou o nome de um ingrediente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ingrediente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido ou erro de validação"),
        @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<IngredienteResponseDTO> update(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id,
            @Valid @RequestBody IngredienteRequestDTO dto) {
        IngredienteResponseDTO updated = service.update(id, dto);
        addHateoasLinks(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Remover ingrediente", description = "Retira o ingrediente do banco de dados.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ingrediente deletado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Ingrediente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(IngredienteResponseDTO dto) {
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(IngredienteController.class).findById(dto.getId())).withSelfRel());
    }
}
