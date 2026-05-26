package com.fernando.apipizaria2.controllers;

import com.fernando.apipizaria2.domain.dtos.PedidoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.PedidoResponseDTO;
import com.fernando.apipizaria2.services.PedidoService;
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
@RequestMapping("/v1/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para criar e acompanhar o status dos pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os pedidos", description = "Retorna a lista completa de pedidos em formato paginado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> findAll(Pageable pageable) {
        Page<PedidoResponseDTO> page = service.findAll(pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar pedido por ID", description = "Traz os detalhes de um pedido, com os produtos e quantidades.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        PedidoResponseDTO dto = service.findById(id);
        addHateoasLinks(dto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Filtrar por status", description = "Busca pedidos filtrando pelo Enum de status (RECEBIDO, PREPARANDO, PRONTO, ENTREGUE).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação bem sucedida"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @GetMapping("/status")
    public ResponseEntity<Page<PedidoResponseDTO>> findByStatus(@RequestParam String status, Pageable pageable) {
        Page<PedidoResponseDTO> page = service.findByStatus(status, pageable);
        page.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Criar novo pedido", description = "Realiza um novo pedido para o cliente com os itens desejados. Suporta idempotência via header X-Idempotency-Key.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação"),
        @ApiResponse(responseCode = "404", description = "Cliente ou Produto não encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflito (Idempotência atingida, requisição duplicada)"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> create(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO created = service.create(dto);
        addHateoasLinks(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Atualizar status do pedido", description = "Avança ou altera o status de um pedido já existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> updateStatus(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id,
            @RequestParam String status) {
        PedidoResponseDTO updated = service.updateStatus(id, status);
        addHateoasLinks(updated);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Cancelar pedido", description = "Exclui um pedido do histórico do sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pedido deletado com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID inválido (negativo ou zero)"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não Autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "O ID deve ser um número positivo maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(PedidoResponseDTO dto) {
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PedidoController.class).findById(dto.getId())).withSelfRel());
        dto.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ClienteController.class).findById(dto.getClienteId())).withRel("cliente"));
    }
}
