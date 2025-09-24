

package part_3.leader_election_peer_reg;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import node_API.nodeApi;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

public class ElectionMain {
   
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            nodeApi node5 = (nodeApi) registry.lookup("Node5");
            nodeApi node2 = (nodeApi) registry.lookup("Node2"); 

            new Thread(() -> {
                try {
                    System.out.println("Starting election for Node 5...");
                    node5.startElection();
                } catch (Exception e) {
                    System.err.println("Error starting election on Node5: " + e.getMessage());
                }
            }).start();

            new Thread(() -> {
                try {
                    System.out.println("Starting election for Node 2...");
                    node2.startElection();
                } catch (Exception e) {
                    System.err.println("Error starting election on Node2: " + e.getMessage());
                }
            }).start();

            System.out.println("Election process initiated concurrently.");
        } catch (Exception e) {
            System.err.println("Error in ElectionMain: " + e.getMessage());
        }
    }
}