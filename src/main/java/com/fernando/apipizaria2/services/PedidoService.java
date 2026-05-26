package com.fernando.apipizaria2.services;

import com.fernando.apipizaria2.domain.dtos.ItemPedidoResponseDTO;
import com.fernando.apipizaria2.domain.dtos.PedidoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.PedidoResponseDTO;
import com.fernando.apipizaria2.domain.entities.Cliente;
import com.fernando.apipizaria2.domain.entities.ItemPedido;
import com.fernando.apipizaria2.domain.entities.Pedido;
import com.fernando.apipizaria2.domain.entities.Produto;
import com.fernando.apipizaria2.domain.enums.StatusPedido;
import com.fernando.apipizaria2.exceptions.ResourceNotFoundException;
import com.fernando.apipizaria2.repositories.ClienteRepository;
import com.fernando.apipizaria2.repositories.PedidoRepository;
import com.fernando.apipizaria2.repositories.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository repository, ClienteRepository clienteRepository, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> findByStatus(String status, Pageable pageable) {
        StatusPedido sp = StatusPedido.valueOf(status.toUpperCase());
        return repository.findByStatus(sp, pageable).map(this::toDTO);
    }

    @Transactional
    public PedidoResponseDTO create(PedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setDataHora(LocalDateTime.now());
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setCliente(cliente);

        if (dto.itens() != null) {
            dto.itens().forEach(itemDto -> {
                Produto produto = produtoRepository.findById(itemDto.produtoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
                ItemPedido item = new ItemPedido();
                item.setQuantidade(itemDto.quantidade());
                item.setPrecoUnitario(produto.getPrecoBase()); // Simplificação do preço
                item.setProduto(produto);
                item.setPedido(pedido);
                pedido.getItens().add(item);
            });
        }

        return toDTO(repository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO updateStatus(Long id, String status) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
        pedido.setStatus(StatusPedido.valueOf(status.toUpperCase()));
        return toDTO(repository.save(pedido));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido não encontrado");
        }
        repository.deleteById(id);
    }

    private PedidoResponseDTO toDTO(Pedido entity) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(entity.getId());
        dto.setDataHora(entity.getDataHora());
        dto.setStatus(entity.getStatus().name());
        dto.setClienteId(entity.getCliente().getId());

        if (entity.getItens() != null) {
            dto.setItens(entity.getItens().stream().map(this::toItemDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private ItemPedidoResponseDTO toItemDTO(ItemPedido item) {
        ItemPedidoResponseDTO dto = new ItemPedidoResponseDTO();
        dto.setId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setProdutoId(item.getProduto().getId());
        dto.setProdutoNome(item.getProduto().getNome());
        return dto;
    }
}
