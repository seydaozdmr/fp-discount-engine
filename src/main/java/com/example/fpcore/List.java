package com.example.fpcore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static com.example.fpcore.TailCall.call;
import static com.example.fpcore.TailCall.done;

public abstract class List<A> {
    public abstract A head();
    public abstract List<A> tail();
    public abstract boolean isEmpty();
    public abstract List<A> setHead(A head);
    public abstract String toString();
    public abstract List<A> drop (int n);
    public abstract List<A> dropWhile(Function<A,Boolean> condition);
    public abstract List<A> reverse();
    public abstract <B> List<B> map(Function<A,B> function);
    public abstract List<A> filter(Function<A,Boolean> condition);
    public abstract int length();
    public abstract int lengthMemoized();
    public abstract Result<A> headOption();

    public List<A> cons(A a) {
        return new Cons<>(a, this);   // this = mevcut List<A> (Nil ya da Cons)
    }

    public static final List NIL = new Nil();

    private List(){}

    private static class Nil<A> extends List<A> {
        private Nil(){

        }
        public A head () {throw new IllegalStateException("head called on Nil");}
        public List<A> tail () {throw new IllegalStateException("tail called on Nil");}
        public List<A> setHead(A head) {throw new IllegalStateException("setHead called on Nil");}
        public boolean isEmpty () {return true;}
        public String toString() {return "[Nil]";}

        @Override
        public List<A> drop(int n) {
            return this;
        }

        @Override
        public List<A> dropWhile(Function<A, Boolean> condition) {
            return this;
        }

        @Override
        public List<A> reverse() {
            return this;
        }

        @Override
        public <B> List<B> map(Function<A, B> function) {
            return foldRight(list(),h->t->new Nil<>());
        }

        @Override
        public List<A> filter(Function<A, Boolean> condition) {
            return this;
        }

        @Override
        public int length() { return 0; }

        @Override
        public int lengthMemoized() { return 0; }

        @Override
        public Result<A> headOption() { return Result.empty(); }

    }

    private static class Cons<A>  extends List<A> {
        private final A head;
        private final List<A> tail;
        private final int length;
        private Cons(A head, List<A> tail) {
            this.head = head;
            this.tail = tail;
            this.length = tail.lengthMemoized() + 1;
        }
        public A head () {return head;}
        public List<A> tail () {return tail;}
        public boolean isEmpty () {return false;}
        public List<A> setHead(A head) {return new Cons(head, tail());}

        @Override
        public int length() {
            return foldRight(0, h -> t -> t + 1);
        }

        @Override
        public int lengthMemoized() { return length; }

        @Override
        public Result<A> headOption() { return Result.success(head); }

        public String toString() {return String.format("[%sNIL]", toString(new StringBuilder(),this).eval());}

        @Override
        public List<A> drop(int n) {
            return n<=0 ? this : drop_helper(this,n).eval();
        }

        @Override
        public List<A> dropWhile(Function<A, Boolean> condition) {
            return dropWhile_(this,condition).eval();
        }



        private TailCall<List<A>> dropWhile_(List<A> list, Function<A, Boolean> condition) {
            return !list.isEmpty() && condition.apply(list.head()) ? call(()-> dropWhile_(list.tail(),condition)) : done(list);
        }

        private TailCall<List<A>> drop_helper(List<A> list, int n) {
            return n<=0 || list.isEmpty() ? done(list) :
                    call(()-> drop_helper(list.tail(),n-1));
        }

        ;
        private TailCall<StringBuilder> toString(StringBuilder sb, List<A> list) {
            return list.isEmpty() ? done(sb) :
                    call(() -> toString(sb.append(list.head()).append(", "), list.tail()));
        }

        public List<A> reverse() {
            return reverse_(list(), this).eval();
        }

        @Override
        public <B> List<B> map(Function<A, B> function) {
            return foldRight(list(),h->t->new Cons<>(function.apply(h),t));
        }

        @Override
        public List<A> filter(Function<A, Boolean> condition) {
            return foldRight(list(), h -> t -> condition.apply(h) ? new Cons<>(h, t) : t);
        }

        private TailCall<List<A>> reverse_(List<A> list, List<A> cons){
            return cons.isEmpty() ? done(list) : call(()-> reverse_(new Cons<>(cons.head(),list), cons.tail()));
        }



    }

    public static <A> List<A> list() {return NIL;}

    public static <A> List<A> list(A... a){
        List<A> list = list();
        for (int i = a.length - 1; i >= 0; i--) {
            list = new  Cons<A>(a[i], list);
        }
        return list;
    }

