package api.controller;

import api.service.UsageEventScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
public class UsageController {

//  private final UsageEventScheduler usageEventScheduler;
//
//  @PostMapping("/send/{subscriptionId}")
//  public ResponseEntity<String> sendSubscriptionUsage(@PathVariable Long subscriptionId) {
//    try {
//      usageEventScheduler.sendSubscriptionUsage(subscriptionId);
//      return ResponseEntity.ok("Usage event sent successfully for subscription " + subscriptionId);
//    } catch (Exception e) {
//      return ResponseEntity.badRequest().body("Error sending usage event: " + e.getMessage());
//    }
//  }
//
//  @PostMapping("/send-all")
//  public ResponseEntity<String> sendAllActiveSubscriptionsUsage() {
//    try {
//      usageEventScheduler.sendActiveSubscriptionsUsage();
//      return ResponseEntity.ok("Usage events sent successfully for all active subscriptions");
//    } catch (Exception e) {
//      return ResponseEntity.badRequest().body("Error sending usage events: " + e.getMessage());
//    }
//  }
}
