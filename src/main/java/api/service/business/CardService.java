package api.service.business;

import api.dto.card.AddCardRequest;
import api.dto.card.CardResponse;
import api.model.Card;
import api.model.User;
import api.repository.CardRepository;
import api.util.StripeProvider;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CardService {
  private final CardRepository cardRepository;
  private final StripeProvider stripeProvider;
  private final UserService userService;

  public CardResponse addCard(AddCardRequest request, Principal principal) throws Exception {
    User user = userService.getCurrentUser(principal);
    Card card = stripeProvider.attachCard(user, request.getPaymentMethodId());
    return mapToDto(card);
  }

  public List<CardResponse> getCards(Principal principal) {
    User user = userService.getCurrentUser(principal);
    return cardRepository.findByUser(user)
        .stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
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