    //ilk elemanı değiştirme
    public static <A> List<A> setHead(List<A> list, A elem){
        return list.setHead(elem);
    }

    public static <A> List<A> concat (List<A>list1, List<A>list2){
        return list1.isEmpty() ? list2 : new Cons<>(list1.head(), concat(list1.tail(), list2));
    }

    public static Integer sum(List<Integer> list){
        return list.isEmpty() ? 0 : list.head() + sum(list.tail());
    }

    //stack based
    public static <A,B> B foldRight(List<A> list, B n , Function<A,Function<B,B>> f){
        return list.isEmpty() ? n : f.apply(list.head()).apply(foldRight(list.tail(), n, f));
    }

    public static Integer sumFold(List<Integer> list) {
        return foldRight(list,0, x->y->x+y);
    }

    public static Double product(List<Double> list){
        return foldRight(list,1.0, x->y->x*y);
    }

    //head based
    public <B> B foldRight(B identity, Function<A, Function<B, B>> f) {
        return foldRight_(identity, this.reverse(), identity, f).eval();
    }
    private <B> TailCall<B> foldRight_(B acc, List<A> ts, B identity, Function<A, Function<B, B>> f) {
        return ts.isEmpty()
                ? done(acc)
                : call(() -> foldRight_(f.apply(ts.head()).apply(acc), ts.tail(), identity, f));
    }

    /** Her elemanı A -> List<B> ile listeye çevirir ve sonuçları tek listede birleştirir. */
    public <B> List<B> flatMap(Function<A, List<B>> f) {
        return foldRight(list(), h -> t -> concat(f.apply(h), t));
    }

    public <B> B foldLeft(B identity, Function<B,Function<A,B>> f){
        return foldLeft_(identity,this,f).eval();
    }

    private <B> TailCall<B> foldLeft_(B identity, List<A> list, Function<B,Function<A,B>> f){
        return list.isEmpty() ?
                done(identity) :
                call(()-> foldLeft_(f.apply(identity).apply(list.head()), list.tail(), f));
    }


    public static <A> List<A> concat2(List<A> l1, List<A> l2) {
        return foldRight(l1, l2, x -> y -> new Cons<>(x, y));
    }
    // veya foldLeft ile:
    public static <A> List<A> concat3(List<A> l1, List<A> l2) {
        return l1.reverse().foldLeft(l2, x -> x::cons);
    }

    // --- 8.2.2: List <-> Result dönüşümleri ---

    /** Liste listesini tek düz listeye indirger (8.2.2, flattenResult'ın yardımcısı). */
    public static <A> List<A> flatten(List<List<A>> list) {
        return list.foldRight(list(), x -> y -> concat(x, y));
    }

    /**
     * Exercise 8.5 — Failure/Empty olan elemanları atar, sadece Success değerlerini toplar.
     * Hiçbiri başarılı değilse boş liste döner (hata değil).
     */
    public static <A> List<A> flattenResult(List<Result<A>> list) {
        Function<Result<A>, Function<List<List<A>>, List<List<A>>>> f =
                x -> y -> y.cons(x.map(List::list).getOrElse(list()));
        return flatten(list.foldRight(list(), f));
    }

    /**
     * Exercise 8.7 — map + sequence'i tek foldRight'ta birleştirir: her elemanı f ile
     * Result<B>'ye çevirir, hepsi Success ise Success<List<B>>, biri bile değilse o hatayı taşır.
     */
    public static <A, B> Result<List<B>> traverse(List<A> list, Function<A, Result<B>> f) {
        return list.foldRight(Result.success(list()),
                x -> y -> Result.map2(f.apply(x), y, a -> b -> b.cons(a)));
    }

    /**
     * Exercise 8.6 — hepsi Success ise Success<List<A>>, en az biri Failure/Empty ise
     * o Result'u (Failure ya da Empty) döner. Empty'yi de Failure gibi ele alır.
     */
    public static <A> Result<List<A>> sequence(List<Result<A>> list) {
        return traverse(list, x -> x);
    }

    /**
     * sequence'in Empty'leri gerçek hata saymayan sürümü: önce Empty olanları eler,
     * sonra kalan Success/Failure'lar üzerinde sequence çalışır.
     */
    public static <A> Result<List<A>> sequence2(List<Result<A>> list) {
        return sequence(list.filter(a -> a.isSuccess() || a.isFailure()));
    }

    // --- 8.2.1: head/tail'e güvenli erişim ---

    /** Exercise 8.3 — son elemana güvenli erişim; foldLeft "her seferinde son gördüğünü tut". */
    public Result<A> lastOption() {
        return foldLeft(Result.empty(), x -> Result::success);
    }

