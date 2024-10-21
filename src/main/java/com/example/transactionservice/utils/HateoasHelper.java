package com.example.transactionservice.utils;

import com.example.transactionservice.controller.TransactionController;
import com.example.transactionservice.model.TransactionDto;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.hateoas.IanaLinkRelations.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HateoasHelper {

    public static PagedModel<EntityModel<TransactionDto>> convertToEntityModel(int page, int size,
                                                                               Page<TransactionDto> transactionPage) {

        return PagedModel.of(
                transactionPage.getContent().stream()
                        .map(transactionDto -> EntityModel.of(
                                transactionDto,
                                linkTo(methodOn(TransactionController.class).getTransaction(transactionDto.id())).withSelfRel()))
                        .collect(Collectors.toList()),
                new PagedModel.PageMetadata(size, page, transactionPage.getTotalElements(), transactionPage.getTotalPages())
        );
    }

    @NotNull
    public static EntityModel<TransactionDto> getEntityModel(Long id, TransactionDto transactionDto) {
        var entityModel = EntityModel.of(transactionDto);
        entityModel.add(linkTo(methodOn(TransactionController.class)
                .getTransaction(id)).withSelfRel());

        return entityModel;
    }

    public static void addPaginationLinks(String userId, int page, int size, Page<TransactionDto> transactionPage,
                                          PagedModel<EntityModel<TransactionDto>> pagedModel) {

        pagedModel.add(linkTo(methodOn(TransactionController.class).getTransactions(userId, page, size)).withSelfRel());
        if (transactionPage.hasPrevious()) {
            addPaginationRelation(userId, page - 1, size, PREV, pagedModel);
        }
        if (transactionPage.hasNext()) {
            addPaginationRelation(userId, page + 1, size, NEXT, pagedModel);
        }

        if (!transactionPage.isEmpty()) {
            addPaginationRelation(userId, 0, size, FIRST, pagedModel);
            int totalPages = transactionPage.getTotalPages();
            addPaginationRelation(userId, totalPages == 0 ? 0 : totalPages - 1, size, LAST, pagedModel);
        }
    }

    private static void addPaginationRelation(String userId, int page, int size, LinkRelation link,
                                              PagedModel<EntityModel<TransactionDto>> pagedModel) {
        pagedModel.add(linkTo(methodOn(TransactionController.class)
                .getTransactions(userId, page, size)).withRel(link));
    }
}
