# Functional Programming in Java – Bölüm 1–5 Özet

Bu doküman, **Pierre‑Yves Saumont – Functional Programming in Java** kitabının
ilk 5 bölümünü, konuşmamız boyunca ele aldığımız örnekler ve pratik çıkarımlarla
birlikte özetler.

---

## Bölüm 1 – Why Functional Programming?

### Ana problem
- Java projelerindeki hataların çoğu:
  - `null`
  - mutable state
  - side‑effect
  - belirsiz metod davranışları
  yüzünden oluşur.

### FP’nin iddiası
> FP hataları azaltmaz, **hatanın oluşmasını zorlaştırır**.

### Temel çıkarımlar
- Boolean dönüşler bilgi kaybıdır
- `if / else` bolluğu domain’i gizler
- “Bu metod ne yapıyor?” sorusu net değilse tasarım yanlıştır

---

## Bölüm 2 – What Is Functional Programming?

### FP bir syntax değildir
- FP ≠ Stream + lambda
- FP = **düşünme biçimi**

### FP’nin 4 temel ilkesi
1. **Pure Function**
   - Aynı input → aynı output
   - Yan etki yok
2. **No Side Effects**
   - DB, log, state değişimi hesaplamadan ayrılmalı
3. **Immutability**
   - Nesneler değişmez, yenisi üretilir
4. **Referential Transparency**
   - Fonksiyon çağrısı, sonucu ile yer değiştirebilir

### OOP + FP
- OOP → yapı (structure)
- FP → davranış (behavior)
- DDD’de:
  - Entity / Aggregate → OOP
  - Domain logic → FP

---

## Bölüm 3 – Making Java More Functional

### Fonksiyonlar birinci sınıf vatandaş
- Java 8+ ile fonksiyonlar:
  - parametre olabilir
  - return edilebilir

### Temel functional interface’ler
- `Function<T, R>`
- `Predicate<T>`
- `Supplier<T>`
- `Consumer<T>`

### Higher‑Order Function
- Fonksiyon alıp fonksiyon döndüren fonksiyonlar
- `if` yerine **behavior selection**

### FP kazanımları
- Daha az `if`
- Daha okunur domain kuralları
- Test edilebilirlik

---

## Bölüm 4 – Working with Immutable Objects

### Mutable state problemi
- Kim değiştirdi?
- Ne zaman değişti?
- Hangi sırayla çağrıldı?

### Immutable yaklaşım
- Setter yok
- Her değişiklik yeni nesne üretir

```java
Order shipped = order.ship();
```

### Kazanımlar
- Thread‑safe
- Kolay debug
- FP pipeline uyumu

### FP zinciri (pipeline)
```java
order
  |> applyVipDiscount
  |> applyCampaign
  |> applyTax
```

Immutability olmadan bu zincir güvenli değildir.

---

## Bölüm 5 – Handling Absence of Value (Option)

### Null problemi
- Null bir değer değildir
- Gizli sözleşme yaratır
- Runtime’da patlar

### Option / Optional
- `Some(value)` / `None`
- Yokluk artık **type‑safe**

### Option + pipeline
```java
return orderOpt
    .filter(Order::isPaid)
    .map(this::applyDiscount)
    .map(Order::total);
```

- Zincir kendiliğinden durur
- Exception gerekmez

### map vs flatMap
- Fonksiyon Optional dönüyorsa → `flatMap`

### DDD notu
- Repo / service boundary’de `Optional`
- Entity field olarak genelde önerilmez

---

## Genel Özet

Bu 5 bölümün ana mesajı:

> **FP = daha az belirsizlik, daha çok güven**

Kazandıklarımız:
- Domain konuşur hale gelir
- Kurallar ayrışır
- Kod test yazmayı zorlamaz, **doğal hale getirir**
- Pipeline + immutability ile karmaşık iş kuralları sadeleşir

---

## Sonraki Bölüm
**Bölüm 6 – Error Handling the Functional Way**
- Result / Either
- Exception atmadan hata yönetimi
- Validation ve batch job senaryoları
