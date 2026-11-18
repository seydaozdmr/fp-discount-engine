# FP Alıştırma Defteri

Saumont'un *Functional Programming in Java* kitabındaki kavramlara göre sıralanmış, gerçek hayat senaryolarıyla 30 alıştırma.
Çözümü `fpcore` tipleriyle (List, Result, Option, Validation, TailCall, LazyStream) yaz; kabul kriterlerini JUnit testine çevir.

> Etkileşimli sürüm (filtre ve ilerleme takibi): [alistirma-defteri.html](alistirma-defteri.html), tarayıcıda yerel olarak aç.
> Kural: döngü yok, null yok, exception yok, mutation yok.

## İçindekiler

- **Seviye 1 · Saf fonksiyonlar ve kompozisyon** (Bölüm 2–3)
  - [ ] [#01 KDV hesaplayıcı](#01-kdv-hesaplayıcı)
  - [ ] [#02 Fiyat hattı (pipeline)](#02-fiyat-hattı-pipeline)
  - [ ] [#03 Kargo ücreti](#03-kargo-ücreti)
  - [ ] [#04 URL slug üretici](#04-url-slug-üretici)
  - [ ] [#05 Sıralama kuralı kurucu](#05-sıralama-kuralı-kurucu)
- **Seviye 2 · Özyineleme ve fold** (Bölüm 4–5)
  - [ ] [#06 Sepet toplamı](#06-sepet-toplamı)
  - [ ] [#07 Kelime frekansı](#07-kelime-frekansı)
  - [ ] [#08 Hesap dökümü ve yürüyen bakiye](#08-hesap-dökümü-ve-yürüyen-bakiye)
  - [ ] [#09 Klasör boyutu](#09-klasör-boyutu)
  - [ ] [#10 Taksit planı](#10-taksit-planı)
- **Seviye 3 · Hata yönetimi: Option, Result, Validation** (Bölüm 6–7, 15)
  - [ ] [#11 Profilden şehir okuma](#11-profilden-şehir-okuma)
  - [ ] [#12 Güvenli sayı okuma](#12-güvenli-sayı-okuma)
  - [ ] [#13 Kayıt formu doğrulama](#13-kayıt-formu-doğrulama)
  - [ ] [#14 CSV sipariş içe aktarma](#14-csv-sipariş-içe-aktarma)
  - [ ] [#15 Ortam ayarları okuma](#15-ortam-ayarları-okuma)
  - [ ] [#16 Döviz çevirici](#16-döviz-çevirici)
- **Seviye 4 · Koleksiyonlar ve tembellik** (Bölüm 8–9)
  - [ ] [#17 Sayfalama](#17-sayfalama)
  - [ ] [#18 Not ortalaması ve karne](#18-not-ortalaması-ve-karne)
  - [ ] [#19 Log tarama](#19-log-tarama)
  - [ ] [#20 Sonsuz fatura numarası](#20-sonsuz-fatura-numarası)
  - [ ] [#21 Pahalı hesaplamayı önbelleğe alma](#21-pahalı-hesaplamayı-önbelleğe-alma)
- **Seviye 5 · Durum ve etkiler** (Bölüm 12–13)
  - [ ] [#22 Test edilebilir kupon kodu](#22-test-edilebilir-kupon-kodu)
  - [ ] [#23 Otopark bariyeri](#23-otopark-bariyeri)
  - [ ] [#24 Sepet olay kaydı](#24-sepet-olay-kaydı)
  - [ ] [#25 Rapor üret, sonra yaz](#25-rapor-üret-sonra-yaz)
  - [ ] [#26 Yeniden deneme politikası](#26-yeniden-deneme-politikası)
- **Seviye 6 · Gerçek hayat mini projeleri** (Hepsi bir arada)
  - [ ] [#27 Kargo takip durumu](#27-kargo-takip-durumu)
  - [ ] [#28 Fatura motoru](#28-fatura-motoru)
  - [ ] [#29 Hız sınırlayıcı (rate limiter)](#29-hız-sınırlayıcı-rate-limiter)
  - [ ] [#30 Toplantı çakışma kontrolü](#30-toplantı-çakışma-kontrolü)

---

## Seviye 1 · Saf fonksiyonlar ve kompozisyon

*Bölüm 2–3* · Yan etkisiz fonksiyon yazma, compose/andThen, currying. Her şeyin temeli.

### 01 KDV hesaplayıcı

**E-ticaret · saf fonksiyon** · Kavramlar: `saf fonksiyon`, `BigDecimal` · Kitap: 2.1

**Senaryo.** Ürün fiyatına KDV ekleyen bir fonksiyon lazım. Oran sistemden okunuyor ama fonksiyonun kendisi hiçbir dış duruma dokunmamalı.

**Görev.** Oranı parametre alan, fiyat döndüren saf bir fonksiyon yaz. Sonuç 2 haneye HALF_UP yuvarlanmalı.

```java
Function<BigDecimal, BigDecimal> addVat(BigDecimal rate)
```

| Girdi | Beklenen |
|---|---|
| `addVat(0.20).apply(100.00)` | 120.00 |
| `addVat(0.10).apply(19.99)` | 21.99 |
| `addVat(0.00).apply(5)` | 5.00 |

<details><summary>İpucu</summary>

Oranı dışarıdan alan bir fonksiyon döndür: rate -> price -> price.multiply(ONE.add(rate)).setScale(2, HALF_UP). Static bir alan ya da singleton'dan oran okursan fonksiyon saf olmaz.

</details>

### 02 Fiyat hattı (pipeline)

**E-ticaret · kompozisyon** · Kavramlar: `compose`, `andThen` · Kitap: 2.2–2.3

**Senaryo.** Sepet fiyatı sırayla: kampanya indirimi → KDV → kuruş yuvarlama. Adımların sırası iş kuralı; değişmesi kolay olmalı.

**Görev.** Üç küçük fonksiyon yaz ve bunları tek bir Function'a kompoze et. Sonra sırayı değiştirip (önce KDV sonra indirim) sonucun farkını gözle.

```java
Function<BigDecimal, BigDecimal> pipeline = discount(0.10).andThen(addVat(0.20)).andThen(round2)
```

| Girdi | Beklenen |
|---|---|
| `pipeline.apply(100)` | 108.00 |
| `ters sıra: addVat.andThen(discount)` | 108.00 (yüzdelerde aynı!) |
| `discount sabit 10 TL ise` | sıra sonucu değiştirir |

<details><summary>İpucu</summary>

Yüzde indirimler çarpma olduğu için yer değiştirebilir (commutative); sabit tutarlı indirimde (−10 TL) değiştiremez. Bu, kompozisyon sırasının neden iş kuralı olduğunu gösterir.

</details>

### 03 Kargo ücreti

**Lojistik · currying** · Kavramlar: `currying`, `kısmi uygulama` · Kitap: 2.3.6–2.3.8

**Senaryo.** Kargo ücreti bölgeye, desiye ve teslim hızına bağlı. Bölge oturum başında bilinir, desi sepete göre değişir.

**Görev.** Üç parametreli fonksiyonu curried biçimde yaz; bölgeyi önceden uygulayıp tek parametreli hesaplayıcılar üret.

```java
Function<Region, Function<Speed, Function<Integer, BigDecimal>>> shippingFee
```

| Girdi | Beklenen |
|---|---|
| `shippingFee(MARMARA)(STANDARD)(3)` | 45.00 |
| `shippingFee(DOGU)(EXPRESS)(3)` | 112.50 |
| `istanbul = shippingFee(MARMARA)(STANDARD); istanbul(1)` | 35.00 |

<details><summary>İpucu</summary>

Taban ücret = bölge tabanı + desi × birim; EXPRESS çarpanı 1.5. Kısmi uygulama sayesinde sepet ekranı sadece desiyi bilen bir fonksiyon alır.

</details>

### 04 URL slug üretici

**CMS / blog · string dönüşümleri** · Kavramlar: `compose`, `saf fonksiyon` · Kitap: 2.2

**Senaryo.** Blog yazısı başlığından URL üretilecek: "Java'da Fonksiyonel Programlama!" → "javada-fonksiyonel-programlama".

**Görev.** trim, Türkçe karakter dönüşümü, küçük harf, noktalama temizliği ve boşluk → tire adımlarını ayrı fonksiyonlar olarak yazıp compose et.

```java
Function<String, String> slugify
```

| Girdi | Beklenen |
|---|---|
| `"  Çok Güzel Şeyler  "` | "cok-guzel-seyler" |
| `"Java'da FP!"` | "javada-fp" |
| `"a  --  b"` | "a-b" |

<details><summary>İpucu</summary>

toLowerCase(new Locale("tr")) kullanmazsan "I" yanlış dönüşür. Her adımı tek tek test edebilmek kompozisyonun asıl kazancı.

</details>

### 05 Sıralama kuralı kurucu

**Ürün listeleme · higher-order** · Kavramlar: `higher-order`, `Comparator` · Kitap: 2.3

**Senaryo.** Ürün listesi kullanıcının seçimine göre sıralanır: önce stokta olanlar, sonra fiyat, eşitse puan (azalan).

**Görev.** Seçimlerden (List<SortKey>) bir Comparator üreten fonksiyon yaz. Comparator'ları foldLeft ile birleştir.

```java
Comparator<Product> comparatorFor(List<SortKey> keys)
```

| Girdi | Beklenen |
|---|---|
| `[IN_STOCK, PRICE_ASC]` | stoktakiler önce, kendi içinde ucuzdan pahalıya |
| `[] (boş)` | sıra değişmez (0 döndüren comparator) |
| `[RATING_DESC]` | puanı yüksek olan önce |

**Projede:** Projede DiscountRanking.BEST_FIRST aynı fikir: amount → priority → ruleName.

<details><summary>İpucu</summary>

Birim eleman (a, b) -> 0, birleştirme thenComparing. Bu bir monoid: fold için tam uygun.

</details>

---

## Seviye 2 · Özyineleme ve fold

*Bölüm 4–5* · Döngü yerine fold; derin özyinelemede TailCall ile stack güvenliği.

### 06 Sepet toplamı

**E-ticaret · foldLeft** · Kavramlar: `foldLeft` · Kitap: 3.3.5, 5.6

**Senaryo.** Sepet kalemleri (birim fiyat × adet) toplanacak; aynı zamanda toplam adet de lazım.

**Görev.** Tek bir foldLeft ile hem toplam tutarı hem toplam adedi hesapla (Tuple biriktirici).

```java
Tuple<BigDecimal, Integer> totals(List<CartLine> lines)
```

| Girdi | Beklenen |
|---|---|
| `[(10.00 ×2), (5.50 ×1)]` | (25.50, 3) |
| `[]` | (0, 0) |
| `[(0.10 ×3)]` | (0.30, 3) — double kullanma! |

<details><summary>İpucu</summary>

Başlangıç değeri new Tuple<>(ZERO, 0); fonksiyon acc -> line -> new Tuple<>(acc._1.add(...), acc._2 + line.qty()). İki ayrı döngü yerine tek geçiş.

</details>

### 07 Kelime frekansı

**Metin analizi · fold + Map** · Kavramlar: `foldLeft`, `groupBy` · Kitap: 5.6, 8.5

**Senaryo.** Müşteri yorumlarında en sık geçen 5 kelime raporlanacak (stop-word'ler hariç).

**Görev.** Metni kelimelere ayır, filtrele, frekans tablosunu fold ile kur, en sık 5 tanesini döndür.

```java
List<Tuple<String, Integer>> topWords(String text, int n, Set<String> stopWords)
```

| Girdi | Beklenen |
|---|---|
| `"kargo hızlı kargo iyi", n=1` | [(kargo, 2)] |
| `"ve ve ve" (ve stop-word)` | [] |
| `eşit frekans` | alfabetik sıra (deterministik) |

<details><summary>İpucu</summary>

Eşitlikte sırayı sabitlemeyi unutma; DiscountRanking'deki üçüncü anahtar gibi. Map'i mutable güncellemek yerine her adımda yeni değer üret ya da groupBy kullan.

</details>

### 08 Hesap dökümü ve yürüyen bakiye

**Bankacılık · scanLeft** · Kavramlar: `foldLeft`, `scanLeft` · Kitap: 5.6

**Senaryo.** Banka ekstresinde her işlemden sonraki bakiye gösterilir. Açılış bakiyesi ve işlem listesi verili.

**Görev.** foldLeft ile scanLeft yaz (ara sonuçların hepsini döndüren fold) ve yürüyen bakiyeyi hesapla.

```java
<A, B> List<B> scanLeft(List<A> list, B zero, Function<B, Function<A, B>> f)
```

| Girdi | Beklenen |
|---|---|
| `açılış 100, [+50, −30, −200]` | [100, 150, 120, −80] |
| `boş liste` | [100] |
| `10 000 işlem` | StackOverflow yok |

<details><summary>İpucu</summary>

Biriktirici, ters sırada tutulan bir liste olsun: acc.cons(f(acc.head)(a)); sonda reverse. Böylece her adım O(1).

</details>

### 09 Klasör boyutu

**Dosya sistemi · ağaç özyinelemesi** · Kavramlar: `özyineleme`, `fold` · Kitap: 4, 10

**Senaryo.** Bir klasör ağacında toplam boyut ve en büyük dosya bulunacak. Klasörler iç içe ve sayısı belirsiz.

**Görev.** Node = File(name, size) | Dir(name, List<Node>) tipini tanımla; boyutu ve en büyük dosyayı özyinelemeli fold ile bul.

```java
long totalSize(Node n);  Option<File> largest(Node n)
```

| Girdi | Beklenen |
|---|---|
| `Dir[File 10, Dir[File 30, File 5]]` | 45 ; File 30 |
| `boş Dir` | 0 ; None |
| `tek File 7` | 7 ; Some(File 7) |

<details><summary>İpucu</summary>

Bir sum type: her durumu ayrı ele al. largest için Option'ları birleştirirken boş klasör None verir; null döndürme.

</details>

### 10 Taksit planı

**Fintech · stack-safe özyineleme** · Kavramlar: `TailCall`, `özyineleme` · Kitap: 4.2

**Senaryo.** Kredi taksit planı ay ay üretilir: her ay faiz eklenir, taksit düşülür. 360 aylık konut kredisi de olabilir.

**Görev.** Planı özyinelemeli yaz; sonra TailCall ile stack-safe hâle getir ve 100 000 adımla dene.

```java
List<Installment> plan(BigDecimal principal, BigDecimal monthlyRate, BigDecimal payment)
```

| Girdi | Beklenen |
|---|---|
| `1000, %1, 340` | 3 taksit, sonuncusu kalan tutar |
| `taksit < faiz` | sonsuz döngü yerine Result.failure |
| `çok küçük taksit` | TailCall ile StackOverflow yok |

<details><summary>İpucu</summary>

Önce saf bir step(state) yaz, sonra go(acc, state) ile ret/sus kullan. Taksitin faizi karşılamadığı durumu döngüye girmeden yakala.

</details>

---

## Seviye 3 · Hata yönetimi: Option, Result, Validation

*Bölüm 6–7, 15* · null ve exception yerine değer döndürme; kısa devre ile hata biriktirme farkı.

### 11 Profilden şehir okuma

**Kullanıcı verisi · Option zinciri** · Kavramlar: `Option`, `flatMap` · Kitap: 6.3

**Senaryo.** user.getAddress().getCity().getName() zinciri, adres ya da şehir null olduğunda patlıyor.

**Görev.** Her getter'ı Option döndürecek şekilde sar ve zinciri flatMap ile kur. Şehir yoksa "Bilinmiyor" göster.

```java
String cityLabel(Option<User> user)
```

| Girdi | Beklenen |
|---|---|
| `adres var, şehir var` | "İzmir" |
| `adres yok` | "Bilinmiyor" |
| `user None` | "Bilinmiyor" |

<details><summary>İpucu</summary>

user.flatMap(User::address).flatMap(Address::city).map(City::name).getOrElse("Bilinmiyor"). Hiç if yok.

</details>

### 12 Güvenli sayı okuma

**Form girdisi · Result** · Kavramlar: `Result`, `total fonksiyon` · Kitap: 7.4

**Senaryo.** Kullanıcı adet alanına "üç", "-2" ya da "99999999999" yazabiliyor.

**Görev.** String'den pozitif int'e çeviren total fonksiyonu yaz; her hata durumu kendi mesajını taşısın.

```java
Result<Integer> parseQuantity(String raw)
```

| Girdi | Beklenen |
|---|---|
| `"3"` | Success(3) |
| `"üç"` | Failure("adet sayı olmalı: üç") |
| `"-2"` | Failure("adet pozitif olmalı: -2") |
| `null` | Failure("adet boş olamaz") |

**Projede:** Projedeki fpcore.Assertion ile doğrudan yazabilirsin.

<details><summary>İpucu</summary>

Assertion.assertNotNull → parse (try/catch tek yerde) → Assertion.assertCondition. Hata mesajında değeri de göster (15.2.3).

</details>

### 13 Kayıt formu doğrulama

**Web · hata biriktirme** · Kavramlar: `Validation`, `map3` · Kitap: 7, 15.1

**Senaryo.** Üyelik formunda e-posta, şifre ve yaş var. Kullanıcı tüm hatalarını tek seferde görmeli.

**Görev.** Her alan için Result döndüren doğrulayıcılar yaz, sonra Validation.map3 ile hataları birleştir.

```java
Validation<Member> register(String email, String password, String age)
```

| Girdi | Beklenen |
|---|---|
| `geçerli üçü` | valid(Member) |
| `kötü e-posta + kısa şifre` | 2 hata birlikte |
| `yaş "abc" ve 15` | alan içinde ilk hata: "yaş sayı olmalı" |

<details><summary>İpucu</summary>

Alan içinde Result.flatMap (sırayla, ilk hatada dur), alanlar arasında Validation (hepsini topla). PricingService.validateRequest ile aynı kalıp.

</details>

### 14 CSV sipariş içe aktarma

**Veri entegrasyonu · traverse** · Kavramlar: `traverse`, `sequence`, `Result` · Kitap: 8.3

**Senaryo.** Tedarikçi her gün sipariş CSV'si gönderiyor. Bir satır bozuksa dosyanın tamamı reddedilmeli ve hangi satır olduğu söylenmeli.

**Görev.** Satır → Result<Order> parse fonksiyonunu yaz; List.traverse ile Result<List<Order>> elde et. Hata mesajı satır numarasını içersin.

```java
Result<List<Order>> importCsv(List<String> lines)
```

| Girdi | Beklenen |
|---|---|
| `3 geçerli satır` | Success([o1, o2, o3]) |
| `2. satırda fiyat "x"` | Failure("satır 2: fiyat geçersiz: x") |
| `boş dosya` | Success([]) |

<details><summary>İpucu</summary>

Satır numarası için önce zipWith(range(1, n+1)) ya da index'li bir Tuple listesi üret. Alternatif: tüm hatalı satırları listelemek istersen Validation ile traverse yaz.

</details>

### 15 Ortam ayarları okuma

**Backend · yapılandırma** · Kavramlar: `Result`, `mapFailure` · Kitap: 15.2

**Senaryo.** Uygulama DB_URL, DB_POOL_SIZE ve FEATURE_FLAGS ortam değişkenlerini okuyor. Eksik ya da hatalı değerde açılırken ne yanlış olduğunu söylemeli.

**Görev.** Map<String,String> kaynağından tipli okuyucular yaz: getString, getInt (aralık kontrolüyle), getList. Hepsi hatada değişken adını ve değeri göstersin.

```java
Result<DbConfig> readDbConfig(Map<String, String> env)
```

| Girdi | Beklenen |
|---|---|
| `tamamı doğru` | Success(DbConfig) |
| `DB_POOL_SIZE=abc` | Failure("Invalid value for DB_POOL_SIZE: abc") |
| `DB_URL yok` | Failure("Property DB_URL not found") |

**Projede:** CampaignProperties.validate() aynı ilkeyle yazıldı.

<details><summary>İpucu</summary>

Okuma kaynağını parametre yap (Map), System.getenv'e doğrudan bağlanma; böylece test edebilirsin. Etki (getenv) en dışta.

</details>

### 16 Döviz çevirici

**Fintech · map2** · Kavramlar: `map2`, `lift` · Kitap: 7.6, 8.2

**Senaryo.** Tutar TRY → EUR çevrilecek. Tutar kullanıcıdan, kur bir servisten geliyor; ikisi de başarısız olabilir.

**Görev.** İki Result'ı map2 ile birleştiren convert fonksiyonu yaz. Sonra lift2 ile normal (a, b) -> c fonksiyonunu Result dünyasına taşı.

```java
Result<BigDecimal> convert(Result<BigDecimal> amount, Result<BigDecimal> rate)
```

| Girdi | Beklenen |
|---|---|
| `Success(100), Success(0.028)` | Success(2.80) |
| `Failure(kur servisi kapalı), Success(..)` | Failure(kur servisi kapalı) |
| `Empty, Success(..)` | Empty |

<details><summary>İpucu</summary>

map2(ra, rb, a -> b -> a.multiply(b).setScale(2, HALF_UP)). İçeride if yok; başarısızlık yayılımını Result yapıyor.

</details>

---

## Seviye 4 · Koleksiyonlar ve tembellik

*Bölüm 8–9* · traverse/sequence, zip, groupBy; sonsuz ve lazy veri, memoization.

### 17 Sayfalama

**REST API · List işlemleri** · Kavramlar: `drop`, `take`, `splitAt` · Kitap: 5, 8.4

**Senaryo.** Ürün listesi ?page=3&size=20 parametreleriyle sayfa sayfa döndürülüyor; toplam sayfa sayısı da lazım.

**Görev.** Saf bir paginate fonksiyonu yaz; geçersiz sayfa ya da boyut için Result.failure döndür.

```java
Result<Page<T>> paginate(List<T> items, int page, int size)
```

| Girdi | Beklenen |
|---|---|
| `45 öğe, page=3, size=20` | 5 öğe, totalPages=3 |
| `page=0` | Failure("page 1'den başlar") |
| `page=4 (aralık dışı)` | boş sayfa ya da Failure (kararını ver) |

<details><summary>İpucu</summary>

drop((page-1)*size).take(size). Aralık dışı sayfa iş kararı: boş liste mi, hata mı? İkisini de savunabilirsin; testle belgeye dök.

</details>

### 18 Not ortalaması ve karne

**Eğitim · zip, map, groupBy** · Kavramlar: `zipWith`, `groupBy`, `map` · Kitap: 8.4–8.5

**Senaryo.** Öğrenci listesi ve ayrı bir not listesi var (aynı sıra). Her öğrencinin ağırlıklı ortalaması ve harf notu hesaplanacak, sonra sınıf harf notuna göre gruplanacak.

**Görev.** zipWith ile öğrenci ve notları eşle; ortalama → harf notu dönüşümünü yaz; groupBy ile dağılımı çıkar.

```java
Map<Letter, List<String>> report(List<Student> s, List<List<Integer>> grades, List<Double> weights)
```

| Girdi | Beklenen |
|---|---|
| `vize 60, final 80 (0.4/0.6)` | 72 → CC |
| `listeler farklı uzunlukta` | Failure (sessizce kesme!) |
| `boş sınıf` | boş map |

<details><summary>İpucu</summary>

zipWith kısa listeye göre keser; bu hatayı gizler. Önce uzunluk eşitliğini Assertion ile kontrol et.

</details>

### 19 Log tarama

**Operasyon · lazy stream** · Kavramlar: `LazyStream`, `takeWhile`, `filter` · Kitap: 9

**Senaryo.** Büyük bir log dosyasında belirli bir saatten sonraki ilk 10 ERROR satırı lazım. Dosyanın tamamını belleğe almak istemiyorsun.

**Görev.** Satırları lazy bir stream olarak modelle; dropWhile(zaman < başlangıç) → filter(ERROR) → take(10) zincirini kur. Kaç satırın gerçekten okunduğunu say.

```java
List<LogLine> firstErrors(LazyStream<String> lines, LocalTime from, int n)
```

| Girdi | Beklenen |
|---|---|
| `1 000 000 satır, ilk 10 ERROR 500. satırda bitiyor` | ~500 satır okunur |
| `hiç ERROR yok` | [] (tüm akış okunur) |
| `n=0` | [] ve hiç satır okunmaz |

<details><summary>İpucu</summary>

Okunan satır sayısını görmek için kaynağı bir sayaçla sar (sadece testte). Lazy olmasaydı 1 000 000 satırın tamamı işlenirdi.

</details>

### 20 Sonsuz fatura numarası

**Muhasebe · sonsuz akış** · Kavramlar: `iterate`, `unfold` · Kitap: 9.4

**Senaryo.** Fatura numaraları FT-2026-000001 biçiminde ve yıl başında sıfırlanıyor.

**Görev.** Başlangıç tarihi ve sayaçtan sonsuz bir numara akışı üret; yıl değişimini unfold durumunda ele al.

```java
LazyStream<String> invoiceNumbers(int year, int start)
```

| Girdi | Beklenen |
|---|---|
| `(2026, 1).take(2)` | [FT-2026-000001, FT-2026-000002] |
| `(2026, 999999).take(2)` | taşma: Failure ya da yeni seri (karar ver) |
| `take(0)` | [] |

<details><summary>İpucu</summary>

unfold(state, s -> Option<Tuple<value, nextState>>) ile durum (yıl, sayaç) taşı. Akış sonsuz; takeWhile ya da take olmadan toList çağırma.

</details>

### 21 Pahalı hesaplamayı önbelleğe alma

**Performans · memoization** · Kavramlar: `memoization`, `saf fonksiyon` · Kitap: 4.4

**Senaryo.** Posta koduna göre vergi bölgesi hesaplamak 200 ms sürüyor ve sonuç hiç değişmiyor.

**Görev.** Herhangi bir Function<A,B>'yi memoize eden genel bir sarmalayıcı yaz; çağrı sayısını sayarak doğrula.

```java
<A, B> Function<A, B> memoize(Function<A, B> f)
```

| Girdi | Beklenen |
|---|---|
| `aynı posta kodu 3 kez` | f 1 kez çağrılır |
| `farklı 2 kod` | f 2 kez çağrılır |
| `paralel çağrı` | ConcurrentHashMap.computeIfAbsent |

<details><summary>İpucu</summary>

Memoization sadece saf fonksiyonlarda güvenli: sonucu zamana ya da dış duruma bağlı bir fonksiyonu önbelleğe almak hata üretir.

</details>

---

## Seviye 5 · Durum ve etkiler

*Bölüm 12–13* · Değişen durumu (state) parametre olarak taşıma, etkileri programın kenarına itme.

### 22 Test edilebilir kupon kodu

**Kampanya · State + RNG** · Kavramlar: `State`, `RNG` · Kitap: 12.1–12.2

**Senaryo.** Kampanya için 8 karakterlik rastgele kupon kodları üretiliyor. Testte aynı kodların tekrar üretilebilmesi gerekiyor.

**Görev.** RNG'yi durum olarak taşıyan Random<String> yaz; sequence ile n kod üret. Aynı seed aynı kodları vermeli.

```java
Random<List<String>> coupons(int n)  // Random<A> = State<RNG, A>
```

| Girdi | Beklenen |
|---|---|
| `seed 42, n=3` | her çalıştırmada aynı 3 kod |
| `seed 43` | farklı kodlar |
| `n=0` | [] ve RNG değişmez |

<details><summary>İpucu</summary>

Karakter seçimi = intRnd.map(i -> ALPHABET.charAt(abs(i) % len)); 8 karakter = sequence(fill(8, charRnd)). Math.random() kullanırsan test edemezsin.

</details>

### 23 Otopark bariyeri

**Gömülü sistem · durum makinesi** · Kavramlar: `state machine`, `foldLeft` · Kitap: 12.3

**Senaryo.** Bariyer: kart okutulunca açılır, araç geçince kapanır, kapalıyken araç algılanırsa alarm verir.

**Görev.** Durum ve girdi tiplerini tanımla; (State, Input) → State saf geçiş fonksiyonunu yaz. Girdi listesini foldLeft ile işle.

```java
BarrierState step(BarrierState s, Input i);  BarrierState run(List<Input> inputs)
```

| Girdi | Beklenen |
|---|---|
| `[CARD, CAR_PASSED]` | CLOSED, geçiş=1 |
| `[CAR_PASSED] (kapalıyken)` | ALARM |
| `[CARD, CARD]` | OPEN (ikinci kart etkisiz) |

<details><summary>İpucu</summary>

Kitaptaki Condition/Transition listesi ya da sealed interface + switch. Geçiş fonksiyonu saf olduğu için her senaryo bir tablo testi olur.

</details>

### 24 Sepet olay kaydı

**Event sourcing · fold** · Kavramlar: `foldLeft`, `immutable` · Kitap: 5, 12

**Senaryo.** Sepet doğrudan güncellenmiyor; ItemAdded, ItemRemoved, CouponApplied olayları saklanıyor. Güncel sepet olaylardan yeniden kuruluyor.

**Görev.** Olay tiplerini tanımla, apply(Cart, Event) → Cart yaz, olay listesini fold ile katlayarak sepeti kur. Bir olaya kadar olan geçmiş durumu da göster.

```java
Cart replay(List<Event> events);  Cart replayUntil(List<Event> events, int n)
```

| Girdi | Beklenen |
|---|---|
| `[Add(A,2), Add(B,1), Remove(A)]` | {B:1} |
| `Remove olmayan ürünü` | sepet değişmez ya da Failure (karar) |
| `replayUntil(…, 1)` | {A:2} |

<details><summary>İpucu</summary>

Geçmişe dönmek bedava: aynı fold'u ilk n olayla çalıştır. Mutable sepetle bunu yapamazdın.

</details>

### 25 Rapor üret, sonra yaz

**Raporlama · etkileri kenara itme** · Kavramlar: `Effect`, `saf çekirdek` · Kitap: 13.1–13.2

**Senaryo.** Gün sonu satış raporu hesaplanıp hem dosyaya yazılıyor hem e-postayla gönderiliyor. Şu an hesaplama ve yazma iç içe.

**Görev.** Raporu saf bir fonksiyonla üret (List<Sale> → Report → String). Etkileri (dosya, e-posta) Effect<String> olarak dışarıdan ver; Result.forEachOrFail ile uygula.

```java
Result<String> render(List<Sale> sales);  void main: render(...).forEachOrFail(fileEffect)
```

| Girdi | Beklenen |
|---|---|
| `test` | render'ı etkisiz test et; Effect'i sahte bir listeye yazdır |
| `boş satış` | "Bugün satış yok" raporu, hata değil |
| `yazma başarısız` | hata yalnızca main'de raporlanır |

<details><summary>İpucu</summary>

Kural: try/catch ve System.out sadece main'de. render içinde LocalDate.now() çağırma; tarihi parametre al.

</details>

### 26 Yeniden deneme politikası

**Entegrasyon · etkiyi tarif etme** · Kavramlar: `Supplier`, `Result`, `TailCall` · Kitap: 13.3

**Senaryo.** Ödeme servisine istek ara sıra zaman aşımına uğruyor. 3 kez, artan beklemeyle denenmeli.

**Görev.** Etkiyi Supplier<Result<T>> olarak al; retry(n, delays) bir Supplier<Result<T>> döndürsün. Bekleme fonksiyonunu da parametre yap ki test anında bitsin.

```java
<T> Supplier<Result<T>> retry(Supplier<Result<T>> call, List<Duration> delays, Effect<Duration> sleep)
```

| Girdi | Beklenen |
|---|---|
| `2. denemede başarılı` | Success, sleep 1 kez |
| `hep başarısız, 3 gecikme` | son Failure, sleep 3 kez |
| `gecikme listesi boş` | tek deneme |

<details><summary>İpucu</summary>

retry çağrıldığında hiçbir şey olmaz; sadece programı tarif eder. Çalışma .get() ile başlar. Bu, 13. bölümdeki "program döndüren fonksiyon" fikri.

</details>

---

## Seviye 6 · Gerçek hayat mini projeleri

*Hepsi bir arada* · Önceki seviyelerin birleşimi; her biri küçük bir domain modeli.

### 27 Kargo takip durumu

**Lojistik · olaylar + durum makinesi** · Kavramlar: `state machine`, `Result`, `foldLeft` · Kitap: 7, 12

**Senaryo.** Kargo olayları sırasız gelebiliyor (DELIVERED, OUT_FOR_DELIVERY'den önce). Müşteriye doğru güncel durum ve tahmini teslim gösterilmeli.

**Görev.** Olayları zamana göre sırala, geçerli geçişleri kontrol eden bir fold yaz; geçersiz geçişi Result.failure ile raporla (hangi olay, hangi durumdan).

```java
Result<Shipment> track(List<ShipmentEvent> events)
```

| Girdi | Beklenen |
|---|---|
| `sırasız ama geçerli olaylar` | DELIVERED |
| `DELIVERED → IN_TRANSIT` | Failure("geçersiz geçiş") |
| `hiç olay yok` | CREATED |

<details><summary>İpucu</summary>

foldLeft(Result.success(initial), acc -> e -> acc.flatMap(s -> transition(s, e))). Kısa devre ilk geçersiz geçişte durur.

</details>

### 28 Fatura motoru

**Muhasebe · uçtan uca** · Kavramlar: `Validation`, `fold`, `BigDecimal` · Kitap: 7, 8, 15

**Senaryo.** Fatura satırları farklı KDV oranlarında (%1, %10, %20). Satır iskontosu, genel iskonto, KDV dökümü ve kuruş yuvarlama farkı hesaplanacak.

**Görev.** Satırları doğrula (Validation), oranlara göre grupla, her grup için matrah ve KDV hesapla, genel toplamı yuvarlama farkını kaybetmeden üret.

```java
Validation<Invoice> issue(List<InvoiceLineInput> lines, BigDecimal generalDiscount)
```

| Girdi | Beklenen |
|---|---|
| `3 satır, 2 farklı oran` | oran başına matrah + KDV dökümü |
| `negatif adet satırı` | satır numarasıyla hata |
| `0.10 × 3 satır` | 0.30 (yuvarlama farkı yok) |

<details><summary>İpucu</summary>

Önce satır bazında yuvarla mı, toplamda mı? Bu bir iş kuralı; parametre yap. Projedeki OrderPricing.addDiscount'un "toplam 0'ın altına düşmez" kuralına bak.

</details>

### 29 Hız sınırlayıcı (rate limiter)

**API güvenliği · saf durum geçişi** · Kavramlar: `State`, `saf fonksiyon` · Kitap: 12

**Senaryo.** Her kullanıcı dakikada en fazla 60 istek atabilir (kayan pencere). Zamanı okuyan kod test edilemez hâle geliyor.

**Görev.** Zamanı parametre alan saf bir fonksiyon yaz: (pencere durumu, şimdi) → (izin var mı, yeni durum). Saati sadece en dış katmanda oku.

```java
Tuple<Boolean, Window> tryAcquire(Window w, Instant now, int limit, Duration span)
```

| Girdi | Beklenen |
|---|---|
| `59 istek, 60.` | (true, …) |
| `60 istek, 61.` | (false, durum değişmez) |
| `61 sn sonra` | eski istekler düşer, (true, …) |

<details><summary>İpucu</summary>

Window içinde zaman damgalarının immutable listesi; önce süresi dolanları at (dropWhile), sonra boyutu kontrol et. Bu State<Window, Boolean>'ın ta kendisi.

</details>

### 30 Toplantı çakışma kontrolü

**Takvim · sıralama + fold** · Kavramlar: `foldLeft`, `Option`, `zip` · Kitap: 5, 8

**Senaryo.** Toplantı odası rezervasyonunda yeni bir aralık eklenmeden önce çakışma kontrol ediliyor; ayrıca günün boş aralıkları listelenecek.

**Görev.** Aralıkları başlangıca göre sırala; çakışan çiftleri bul; mesai saatleri içindeki boşlukları ardışık çiftler üzerinden (zip(list, list.tail)) hesapla.

```java
List<Tuple<Slot, Slot>> conflicts(List<Slot> s);  List<Slot> freeSlots(List<Slot> s, Slot workday)
```

| Girdi | Beklenen |
|---|---|
| `09-10, 09:30-11` | 1 çakışma |
| `09-10, 10-11` | çakışma yok (uç noktalar) |
| `hiç toplantı yok` | boş = tüm mesai |

<details><summary>İpucu</summary>

Uç nokta kuralını ([başlangıç, bitiş) yarı açık aralık) baştan belirle; 11. bölümdeki > / >= hatası burada da çıkar.

</details>
