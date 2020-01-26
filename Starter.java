import java.util.ArrayList;
import java.util.Scanner;

public class Starter {
    static long[] userId = {0, 0, 0, 1};
    static ArrayList<Block> blockList = new ArrayList<>();
    static int counter = 0;

    public static void main(String[] args) {
        //load configs
        //open sockets to peers and such
        //read in blocks from files
        //calculate optimal target list
        System.out.println("W E L C O M E   T O   D A G N E T   V 0 . 0 . 1");
        System.out.print("You are currently logged in as user: ");
        for (int i = 0; i < 4; i++) {
            System.out.printf("%016x", userId[i]);
        }
        System.out.println("\n");
        String menu = "What can I do for you?\n" +
                "1: make a new block\n" +
                "2: send block(s) to memory\n" +
                "3: read block(s) from memory\n" +
                "4: parse info from loaded block\n" +
                "\n" +
                "0: exit";
        Scanner s = new Scanner(System.in);
        s.useDelimiter("\n");
        while (true) {
            System.out.println(menu);
            int choice = -1;
            do {
                if (choice >= 0) {
                    System.out.println("Sorry, I couldn't understand that\n");
                    System.out.println(menu);
                }
                String response = s.next();
                try {
                    choice = Integer.parseInt(response);
                } catch (NumberFormatException e) {
                    System.out.println("Sorry, I couldn't understand that\n");
                    System.out.println(menu);
                    choice = -1;
                }
            } while (choice < 0 || choice > 7 || choice == 6);
            if (choice == 0) {
                System.out.println("Thank you for participating in the pre alpha!");
                s.close();
                return;
            }
            if (choice == 1) {
                makeBlock(s);
            }
            if (choice == 2) {
                if (blockList.size() < 1) {
                    System.out.println("I have no blocks to save. Please make a block or load one first!\n");
                    continue;
                }
                System.out.println("sending the stored blocks to memory...");
                if (!sendToMem()) {
                    System.out.println("something went wrong along the way, but most blocks should have been saved");
                }
            }
            if (choice == 3) {
                readFromMem(s);
            }
            if (choice == 4) {
                parseInfo(s);
            }
        }
    }

    private static void parseInfo(Scanner s) {
        System.out.println("Parsing block info:");
        System.out.println("I have " + blockList.size() + " block(s) in memory\n");
        if (blockList.size() < 1) {
            System.out.println("No blocks to parse from. Please make a block or load one first!\n");
            return;
        }
        int choice = 0;
        while (true) {
            System.out.println("Pick a block:\n0: exit");
            for (int i = 0; i < blockList.size(); i++) {
                System.out.println((i + 1) + ": block id#" + blockList.get(i).hash[3]);
            }
            try {
                choice = Integer.parseInt(s.next());
            } catch (NumberFormatException e) {
                System.out.println("numbers only please");
                continue;
            }
            if (choice < 0 || choice > blockList.size()) {
                System.out.println("that's not a valid choice from the list");
                continue;
            }
            break;
        }
        System.out.println();
        if (choice == 0) {
            return;
        }
        Block b = blockList.get(choice - 1);
        System.out.println("You have chosen the block with ID#" + b.hash[3]);
        System.out.println("This block was hashed by the user with ID#" + b.miner[3]);
        if (b.commenting) {
            System.out.println("This block contains at least 1 comment");
        } else {
            System.out.println("This block contains no comments");
        }
        if (b.voting) {
            System.out.println("This block contains at least 1 vote");
        } else {
            System.out.println("This block contains no votes");
        }
        System.out.println("\nPrinting the data in the order they appear in the block...");
        for (byte[] data : b.riderData) {
            System.out.println();
            switch (data[0]) {
                case 0:
                    System.out.println("The block was given a hash/ID# of " + data[data.length - 1] + "!");
                    break;
                case 1:
                    int weight = 0;
                    for (int i = 1; i <= 4; i++) {
                        weight += ((int) (data[i])) << (32 - (8 * i));
                    }
                    System.out.println("User " + data[data.length - 1] + " voted on a comment with " + weight + " weight!");
                    break;
                case 2:
                    System.out.println("User " + data[data.length - 1] + " posted the following comment:");
                    int length = 0;
                    for (int i = 1; i <= 4; i++) {
                        length += ((int) (data[i])) << (32 - (8 * i));
                    }
                    for (int i = 0; i < length; i++) {
                        System.out.print((char) data[5 + i]);
                    }
                    System.out.println("\n");
                    if (Math.random() < 0.01) {
                        System.out.print("I dunno man, seems kinda like a shitpost to me...");
                    } else {
                        System.out.println("Wow, very insightful!");
                    }
                    break;
                default:
                    System.out.println("The block seems to contain some type of info that you do not know how to parse...\n" +
                            "It's either corrupted data, encrypted data, or data of a form you need to ask other users about!");
            }
        }
        System.out.println("\n\n");
    }

