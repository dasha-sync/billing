package api.controller;

import api.dto.common.ApiResponse;
import api.dto.subscription.SubscriptionResponse;
import api.dto.subscription.UpdatePaymentMethodRequest;
import api.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/subscription")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class SubscriptionController {
  private final SubscriptionService subscriptionService;

  @PostMapping("/create/{cardId}")
  public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
      @PathVariable Long cardId,
      Principal principal) throws Exception {
    SubscriptionResponse subscription = subscriptionService.createSubscription(cardId, principal.getName());
    return ResponseEntity.ok(new ApiResponse<>("Subscription created successfully", subscription));
  }

  @PostMapping("/cancel")
  public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(Principal principal) throws Exception {
    SubscriptionResponse subscription = subscriptionService.cancelSubscription(principal.getName());
    return ResponseEntity.ok(new ApiResponse<>("Subscription cancelled successfully", subscription));
  }

  @PutMapping("/payment-method")
  public ResponseEntity<ApiResponse<SubscriptionResponse>> updatePaymentMethod(
      @Valid @RequestBody UpdatePaymentMethodRequest request,
      Principal principal) throws Exception {
    SubscriptionResponse subscription = subscriptionService.updatePaymentMethod(request, principal.getName());
    return ResponseEntity.ok(new ApiResponse<>("Payment method updated successfully", subscription));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(Principal principal) {
    SubscriptionResponse subscription = subscriptionService.getSubscription(principal.getName());
    return ResponseEntity.ok(new ApiResponse<>("User subscription", subscription));
  }
}
