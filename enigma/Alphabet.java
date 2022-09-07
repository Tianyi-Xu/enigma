package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Tianyi Xu
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        this.ALPHABET = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return ALPHABET.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int i = 0; i < ALPHABET.length(); i++) {
            if (ALPHABET.charAt(i) == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return ALPHABET.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (!contains(ch)) {
            return -1;
        }
        return ALPHABET.indexOf(ch);
    }

    private final String ALPHABET;
}
