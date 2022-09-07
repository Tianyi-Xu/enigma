package enigma;

import net.sf.saxon.trans.SymbolicName;

import java.awt.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Tianyi Xu
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        for (Rotor rotor : allRotors) {
            _allRotorsMap.put(rotor.name(),rotor);
        }

        _myrotors = new Rotor[_numRotors];
        _plugboard = new Permutation("", alpha);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != _numRotors) {
            throw new EnigmaException("Wrong number of rotors when inserting");
        }

        /* Clear my rotors */
        _myrotors = new Rotor[_numRotors];

        /* record the names to ensure the rotors are unique in our setting */
        HashSet<String> names = new HashSet<>();

        /* Insert reflector */
        String rotorName = rotors[0];
        Rotor rotor = _allRotorsMap.getOrDefault(rotors[0], null);
        if (rotor == null) {
            throw new EnigmaException("The reflector name might be misspelled ");
        }

        if (!rotor.reflecting()) {
                throw new EnigmaException("The first rotor should be a reflector");
            }
        names.add(rotorName);
        _myrotors[0] = rotor;

        /* Insert fixed rotors */
        for(int i = 1; i < _numRotors - _pawls; i++) {
           rotorName = rotors[i];
           rotor = _allRotorsMap.getOrDefault(rotors[i], null);
            if (rotor == null) {
                throw new EnigmaException("The fixed rotor name might be misspelled ");
            }

            if (rotor.rotates()) {
                throw new EnigmaException("The rotor at positon " + i + " should be fixed rotor");
            }

            if (names.contains(rotorName)) {
                throw new EnigmaException("A rotor is repeated in the setting");
            }
            names.add(rotorName);
            _myrotors[i] = rotor;
        }

        /* Insert rotating rotors */
        for (int i = _numRotors - _pawls; i < _numRotors; i++) {
            rotorName = rotors[i];
            rotor = _allRotorsMap.getOrDefault(rotors[i], null);
            if (rotor == null) {
                throw new EnigmaException("The rotating rotor name might be misspelled ");
            }

            if (!rotor.rotates()) {
                throw new EnigmaException("The rotor at positon " + i + " should be rotating rotor");
            }

            if (names.contains(rotorName)) {
                throw new EnigmaException("A rotating rotor is repeated in the setting");
            }
            names.add(rotorName);
            _myrotors[i] = rotor;
        }


    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numRotors - 1) {
            throw error("number of settings of the rotors should fit number of rotors in the machine.");
        }

        for (int i = 1; i < _numRotors; i++) {
            if (!_alphabet.contains(setting.charAt(i - 1))) {
                throw error("the setting contains character not in the alphabet");
            }
            _myrotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard; // FIXME
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        /* Record all the rotors gonna be advances to ensure all the rotors are advance at most once */
        HashSet<Integer> advances = new HashSet<>();

        /* Advance the last rotor first (which has pawl in default) */
        advances.add(_numRotors - 1);

        /* For all the Rotors has pawl, record advances */
        for (int i = _numRotors - 2; i >= _numRotors - _pawls; i--){
            if (_myrotors[i + 1].atNotch()) {
                advances.add(i);
                advances.add(i + 1);
            }
        }

        /* Advance rotors */
        for (int index : advances) {
            _myrotors[index].advance();
        }

        /* Keybroad permutation */
        c = _plugboard.permute(c);

        /* Convert forward */
        for (int i = _numRotors - 1; i >=0; i--) {
            c = _myrotors[i].convertForward(c);
        }

        /* Convert backward */
        for (int i = 1; i < _numRotors; i++) {
            c = _myrotors[i].convertBackward(c);
        }

        /* Keyboard permutation */
        c = _plugboard.invert(c);
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        StringBuffer conv_msg = new StringBuffer();
        for (char i : msg.toCharArray()) {
            if (!_alphabet.contains(i)) {
                throw new EnigmaException("char " + i + " not in the alphabet");
            }
            conv_msg.append(_alphabet.toChar(convert(_alphabet.toInt(i))));
        }
        return conv_msg.toString();
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    private final int _numRotors;
    private final int _pawls;
    private final HashMap<String, Rotor> _allRotorsMap = new HashMap<>();
    private Rotor[] _myrotors;
    private Permutation _plugboard;
}
