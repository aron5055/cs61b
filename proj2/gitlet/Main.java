package gitlet;


import static gitlet.ErrorUtils.*;
import static gitlet.MyUtils.*;

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
            case "global-log":
                checkOperands(args.length, 1);
                Repository.globalLog();
                break;
            case "find":
                checkOperands(args.length, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkOperands(args.length, 1);
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkout(getBranchId(readHead()), args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkout(args[1], args[3]);
                } else {
                    exitWithError("Incorrect operands.");
                }
                break;
            case "branch":
                checkOperands(args.length, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkOperands(args.length, 2);
                Repository.rmBranch(args[1]);
                break;
            default:
                invalidCommand();
                break;
        }
    }
}
