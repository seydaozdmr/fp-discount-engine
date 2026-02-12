# Quick templates for Java 17 FP

## Replace nulls with Optional
```java
Optional<Discount> discount = find(id);
return discount
    .filter(Discount::isActive)
    .map(this::applyPolicy)
    .orElseGet(Discount::none);
```

## Loop → Stream pipeline
```java
List<RuleResult> results = rules.stream()
    .map(rule -> rule.apply(cart))
    .filter(RuleResult::applied)
    .sorted(comparing(RuleResult::priority).reversed())
    .toList();
```

## Compose with higher-order helpers
```java
Function<Cart, Cart> chain(Function<Cart, Cart>... steps) =
    cart -> Stream.of(steps).reduce(Function.identity(), Function::andThen).apply(cart);
```

## Algebraic-style modeling with records
```java
sealed interface Decision permits Accept, Reject, NeedsMoreInfo {}
record Accept(Price price) implements Decision {}
record Reject(String reason) implements Decision {}
record NeedsMoreInfo(String field) implements Decision {}
```

## Guard side effects
```java
Supplier<Instant> clock = Instant::now;
Function<Order, Result> calculate = order ->
    Result.of(pureCalculation(order, clock.get()))
          .peek(log::info);
```

## Minimal JUnit 5 property-ish test
```java
@ParameterizedTest
@ValueSource(ints = {0, 1, 10, 100})
void discountNeverNegative(int qty) {
  var cart = Cart.of(qty, Money.of(100));
  assertThat(engine.apply(cart).finalPrice()).isGreaterThanOrEqualTo(Money.zero());
}
```

## Refactor playbook (checklist)
- Make inputs/outputs immutable (`record`, unmodifiable collections).
- Extract pure function first; keep impure boundary thin (I/O, time).
- Replace branching with pattern-focused methods (`map`, `flatMap`, `filter`).
- Prefer declarative collectors over manual accumulation.
- Express failures explicitly (exception type, `Optional`, or `Either`-like pair).
- Add tests that lock expected laws (idempotence, associativity, monotonicity where relevant).
