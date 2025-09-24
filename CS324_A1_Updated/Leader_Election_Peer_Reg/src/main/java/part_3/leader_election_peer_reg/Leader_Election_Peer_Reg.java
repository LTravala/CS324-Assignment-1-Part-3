/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license



 */

package part_3.leader_election_peer_reg;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import node_API.PeerRegister;

public class Leader_Election_Peer_Reg {
    /**
     * @param args Command-line arguments (unused).
     */
    public static void main(String[] args) {
        try {
            // Start RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Create and bind the peer register
            PeerRegister peerRegister = new PeerRegister();
            registry.rebind("PeerRegister", peerRegister);

            System.out.println("PeerRegister is set up. You can now register nodes via PeerRegistrationMain.");
        } catch (Exception e) {
            System.err.println("Error setting up PeerRegister: " + e.getMessage());
        }
    }
}