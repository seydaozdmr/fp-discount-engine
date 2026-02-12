package com.example.discount.application.api;

import com.example.discount.PricingResult;
import com.example.discount.application.service.PricingService;
import com.example.fpcore.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/quote")
    public ResponseEntity<?> quote(@RequestBody PricingRequest request) {
        Result<PricingResult> result = pricingService.quote(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(PricingResponse.from(result.getOrThrow()));
        }
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse("No price could be calculated"));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(result.failureCause().getMessage()));
    }
}
