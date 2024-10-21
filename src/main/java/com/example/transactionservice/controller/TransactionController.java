package com.example.transactionservice.controller;

import com.example.transactionservice.model.TransactionDto;
import com.example.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.transactionservice.utils.HateoasHelper.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Create a new transaction
     */
    @PostMapping
    public ResponseEntity<EntityModel<TransactionDto>> createTransaction(@RequestBody @Valid TransactionDto transactionDto) {
        var createdTransactionDto = transactionService.createTransaction(transactionDto);
        var entityModel = EntityModel.of(createdTransactionDto);

        // Add HATEOAS self-link
        entityModel.add(linkTo(methodOn(TransactionController.class)
                .getTransaction(createdTransactionDto.id())).withSelfRel());

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    /**
     * Read a paginated list of transactions with HATEOAS links
     */
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<TransactionDto>>> getTransactions(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var transactionPage = transactionService.getTransactions(userId, PageRequest.of(page, size));

        // Convert each transaction to an EntityModel and add HATEOAS links
        var pagedModel = convertToEntityModel(page, size, transactionPage);

        // Adding pagination links
        addPaginationLinks(userId, page, size, transactionPage, pagedModel);

        return new ResponseEntity<>(pagedModel, HttpStatus.OK);
    }

    /**
     * Read a single transaction
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TransactionDto>> getTransaction(@PathVariable Long id) {
        var transaction = transactionService.getTransactionById(id);

        if (transaction == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(getEntityModel(id, transaction));
    }
}