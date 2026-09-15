package com.apex.PaymentService.module.InvoiceLineItem.service;

import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemCreateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.request.InvoiceLineItemUpdateDto;
import com.apex.PaymentService.module.InvoiceLineItem.dto.response.InvoiceLineItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InvoiceLineItemService {

    InvoiceLineItemResponseDto createItem(InvoiceLineItemCreateDto dto);

    InvoiceLineItemResponseDto getItem(UUID id);

    List<InvoiceLineItemResponseDto> getItemsByInvoice(UUID invoiceId);

    Page<InvoiceLineItemResponseDto> getItemsByInvoicePaged(UUID invoiceId, Pageable pageable);

    InvoiceLineItemResponseDto updateItem(UUID id, InvoiceLineItemUpdateDto dto);

    void deleteItem(UUID id);

    void deleteAllItemsByInvoice(UUID invoiceId);
}
