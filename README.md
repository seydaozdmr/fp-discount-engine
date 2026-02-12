# fp-discount-engine

A small Java 17 Maven project that demonstrates functional-programming style discount engines:

1) Sequential (compound) stacking  
2) Best discount wins  
3) Grouped stacking with:
   - Exclusivity matrix (group conflicts)
   - Max discount cap (global cap)
   - Audit trail (step-by-step)

## Run demos

```bash
mvn -q test
mvn -q -DskipTests package
java -cp target/fp-discount-engine-1.0.0.jar com.example.discount.Demo
```

## Notes

- `OrderPricing` and `OrderContext` are immutable.
- Rules are expressed as `DiscountRule` = (eligible predicate + calculate function).
- Engines compose behavior rather than mutating state.
