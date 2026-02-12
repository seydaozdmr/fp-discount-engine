# fp-discount-engine

Java 17 Maven project for a functional discount engine with:

1. Sequential (compound) stacking
2. Best discount wins
3. Grouped stacking (exclusivity + global cap + audit trail)
4. FP core primitives (`Option`, `Result`, `Validation`, functional helpers)
5. Spring Boot API entrypoint for production-style usage

## Current architecture

### 1) FP core (`com.example.fpcore`)

- `Option<T>`: `some/none`, `map`, `flatMap`, `filter`, conversions
- `Result<T>`: `Success/Failure/Empty`, `map`, `flatMap`, `map2`, `sequence`, `traverse`, `lift`
- `Validation<T>`: error accumulation (`map2/map3`) for multi-error validation
- `Functions`: `compose`, `andThen`, `curry`, `curry3`, lift helpers

### 2) Monadic discount pipeline (`com.example.discount`)

- Rules evaluate as `Result<BigDecimal>` (`DiscountRule.evaluate`)
- Engines expose `Result`-based APIs:
  - `SequentialDiscountEngine.applySequentiallyResult`
  - `BestDiscountWinsEngine.pickBestResult`
  - `BestDiscountWinsEngine.applyBestResult`
  - `GroupedSelector.selectBestPerGroupResult`
  - `DiscountOrchestratorV2.priceResult`
- Existing non-Result methods are kept and delegate with `getOrThrow()` for backward compatibility.

### 3) Validation accumulation

- `DiscountValidation` validates context and rules, collecting multiple errors at once.
- `DiscountOrchestratorV2.priceValidated(...)` runs validation first, then pricing flow.

### 4) Production-style API (Spring Boot)

- Application: `com.example.discount.application.PricingApplication`
- Endpoint: `POST /api/pricing/quote`
- Service pipeline:
  - request validation (accumulated)
  - default rules + orchestrator
  - functional result mapping to HTTP response

## Run

### Run tests

```bash
mvn -q test
```

### Run demo scenarios

```bash
mvn -q -DskipTests compile
java -cp target/classes com.example.discount.Demo
```

### Run API

```bash
mvn spring-boot:run
```

Request example:

```bash
curl -X POST http://localhost:8080/api/pricing/quote \
  -H "Content-Type: application/json" \
  -d '{
    "subtotal": 1200.00,
    "vip": true,
    "hasCoupon": true
  }'
```

## Notes

- `OrderPricing` and `OrderContext` are immutable.
- Pricing behavior is composed through pure-ish functions and `Result` chaining.
- Failure, empty, and success flows are explicit and test-covered.
