package com.fernando.apipizaria2.services;

import com.fernando.apipizaria2.domain.dtos.ProdutoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.ProdutoResponseDTO;
import com.fernando.apipizaria2.domain.entities.Produto;
import com.fernando.apipizaria2.domain.enums.TamanhoProduto;
import com.fernando.apipizaria2.exceptions.ResourceNotFoundException;
import com.fernando.apipizaria2.repositories.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> findByTamanho(String tamanho, Pageable pageable) {
        TamanhoProduto t = TamanhoProduto.valueOf(tamanho.toUpperCase());
        return repository.findByTamanho(t, pageable).map(this::toDTO);
    }

    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO dto) {
        Produto entity = new Produto();
        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        entity.setPrecoBase(dto.precoBase());
        entity.setTamanho(TamanhoProduto.valueOf(dto.tamanho().toUpperCase()));
        return toDTO(repository.save(entity));
    }

    @Transactional
    public ProdutoResponseDTO update(Long id, ProdutoRequestDTO dto) {
        Produto entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        entity.setPrecoBase(dto.precoBase());
        entity.setTamanho(TamanhoProduto.valueOf(dto.tamanho().toUpperCase()));
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }
        repository.deleteById(id);
    }

    private ProdutoResponseDTO toDTO(Produto entity) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        dto.setPrecoBase(entity.getPrecoBase());
        dto.setTamanho(entity.getTamanho().name());
        return dto;
    }
}
