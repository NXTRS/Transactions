package com.example.transactionservice.service;

import com.example.transactionservice.messaging.PushTransactionNotification;
import com.example.transactionservice.model.TransactionDto;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.utils.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;

    @PushTransactionNotification
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        var savedEntity = transactionRepository.save(mapper.toEntity(transactionDto));
        return mapper.toDto(savedEntity);
    }

    public Page<TransactionDto> getTransactions(String userId, Pageable pageable) {
        var entities = transactionRepository.findAllByUserId(userId, pageable);
        return entities.map(mapper::toDto);
    }

    public TransactionDto getTransactionById(Long id) {
        var entityOptional = transactionRepository.findById(id);
        return mapper.toDto(entityOptional.orElse(null));
    }
}
