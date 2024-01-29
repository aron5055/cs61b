package gitlet;

import static gitlet.Utils.join;

public class ErrorUtils {
    public static void exitWithError(String message) {
        System.out.println(message);
        System.exit(0);
    }

    public static void invalidCommand() {
        exitWithError("No command with that name exists.");
    }

    public static void checkOperands(int numArgs, int numOperands) {
        if (numArgs != numOperands) {
            exitWithError("Incorrect operands.");
        }
    }

    public static void checkInitialized() {
        if (!Repository.GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }

    public static void checkRepoExists() {
        if (Repository.GITLET_DIR.exists()) {
            exitWithError("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }

    public static void checkFileExists(String fileName) {
        if (!join(Repository.CWD, fileName).exists()) {
            exitWithError("File does not exist.");
        }
    }

    public static void checkBranchExists(String branchName) {
        if (!join(Repository.HEADS_DIR, branchName).exists()) {
            exitWithError("A branch with that name does not exist.");
        }
    }
}

