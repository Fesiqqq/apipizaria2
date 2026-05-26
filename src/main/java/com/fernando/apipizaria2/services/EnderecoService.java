package com.fernando.apipizaria2.services;

import com.fernando.apipizaria2.domain.dtos.EnderecoRequestDTO;
import com.fernando.apipizaria2.domain.dtos.EnderecoResponseDTO;
import com.fernando.apipizaria2.domain.entities.Cliente;
import com.fernando.apipizaria2.domain.entities.Endereco;
import com.fernando.apipizaria2.exceptions.ResourceNotFoundException;
import com.fernando.apipizaria2.repositories.ClienteRepository;
import com.fernando.apipizaria2.repositories.EnderecoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnderecoService {

    private final EnderecoRepository repository;
    private final ClienteRepository clienteRepository;

    public EnderecoService(EnderecoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<EnderecoResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public EnderecoResponseDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Endereco não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<EnderecoResponseDTO> findByCep(String cep, Pageable pageable) {
        return repository.findByCep(cep, pageable).map(this::toDTO);
    }

    @Transactional
    public EnderecoResponseDTO create(EnderecoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        Endereco entity = new Endereco();
        entity.setRua(dto.rua());
        entity.setNumero(dto.numero());
        entity.setBairro(dto.bairro());
        entity.setCep(dto.cep());
        entity.setCliente(cliente);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public EnderecoResponseDTO update(Long id, EnderecoRequestDTO dto) {
        Endereco entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereco não encontrado"));
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        entity.setRua(dto.rua());
        entity.setNumero(dto.numero());
        entity.setBairro(dto.bairro());
        entity.setCep(dto.cep());
        entity.setCliente(cliente);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Endereco não encontrado");
        }
        repository.deleteById(id);
    }

    private EnderecoResponseDTO toDTO(Endereco entity) {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setId(entity.getId());
        dto.setRua(entity.getRua());
        dto.setNumero(entity.getNumero());
        dto.setBairro(entity.getBairro());
        dto.setCep(entity.getCep());
        dto.setClienteId(entity.getCliente().getId());
        return dto;
    }
}
