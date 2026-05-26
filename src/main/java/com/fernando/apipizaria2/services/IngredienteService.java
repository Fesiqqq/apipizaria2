package com.fernando.apipizaria2.services;

import com.fernando.apipizaria2.domain.dtos.IngredienteRequestDTO;
import com.fernando.apipizaria2.domain.dtos.IngredienteResponseDTO;
import com.fernando.apipizaria2.domain.entities.Ingrediente;
import com.fernando.apipizaria2.exceptions.ResourceNotFoundException;
import com.fernando.apipizaria2.repositories.IngredienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredienteService {

    private final IngredienteRepository repository;

    public IngredienteService(IngredienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<IngredienteResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public IngredienteResponseDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<IngredienteResponseDTO> findByNomeContaining(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toDTO);
    }

    @Transactional
    public IngredienteResponseDTO create(IngredienteRequestDTO dto) {
        Ingrediente entity = new Ingrediente();
        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        return toDTO(repository.save(entity));
    }

    @Transactional
    public IngredienteResponseDTO update(Long id, IngredienteRequestDTO dto) {
        Ingrediente entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado"));
        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Ingrediente não encontrado");
        }
        repository.deleteById(id);
    }

    private IngredienteResponseDTO toDTO(Ingrediente entity) {
        IngredienteResponseDTO dto = new IngredienteResponseDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        return dto;
    }
}
