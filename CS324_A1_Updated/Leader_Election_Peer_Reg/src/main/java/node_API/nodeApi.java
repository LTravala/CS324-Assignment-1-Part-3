/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package node_API;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface nodeApi extends Remote {
    void receiveMessage(int message, String messageType) throws RemoteException;
    void startElection() throws RemoteException;
    int getNodeId() throws RemoteException;
    void setNextNode(nodeApi nextNode) throws RemoteException;
}
