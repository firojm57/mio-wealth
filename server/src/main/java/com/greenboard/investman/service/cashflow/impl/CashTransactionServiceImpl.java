package com.greenboard.investman.service.cashflow.impl;

import com.greenboard.investman.model.cashflow.CashTransaction;
import com.greenboard.investman.model.cashflow.TaxProfile;
import com.greenboard.investman.model.cashflow.TransactionType;
import com.greenboard.investman.model.cashflow.TransferDetail;
import com.greenboard.investman.model.category.FinancialCategory;
import com.greenboard.investman.repository.cashflow.CashTransactionRepository;
import com.greenboard.investman.repository.category.FinancialCategoryRepository;
import com.greenboard.investman.service.cashflow.CashTransactionService;
import com.greenboard.investman.vo.cashflow.CashTransactionRequestVO;
import com.greenboard.investman.vo.cashflow.CashTransactionResponseVO;
import com.greenboard.investman.vo.cashflow.TaxProfileVO;
import com.greenboard.investman.vo.cashflow.TransferDetailVO;
import com.greenboard.investman.vo.common.PageResponseVO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CashTransactionServiceImpl implements CashTransactionService {

    private final CashTransactionRepository transactionRepository;
    private final FinancialCategoryRepository categoryRepository;

    public CashTransactionServiceImpl(CashTransactionRepository transactionRepository,
                                       FinancialCategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CashTransactionResponseVO createTransaction(CashTransactionRequestVO request) {
        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        CashTransaction transaction = CashTransaction.builder()
                .transactionType(request.getTransactionType())
                .title(request.getTitle())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .category(category)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .remarks(request.getRemarks())
                .tags(request.getTags())
                .build();

        if (request.getTransferDetail() != null) {
            TransferDetail transferDetail = TransferDetail.builder()
                    .transaction(transaction)
                    .source(request.getTransferDetail().getSource())
                    .destination(request.getTransferDetail().getDestination())
                    .transferKind(request.getTransferDetail().getTransferKind())
                    .build();
            transaction.setTransferDetail(transferDetail);
        }

        if (request.getTaxProfile() != null) {
            TaxProfile taxProfile = TaxProfile.builder()
                    .transaction(transaction)
                    .financialYear(request.getTaxProfile().getFinancialYear() != null
                            ? request.getTaxProfile().getFinancialYear()
                            : deriveFinancialYear(request.getTransactionDate()))
                    .taxHead(request.getTaxProfile().getTaxHead())
                    .isTaxable(request.getTaxProfile().isTaxable())
                    .build();
            transaction.setTaxProfile(taxProfile);
        }

        CashTransaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    public CashTransactionResponseVO updateTransaction(String id, CashTransactionRequestVO request) {
        CashTransaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cash transaction not found with id: " + id));

        FinancialCategory category = categoryRepository.findById(request.getCategoryCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + request.getCategoryCode()));

        existing.setTransactionType(request.getTransactionType());
        existing.setTitle(request.getTitle());
        existing.setAmount(request.getAmount());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setCategory(category);
        existing.setPeriodStart(request.getPeriodStart());
        existing.setPeriodEnd(request.getPeriodEnd());
        existing.setRemarks(request.getRemarks());
        existing.setTags(request.getTags());

        if (request.getTransferDetail() != null) {
            if (existing.getTransferDetail() == null) {
                TransferDetail td = TransferDetail.builder()
                        .transaction(existing)
                        .source(request.getTransferDetail().getSource())
                        .destination(request.getTransferDetail().getDestination())
                        .transferKind(request.getTransferDetail().getTransferKind())
                        .build();
                existing.setTransferDetail(td);
            } else {
                existing.getTransferDetail().setSource(request.getTransferDetail().getSource());
                existing.getTransferDetail().setDestination(request.getTransferDetail().getDestination());
                existing.getTransferDetail().setTransferKind(request.getTransferDetail().getTransferKind());
            }
        } else {
            existing.setTransferDetail(null);
        }

        if (request.getTaxProfile() != null) {
            if (existing.getTaxProfile() == null) {
                TaxProfile tp = TaxProfile.builder()
                        .transaction(existing)
                        .financialYear(request.getTaxProfile().getFinancialYear() != null
                                ? request.getTaxProfile().getFinancialYear()
                                : deriveFinancialYear(request.getTransactionDate()))
                        .taxHead(request.getTaxProfile().getTaxHead())
                        .isTaxable(request.getTaxProfile().isTaxable())
                        .build();
                existing.setTaxProfile(tp);
            } else {
                existing.getTaxProfile().setFinancialYear(request.getTaxProfile().getFinancialYear());
                existing.getTaxProfile().setTaxHead(request.getTaxProfile().getTaxHead());
                existing.getTaxProfile().setTaxable(request.getTaxProfile().isTaxable());
            }
        } else {
            existing.setTaxProfile(null);
        }

        CashTransaction updated = transactionRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteTransaction(String id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Cash transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CashTransactionResponseVO getTransactionById(String id) {
        return transactionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Cash transaction not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseVO<CashTransactionResponseVO> getTransactions(
            TransactionType type,
            String categoryCode,
            LocalDate fromDate,
            LocalDate toDate,
            String search,
            Pageable pageable) {

        Specification<CashTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (categoryCode != null && !categoryCode.isBlank() && !"ALL".equalsIgnoreCase(categoryCode)) {
                predicates.add(cb.equal(root.get("category").get("code"), categoryCode));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), toDate));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate remarksMatch = cb.like(cb.lower(root.get("remarks")), pattern);
                Predicate tagsMatch = cb.like(cb.lower(root.get("tags")), pattern);
                predicates.add(cb.or(titleMatch, remarksMatch, tagsMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CashTransaction> page = transactionRepository.findAll(spec, pageable);
        return PageResponseVO.from(page, this::mapToResponse);
    }

    private CashTransactionResponseVO mapToResponse(CashTransaction entity) {
        TransferDetailVO transferVO = null;
        if (entity.getTransferDetail() != null) {
            transferVO = TransferDetailVO.builder()
                    .source(entity.getTransferDetail().getSource())
                    .destination(entity.getTransferDetail().getDestination())
                    .transferKind(entity.getTransferDetail().getTransferKind())
                    .build();
        }

        TaxProfileVO taxVO = null;
        if (entity.getTaxProfile() != null) {
            taxVO = TaxProfileVO.builder()
                    .financialYear(entity.getTaxProfile().getFinancialYear())
                    .taxHead(entity.getTaxProfile().getTaxHead())
                    .isTaxable(entity.getTaxProfile().isTaxable())
                    .build();
        }

        return CashTransactionResponseVO.builder()
                .id(entity.getId())
                .transactionType(entity.getTransactionType())
                .title(entity.getTitle())
                .amount(entity.getAmount())
                .transactionDate(entity.getTransactionDate())
                .categoryCode(entity.getCategory() != null ? entity.getCategory().getCode() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .remarks(entity.getRemarks())
                .tags(entity.getTags())
                .transferDetail(transferVO)
                .taxProfile(taxVO)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String deriveFinancialYear(LocalDate date) {
        if (date == null) return null;
        int year = date.getYear();
        int month = date.getMonthValue();
        if (month >= 4) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}
