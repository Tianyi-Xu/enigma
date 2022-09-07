package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Tianyi Xu
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        if (!_input.hasNext("\\*.*")) {
            throw new EnigmaException("bad input, please put * before initialize the machine");
        }

        while (_input.hasNext("\\*.*")) {
            /* Skip the '*' */
            _input.next();
            /* set up the machine */
            setUp(machine, _input.nextLine());
            while (_input.hasNext("[^*]+")) { // msg to convert
                String convMsg = machine.convert(_input.nextLine().replaceAll("\\s", ""));
                printMessageLine(convMsg);
            }
            if (_input.hasNext("\\*.*")) {
                _output.println();
            }


        }

    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine());
            String[] numRotorsPawls = _config.nextLine().trim().split("\\s+");
            int numRotors = Integer.parseInt(numRotorsPawls[0]);
            int numPawls = Integer.parseInt(numRotorsPawls[1]);
            HashSet<Rotor> allRotors = new HashSet<>();
            if (numPawls >= numRotors) {
               throw new EnigmaException("num of pawls should be less than num of rotors");
            };

            while(_config.hasNext()) {
                Rotor rotor =  readRotor();
                allRotors.add(rotor);
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        } catch (NumberFormatException excn) {
            throw error ("bad format for number of rotors in configuration file ");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorName = _config.next();
            String typeAndNotch = _config.next();
            String rotorType = typeAndNotch.substring(0,1);
            String notches = typeAndNotch.substring(1); // substring startIndex <= length, if equals length return a empty string
            String rotorCircles = "";
            while (_config.hasNext("\\(.*\\)")) {
                rotorCircles += _config.next();
            }

            switch (rotorType) {
                case "N" :
                    return new FixedRotor(rotorName,
                            new Permutation(rotorCircles, _alphabet));
                case "R" :
                    return new Reflector(rotorName,
                            new Permutation(rotorCircles, _alphabet));
                case "M":
                    return new MovingRotor(rotorName,
                            new Permutation(rotorCircles, _alphabet), notches);
                default:
                    throw new EnigmaException("configuration might has wrong format in rotor type");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        String[] settingsArr = settings.trim().split("\\s+");
        String[] rotors = new String[M.numRotors()];
        String initialSetting = "";
        String cycles = "";


        int i;
        int rotorsSize = 0;
        for (i = 0; i < settingsArr.length && rotorsSize < M.numRotors(); i++) {
            if (settingsArr[i].equals("")) {
                continue;
            }
            rotors[rotorsSize] = settingsArr[i];
            rotorsSize++;
        }
        for (; i < settingsArr.length; i++) {
            if (settingsArr[i].startsWith("(")) {
                cycles += settingsArr[i];
            } else {
                initialSetting += settingsArr[i];
            }

        }
        M.insertRotors(rotors);
        M.setRotors(initialSetting);
        M.setPlugboard(new Permutation(cycles, _alphabet));
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        StringBuffer printed = new StringBuffer();
        for (int i = 0; i < msg.length(); i++) {
            if (i != 0 && i % 5 == 0) {
                printed.append(" ");
            }
            printed.append(msg.charAt(i));;
        }
        _output.println(printed);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
