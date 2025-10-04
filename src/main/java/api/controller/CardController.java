package api.controller;

import api.dto.card.AddCardRequest;
import api.dto.card.CardResponse;
import api.dto.common.ApiResponse;
import api.service.CardService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cards")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CardController {
  private final CardService cardService;

  @PostMapping("/add")
  public ResponseEntity<ApiResponse<CardResponse>> addCard(
      @Valid @RequestBody AddCardRequest request,
      Principal principal) throws Exception {
    CardResponse card = cardService.addCard(request, principal);
    return ResponseEntity.ok(new ApiResponse<>("Card added successfully", card));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(Principal principal) {
    List<CardResponse> cards = cardService.getCards(principal);
    return ResponseEntity.ok(new ApiResponse<>("User cards", cards));
  }
}