    // --- 8.3.2: indeksli erişim ---

    /** Exercise 8.12 — sınır dışı indekste exception yerine Failure döner. */
    public Result<A> getAt(int index) {
        return index < 0 || index >= length()
                ? Result.failure("Index out of bound")
                : getAt_(this, index).eval();
    }

    private static <A> TailCall<Result<A>> getAt_(List<A> list, int index) {
        return index == 0
                ? done(Result.success(list.head()))
                : call(() -> getAt_(list.tail(), index - 1));
    }

    // --- 8.3.1: zip / product / unzip ---

    /** Exercise 8.8 — iki listeyi aynı indeksteki elemanları birleştirerek zip'ler; kısa olan sınır. */
    public static <A, B, C> List<C> zipWith(List<A> list1, List<B> list2,
                                             Function<A, Function<B, C>> f) {
        return zipWith_(list(), list1, list2, f).eval().reverse();
    }

    private static <A, B, C> TailCall<List<C>> zipWith_(List<C> acc,
            List<A> list1, List<B> list2, Function<A, Function<B, C>> f) {
        return list1.isEmpty() || list2.isEmpty()
                ? done(acc)
                : call(() -> zipWith_(
                        new Cons<>(f.apply(list1.head()).apply(list2.head()), acc),
                        list1.tail(), list2.tail(), f));
    }

    /** Exercise 8.9 — iki listenin kartezyen çarpımı (tüm olası kombinasyonlar). */
    public static <A, B, C> List<C> product(List<A> list1, List<B> list2,
                                             Function<A, Function<B, C>> f) {
        return list1.flatMap(a -> list2.map(b -> f.apply(a).apply(b)));
    }

    /** Exercise 8.10 — bir tuple listesini bir tuple-of-lists'e çevirir. */
    public static <A, B> Tuple<List<A>, List<B>> unzip(List<Tuple<A, B>> list) {
        return list.foldRight(new Tuple<>(list(), list()),
                t -> tl -> new Tuple<>(tl._1.cons(t._1), tl._2.cons(t._2)));
    }

    /** Exercise 8.11 — genelleştirilmiş unzip: her elemanı bir Tuple'a çeviren f verilir. */
    public <A1, A2> Tuple<List<A1>, List<A2>> unzip(Function<A, Tuple<A1, A2>> f) {
        return this.foldRight(new Tuple<>(list(), list()), a -> tl -> {
            Tuple<A1, A2> t = f.apply(a);           // f'yi TEK SEFER çağır
            return new Tuple<>(tl._1.cons(t._1), tl._2.cons(t._2));
        });
    }

    // --- 8.3.3: listeyi bölmek ---

    /** Exercise 8.14 — listeyi index konumundan iki parçaya böler; IndexOutOfBounds atmaz. */
    public Tuple<List<A>, List<A>> splitAt(int index) {
        int ii = index < 0 ? 0 : Math.min(index, length());
        return splitAt_(list(), this.reverse(), length() - ii).eval();
    }

    private static <A> TailCall<Tuple<List<A>, List<A>>> splitAt_(
            List<A> acc, List<A> reversed, int i) {
        return i == 0 || reversed.isEmpty()
                ? done(new Tuple<>(reversed.reverse(), acc))
                : call(() -> splitAt_(acc.cons(reversed.head()), reversed.tail(), i - 1));
    }

    // --- 8.3.4: alt-liste arama ---

    /** Exercise 8.16 — list, sub ile mi başlıyor? */
    public static <A> Boolean startsWith(List<A> list, List<A> sub) {
        return startsWith_(list, sub).eval();
    }

    private static <A> TailCall<Boolean> startsWith_(List<A> list, List<A> sub) {
        return sub.isEmpty()
                ? done(true)
                : list.isEmpty()
                    ? done(false)
                    : list.head().equals(sub.head())
                        ? call(() -> startsWith_(list.tail(), sub.tail()))
                        : done(false);
    }

    /** Exercise 8.16 — sub, list içinde ardışık bir alt liste olarak geçiyor mu? */
    public static <A> boolean hasSubList(List<A> list, List<A> sub) {
        return hasSubList_(list, sub).eval();
    }

    private static <A> TailCall<Boolean> hasSubList_(List<A> list, List<A> sub) {
        return list.isEmpty()
                ? done(sub.isEmpty())
                : startsWith(list, sub)
                    ? done(true)
                    : call(() -> hasSubList_(list.tail(), sub));
    }

    // --- 8.3.5: çeşitli yardımcılar ---

