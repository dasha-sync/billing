package api.controller;

import api.dto.common.ApiResponse;
import api.dto.subscription.CreateSubscriptionRequest;
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
@RequestMapping("/subscriptions")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            Principal principal) throws Exception {
        SubscriptionResponse subscription = subscriptionService.createSubscription(request, principal.getName());
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
