package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.GiftCard;
import com.ptskart.api.repository.GiftCardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/giftcards")
public class GiftCardController {

    private final GiftCardRepository giftCardRepository;

    public GiftCardController(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GiftCard>>> getAllGiftCards() {
        return ResponseEntity.ok(ApiResponse.ok("Gift cards list retrieved", giftCardRepository.findAll()));
    }

    @GetMapping("/check/{code}")
    public ResponseEntity<ApiResponse<GiftCard>> checkBalance(@PathVariable String code) {
        return giftCardRepository.findByCodeIgnoreCase(code)
                .map(card -> ResponseEntity.ok(ApiResponse.ok("Gift card found with balance $" + card.getCurrentBalance(), card)))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Gift card code not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GiftCard>> createGiftCard(@RequestBody GiftCard giftCard) {
        giftCard.setCode(giftCard.getCode().toUpperCase().trim());
        if (giftCard.getCurrentBalance() == null) {
            giftCard.setCurrentBalance(giftCard.getInitialBalance());
        }
        return ResponseEntity.ok(ApiResponse.ok("Gift card issued successfully", giftCardRepository.save(giftCard)));
    }
}