    /** Exercise 8.17 — elemanları f(eleman) anahtarına göre gruplar. */
    public <B> Map<B, List<A>> groupBy(Function<A, B> f) {
        return foldRight(new HashMap<>(), t -> mt -> {
            B k = f.apply(t);
            List<A> current = mt.getOrDefault(k, list());
            mt.put(k, current.cons(t));
            return mt;
        });
    }

    /** Exercise 8.18 — bir tohum ve S -> Result<Tuple<A,S>> fonksiyonuyla liste üretir. */
    public static <A, S> List<A> unfold(S z, Function<S, Result<Tuple<A, S>>> f) {
        return unfold_(list(), z, f).eval().reverse();
    }

    private static <A, S> TailCall<List<A>> unfold_(List<A> acc, S z,
            Function<S, Result<Tuple<A, S>>> f) {
        Result<Tuple<A, S>> r = f.apply(z);
        Result<TailCall<List<A>>> next =
                r.map(t -> call(() -> unfold_(acc.cons(t._1), t._2, f)));
        return next.getOrElse(done(acc));
    }

    /** Exercise 8.19 — [start, end) aralığındaki tamsayıların listesi. */
    public static List<Integer> range(int start, int end) {
        return unfold(start, i -> i < end
                ? Result.success(new Tuple<>(i, i + 1))
                : Result.empty());
    }

    /**
     * Exercise 8.20 — koşulu sağlayan en az bir eleman var mı? Saf, stack-safe ve
     * ilk eşleşmede kısa devre yapan imperatif bir implementasyon (bkz. "when not
     * to use folds", 8.3.2/8.3.3): dışarıdan gözlemlenebilir yan etkisi yok.
     */
    public boolean exists(Function<A, Boolean> p) {
        List<A> current = this;
        while (!current.isEmpty()) {
            if (p.apply(current.head())) return true;
            current = current.tail();
        }
        return false;
    }

    /**
     * Exercise 13.1 — her elemana efekti sırayla uygular. Stack-safe: özyineleme yerine
     * döngü kullanır (kitabın "daha verimli seçenek" olarak önerdiği yol).
     */
    public void forEach(Effect<A> ef) {
        List<A> current = this;
        while (!current.isEmpty()) {
            ef.apply(current.head());
            current = current.tail();
        }
    }

    /** Exercise 8.21 — tüm elemanlar koşulu sağlıyor mu? exists'in De Morgan tersi. */
    public boolean forAll(Function<A, Boolean> p) {
        return !exists(x -> !p.apply(x));
    }

    // --- 8.4: otomatik paralel liste işleme ---

    /** Exercise 8.22 (yardımcı) — listeyi index konumundan List<List<A>> olarak böler. */
    public List<List<A>> splitListAt(int i) {
        return splitListAt_(list(), this.reverse(), i).eval();
    }

    private static <A> TailCall<List<List<A>>> splitListAt_(
            List<A> acc, List<A> reversed, int i) {
        return i == 0 || reversed.isEmpty()
                ? done(List.list(reversed.reverse(), acc))
                : call(() -> splitListAt_(acc.cons(reversed.head()), reversed.tail(), i - 1));
    }

    /**
     * Exercise 8.22 — listeyi ikiye böl, her parçayı özyinelemeli olarak tekrar ikiye böl.
     * Adım sayısı log(length) olduğu için stack-safe olması gerekmez.
     */
    public List<List<A>> divide(int depth) {
        return this.isEmpty() ? list(this) : divide(list(this), depth);
    }

    private List<List<A>> divide(List<List<A>> lists, int depth) {
        return lists.head().length() < depth || depth < 2
                ? lists
                : divide(lists.flatMap(x -> x.splitListAt(x.length() / 2)), depth / 2);
    }

    /**
     * Exercise 8.23 — listeyi çok sayıda küçük parçaya bölüp ExecutorService'e dağıtarak
     * foldLeft'i paralel çalıştırır. f: parça içindeki fold; m: parça sonuçlarını birleştirir.
     */
    public <B> Result<B> parFoldLeft(ExecutorService es, B identity,
            Function<B, Function<A, B>> f, Function<B, Function<B, B>> m) {
        final int chunks = 1024;
        final List<List<A>> dList = divide(chunks);
        try {
            List<B> partials = dList
                    .map(x -> es.submit(() -> x.foldLeft(identity, f)))
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return Result.success(partials.foldLeft(identity, m));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /** Exercise 8.24 — her elemana g'yi ExecutorService üzerinde paralel uygular. */
    public <B> Result<List<B>> parMap(ExecutorService es, Function<A, B> g) {
        try {
            return Result.success(this.map(x -> es.submit(() -> g.apply(x)))
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

}
