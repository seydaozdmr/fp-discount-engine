package com.example.exercises;

import org.junit.jupiter.api.Test;

import static com.example.exercises.UrlSlugMaker.lower;
import static com.example.exercises.UrlSlugMaker.removePunctuation;
import static com.example.exercises.UrlSlugMaker.slugify;
import static com.example.exercises.UrlSlugMaker.spacesToHyphen;
import static com.example.exercises.UrlSlugMaker.trim;
import static com.example.exercises.UrlSlugMaker.trimHyphens;
import static com.example.exercises.UrlSlugMaker.turkishToAscii;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlSlugMakerTest {

    @Test
    void trim_removes_leading_and_trailing_spaces() {
        assertEquals("Çok Güzel", trim.apply("  Çok Güzel  "));
    }

    @Test
    void lower_uses_turkish_rules_for_dotted_and_dotless_i() {
        assertEquals("ısparta", lower.apply("ISPARTA"));
        assertEquals("istanbul", lower.apply("İSTANBUL"));
    }

    @Test
    void turkishToAscii_replaces_all_six_turkish_letters() {
        assertEquals("igosuc", turkishToAscii.apply("ığöşüç"));
    }

    @Test
    void turkishToAscii_keeps_other_characters_unchanged() {
        assertEquals("cok guzel seyler 123", turkishToAscii.apply("çok güzel şeyler 123"));
    }

    @Test
    void removePunctuation_drops_everything_except_letters_digits_spaces_and_hyphens() {
        assertEquals("javada fp", removePunctuation.apply("java'da fp!"));
        assertEquals("a-b c1", removePunctuation.apply("a-b, c1."));
    }

    @Test
    void spacesToHyphen_collapses_runs_of_spaces_and_hyphens() {
        assertEquals("a-b", spacesToHyphen.apply("a  --  b"));
        assertEquals("a-b-c", spacesToHyphen.apply("a b-c"));
    }

    @Test
    void trimHyphens_removes_hyphens_at_both_ends_only() {
        assertEquals("a-b", trimHyphens.apply("--a-b--"));
    }

    @Test
    void slugify_handles_turkish_characters_and_spaces() {
        assertEquals("cok-guzel-seyler", slugify.apply("  Çok Güzel Şeyler  "));
    }

    @Test
    void slugify_removes_apostrophe_without_inserting_hyphen() {
        assertEquals("javada-fp", slugify.apply("Java'da FP!"));
    }

    @Test
    void slugify_collapses_repeated_separators() {
        assertEquals("a-b", slugify.apply("a  --  b"));
    }

    @Test
    void slugify_example_from_scenario() {
        assertEquals("javada-fonksiyonel-programlama", slugify.apply("Java'da Fonksiyonel Programlama!"));
    }

    @Test
    void slugify_lowercases_dotless_capital_i_correctly() {
        assertEquals("isik-isparta", slugify.apply("IŞIK ISPARTA"));
    }

    @Test
    void slugify_trims_hyphens_left_by_punctuation_at_the_edges() {
        assertEquals("a-b", slugify.apply("--- a b ---"));
    }

    @Test
    void slugify_keeps_digits() {
        assertEquals("2024-yili-ozeti", slugify.apply("2024 Yılı Özeti"));
    }

    @Test
    void slugify_of_empty_or_only_punctuation_is_empty() {
        assertEquals("", slugify.apply(""));
        assertEquals("", slugify.apply("!!! ???"));
    }

    @Test
    void slugify_is_pure_and_returns_same_result_each_time() {
        assertEquals(slugify.apply("Çok Güzel"), slugify.apply("Çok Güzel"));
    }
}
