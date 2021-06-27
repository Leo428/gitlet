package gitlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 * @author Zheyuan Hu
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains <COMMAND> <OPERAND>.
     */
    private static CommandLineTools cli;

    /**
     * entry for the program.
     * @param args args to initiate functions.
     * @throws IOException
     */
    public static void main(String... args) throws IOException {
        ArrayList<String> operands = new ArrayList<>(Arrays.asList(args));
        cli = new CommandLineTools();
        cli.refreshUntracked();
        cli.execute(operands);
        Utils.writeObject(CommandLineTools.CLI_FILE, cli);
    }

}
