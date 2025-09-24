package node_API;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

public interface PeerRegisterInterface extends Remote {
    void registerPeer(nodeApi peer) throws RemoteException;
    void notifyElectionInProgress(boolean inProgress) throws RemoteException;
    void markReady(int nodeId) throws RemoteException;
    boolean allReady() throws RemoteException;
    void requestStart(int nodeId) throws RemoteException;
    void setElectedLeader(int leaderId) throws RemoteException;
    int getElectedLeader() throws RemoteException;  
}