    private static void readFromMem(Scanner s) {
        System.out.println("\nREAD BLOCKS\n");
        System.out.println("what time is your block from?");
        System.out.println("(all blocks in this demo are from time 0)");
        long time = -1;
        do {
            try {
                time = Long.parseLong(s.next());
            } catch (NumberFormatException e) {
                System.out.println("numbers only please");
                continue;
            }
            break;
        } while (true);
        System.out.println("\nwhat is the id of your block?");
        long id = -1;
        do {
            try {
                id = Long.parseLong(s.next());
            } catch (NumberFormatException e) {
                System.out.println("numbers only please");
                continue;
            }
            break;
        } while (true);
        System.out.println("attempting to retrieve the block with info given...");
        long[] h = {0, 0, 0, id};
        Block b = BlockManager.read(time, h);
        if (b == null) {
            System.out.println("it seems like that block is corrupt or doesn't exit...");
            return;
        } else {
            System.out.println("successfully read a block into the list!");
            blockList.add(b);
        }
        b.parseRaw();
        System.out.println("\n");
    }

    private static boolean sendToMem() {
        boolean success = true;
        for (Block b : blockList) {
            b.generateRaw();
            if (!BlockManager.save(b))
                success = false;
        }
        blockList.clear();
        return success;
    }

    private static void makeBlock(Scanner s) {
        System.out.println("\n\nBLOCKMAKER:\n");
        System.out.println("How many actions would you like to take in this block? (-1 to cancel)");
        int choice = -2;
        do {
            String response = s.next();
            try {
                choice = Integer.parseInt(response);
            } catch (NumberFormatException e) {
            }
            if (choice < 0) {
                System.out.println("Sorry, I couldn't understand that\n");
                System.out.println("How many actions would you like to take in this block?");
            }
            if (choice > 5) {
                System.out.println("Normally this would be allowed, every block would have 100 or so pieces of rider data on it");
                System.out.println("BUT... thats probably gonna be too much for a demo program. Please pick something less than 5");
                choice = -2;
            }
        } while (choice < -1);
        System.out.println();
        if (choice == -1) {
            return;
        }
        Block b = new Block(null);
        b.riderData = new byte[1][];
        for (int i = 0; i < choice; i++) {
            int choice2 = 0;
            do {
                System.out.println("Action #" + (i + 1) + ":\n" +
                        "1: Vote\n" +
                        "2: Comment\n" +
                        "3: More options coming in the future (tagging, dms, combo actions, account self destruction, etc\n\n" +
                        "-1: cancel");
                String response = s.next();
                try {
                    choice2 = Integer.parseInt(response);
                } catch (NumberFormatException e) {
                    choice2 = 0;
                }
                if (choice2 <= 0 && choice2 != -1) {
                    System.out.println("Sorry, I couldn't understand that\n");
                    choice2 = 0;
                }
                if (choice2 > 2) {
                    System.out.println("further options are not supported yet, sorry!");
                    choice2 = 0;
                }
            } while (choice2 == 0);
            if (choice2 == -1) {
                return;
            }
            if (choice2 == 1) {
                System.out.println("creating a vote:");
                int weight = 0;
                do {
                    System.out.println("how much weight would you like to assign to this vote?");
                    String response = s.next();
                    try {
                        weight = Integer.parseInt(response);
                    } catch (NumberFormatException e) {
                        System.out.println("Sorry, I couldn't understand that\n");
                        weight = 0;
                        continue;
                    }
                    if (weight <= 0) {
                        System.out.println("positive numbers only please\n");
                        weight = 0;
                    }
                } while (weight == 0);
                long time = 0;
                long targetB = 0;
                long targetC = 0;
                do {
                    System.out.println("what time is the target comment's host block at?");
                    String response = s.next();
                    try {
                        time = Long.parseLong(response);
                    } catch (NumberFormatException e) {
                        System.out.println("Sorry, I couldn't understand that\n");
                        continue;
                    }
                    break;
                } while (true);
                do {
                    System.out.println("what is the target comment's host block's id?");
                    String response = s.next();
                    try {
                        targetB = Long.parseLong(response);
                    } catch (NumberFormatException e) {
                        System.out.println("Sorry, I couldn't understand that\n");
                        continue;
                    }
                    break;
                } while (true);
                do {
                    System.out.println("who signed the target comment?");
                    String response = s.next();
                    try {
                        targetC = Long.parseLong(response);
                    } catch (NumberFormatException e) {
                        System.out.println("Sorry, I couldn't understand that\n");
                        continue;
                    }
                    break;
                } while (true);
                System.out.println("generating vote...");
                byte[] vData = new byte[1 + 4 + 8 + 4 * 8 + 4 * 8 + 4 * 8];
                vData[0] = 1;
                for (int p = 1; p <= 4; p++) {
                    vData[p] = (byte) (weight >>> 32 - (8 * (p)));
                }
                for (int p = 1; p <= 8; p++) {
                    vData[p + 4] = (byte) (time >>> (64 - 8 * (p)));
                }
                for (int p = 1; p <= 8; p++) {
                    vData[p + 4 + 4 * 8] = (byte) (targetB >>> (64 - 8 * (p)));
                }
                for (int p = 1; p <= 8; p++) {
                    vData[p + 4 + 2 * 4 * 8] = (byte) (targetC >>> (64 - 8 * (p)));
                }
                System.out.println("signing vote...");
                for (int p = 1; p <= 8; p++) {
                    vData[p + 4 + 3 * 4 * 8] = (byte) (userId[3] >>> (64 - 8 * (p)));
                }
                System.out.println("vote added!\n");
                b.voting = true;
                b.addGrow(vData);
            }
            if (choice2 == 2) {
                StringBuilder sb = new StringBuilder();
                String message;
                while (true) {
                    System.out.println("what would you like the comment to say?\n" +
                            "(to end the comment enter an empty line)");
                    while (true) {
                        String str = s.next();
                        if (str.equals(""))
                            break;
                        sb.append(str);
                        sb.append('\n');
                    }
                    if (sb.length() > 0) {
                        sb.delete(sb.length() - 1, sb.length());
                    }
                    message = sb.toString();
                    System.out.println("\nyou have written:\n\"" + message + "\"\n\n");
                    System.out.println("type y to confirm this message");
                    String response = s.next();
                    if (response.toUpperCase().equals("Y")) {
                        System.out.println("comment confirmed\n");
                        break;
                    }
                    System.out.println("comment cancelled\n");
                    sb = new StringBuilder();
                }
                System.out.println("generating comment...");
                int len = message.length();
                byte[] cData = new byte[1 + 4 + len + 4 * 8];
                cData[0] = 2;
                for (int p = 1; p <= 4; p++) {
                    cData[p] = (byte) (len >>> (32 - 8 * p));
                }
                int p = 5;
                for (char c : message.toCharArray()) {
                    cData[p++] = (byte) c;
                }
                System.out.println("signing comment...");
                for (p = 1; p <= 8; p++) {
                    cData[p + 4 + len + 3 * 8] = (byte) (userId[3] >>> (64 - 8 * (p)));
                }
                System.out.println("comment added!");
                b.addGrow(cData);
                b.commenting = true;
            }
        }
        blockList.add(b);
        b.hash[3] = 15 + (++counter) * 16;
        b.targets = new Target[0];
        b.secLevel = b.getSecurityLevel(b.hash);
        b.miner = userId;
        byte[] hashRider = new byte[1 + 4 * 8];
        hashRider[0] = 0;
        hashRider[4 * 8] = 15;
        b.addGrow(hashRider);
        //b.time = System.currentTimeMillis() / 60000;
        b.time = 0;

        System.out.println("\n\nyour block has been made and added to the list");
        System.out.println("it is id#" + b.hash[3] + " and time " + b.time + " if you want to retrieve it");
        System.out.println();
    }
}
