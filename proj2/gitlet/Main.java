package gitlet;


import static gitlet.ErrorUtils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }

        var firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkOperands(args.length, 1);
                Repository.init();
                break;
            case "add":
                checkOperands(args.length, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkOperands(args.length, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkOperands(args.length, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                checkOperands(args.length, 1);
                Repository.log();
                break;
            default:
                invalidCommand();
                break;
        }
    }
}
