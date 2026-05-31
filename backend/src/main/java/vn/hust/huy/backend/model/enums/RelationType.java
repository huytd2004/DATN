package vn.hust.huy.backend.model.enums;

/**
 * Defines the type of relationship between two dictionary entries.
 *
 * <ul>
 *   <li>{@code kanji}    – a kanji character that makes up the word</li>
 *   <li>{@code radical}  – a radical component of a kanji</li>
 *   <li>{@code compound} – a compound word (jukugo) related to this entry</li>
 *   <li>{@code synonym}  – a word with a similar meaning</li>
 * </ul>
 */
public enum RelationType {
    /** Kanji character that makes up the word (e.g. 木, 漏, 日 in 木漏れ日). */
    kanji,

    /** Radical component of a kanji. */
    radical,

    /** Compound / derived word sharing the same root. */
    compound,

    /** Word with a similar meaning. */
    synonym
}
