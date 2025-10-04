package api.service;

import api.dto.card.AddCardRequest;
import api.dto.card.CardResponse;
import api.exception.GlobalException;
import api.model.Card;
import api.model.User;
import api.repository.CardRepository;
import api.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CardService {
  private final StripeService stripeService;
  private final CardRepository cardRepository;
  private final UserRepository userRepository;

  public CardResponse addCard(AddCardRequest request, Principal principal) throws Exception {
    User user = getUser(principal);
    Card card = stripeService.attachCard(user, request.getPaymentMethodId());
    return mapToDto(card);
  }

  public List<CardResponse> getCards(Principal principal) {
    User user = getUser(principal);
    return cardRepository.findByUser(user)
        .stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  private User getUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new GlobalException("User not found", "NOT_FOUND"));
  }

  private CardResponse mapToDto(Card card) {
    return new CardResponse(
        card.getId(),
        card.getBrand(),
        card.getLast4(),
        card.getExpMonth(),
        card.getExpYear());
  }
}
