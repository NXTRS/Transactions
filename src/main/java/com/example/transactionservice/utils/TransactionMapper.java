package com.example.transactionservice.utils;

import com.example.transactionservice.entity.TransactionEntity;
import com.example.transactionservice.model.TransactionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDto toDto(TransactionEntity transactionEntity);
    TransactionEntity toEntity(TransactionDto transactionDto);

}
