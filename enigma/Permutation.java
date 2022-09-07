package enigma;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Tianyi Xu
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _forwardPerm = new int[alphabet.size()];
        _backwardPerm = new int[alphabet.size()];

        for (int i = 0; i < alphabet().size(); i++) {
            _forwardPerm[i] = i;
            _backwardPerm[i] = i;
        }

         for (String cycle: cycles.split("[\\(\\)]")) {
             addCycle(cycle.trim());
         }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (cycle.length() <= 1) {
            return;
        }
        for (int i = 0; i < cycle.length() - 1; i++) {
            _forwardPerm[_alphabet.toInt(cycle.charAt(i))] = _alphabet.toInt(cycle.charAt(i + 1));
        }
        _forwardPerm[_alphabet.toInt(cycle.charAt(cycle.length() - 1))] = _alphabet.toInt(cycle.charAt(0));

        for (int i = 0; i < _backwardPerm.length; i++) {
            _backwardPerm[_forwardPerm[i]] = i;
        }
    }


    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _forwardPerm[wrap(p)];
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _backwardPerm[wrap(c)];
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _alphabet.toChar(_forwardPerm[_alphabet.toInt(p)]);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _alphabet.toChar(_backwardPerm[_alphabet.toInt(c)]);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _forwardPerm.length; i++) {
            if (_forwardPerm[i] == i) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    private int[] _forwardPerm;

    private  int[] _backwardPerm;
    

}
