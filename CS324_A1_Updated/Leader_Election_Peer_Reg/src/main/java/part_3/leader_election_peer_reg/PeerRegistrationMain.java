package part_3.leader_election_peer_reg;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import node_API.nodeApi;
import node_API.PeerRegisterInterface;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

public class PeerRegistrationMain {

    public static void main(String[] args) {
        try {
            // Connect to the RMI registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PeerRegisterInterface peerRegister = (PeerRegisterInterface) registry.lookup("PeerRegister");

            // Ask user for a peer ID
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter peer ID to register this node: ");
            int peerId = Integer.parseInt(scanner.nextLine());

            // Register and bind the node
            Node newNode = new Node(peerId, peerRegister);
            peerRegister.registerPeer(newNode);
            registry.rebind("Node" + peerId, newNode);

            System.out.println("Node " + peerId + " registered and bound to RMI.");

            new Thread(() -> {
                boolean printed = false;
                while (!printed) {
                    try {
                        int winner = peerRegister.getElectedLeader(); // new method in interface
                        if (winner > 0) {
                            System.out.println("Node " + winner + " is the NEW LEADER");
                            printed = true;
                        } else {
                            Thread.sleep(500); 
                        }
                    } catch (Exception e) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }
                }
            }, "WinnerPoller-Node" + peerId).start();

            while (true) {
                System.out.print("[Node " + peerId + "] Type READY / START / EXIT: ");
                String input = scanner.nextLine().trim().toUpperCase();

                if (input.equals("EXIT")) {
                    System.out.println("Exiting peer " + peerId);
                    break;
                } else if (input.equals("READY")) {
                    peerRegister.markReady(peerId);
                    System.out.println("All Ready? " + peerRegister.allReady());
                } else if (input.equals("START")) {
                    peerRegister.requestStart(peerId);
                } else {
                    System.out.println("Invalid command. Use READY, START, or EXIT.");
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Error in PeerRegistrationMain: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
