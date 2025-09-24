package part_3.leader_election_peer_reg;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import node_API.nodeApi;
import node_API.PeerRegisterInterface;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/

public class Node extends UnicastRemoteObject implements nodeApi {

    private final int nodeId;
    private volatile nodeApi nextNode;        // successor in the ring
    private volatile Integer leaderId = null; // learned (or self) leader
    private final PeerRegisterInterface peerRegister;

    private enum MessageType { ELECTION, LEADER }

    public Node(int nodeId, PeerRegisterInterface peerRegister) throws RemoteException {
        super();
        this.nodeId = nodeId;
        this.peerRegister = peerRegister;
    }


    @Override
    public synchronized void setNextNode(nodeApi nextNode) throws RemoteException {
        if (nextNode == null) throw new IllegalArgumentException("nextNode cannot be null");
        this.nextNode = nextNode;
    }

    @Override
    public int getNodeId() throws RemoteException {
        return nodeId;
    }

    @Override
    public void startElection() throws RemoteException {
        if (nextNode == null) throw new RemoteException("nextNode not set yet; ring not wired");
        System.out.println("Node " + nodeId + " starts election");
        nextNode.receiveMessage(nodeId, MessageType.ELECTION.name());
    }

    @Override
    public synchronized void receiveMessage(int message, String messageType) throws RemoteException {
        if (nextNode == null) throw new RemoteException("nextNode not set; ring broken at " + nodeId);

        if (MessageType.ELECTION.name().equals(messageType)) {
            handleElectionMessage(message);
        } else if (MessageType.LEADER.name().equals(messageType)) {
            handleLeaderMessage(message);
        } else {
            System.out.println("Node " + nodeId + " got unknown messageType=" + messageType);
        }
    }


    private void handleElectionMessage(int incomingId) throws RemoteException {
        if (incomingId > nodeId) {
            System.out.println("Node " + nodeId + " forwards ELECTION(" + incomingId + ")");
            nextNode.receiveMessage(incomingId, MessageType.ELECTION.name());
        } else if (incomingId == nodeId) {
            System.out.println("Node " + nodeId + " declares itself LEADER(" + nodeId + ")");
            this.leaderId = nodeId;

            try {
                if (peerRegister != null) {
                    peerRegister.setElectedLeader(nodeId);
                }
            } catch (Exception e) {
                System.err.println("Node " + nodeId + " failed to record winner at register: " + e.getMessage());
            }

            nextNode.receiveMessage(this.nodeId, MessageType.LEADER.name());
        }
    }

    private void handleLeaderMessage(int leader) throws RemoteException {
        if (leader == this.nodeId) {
            System.out.println("Node " + nodeId + " is the NEW LEADER (confirmed).");
            try {
                if (peerRegister != null) {
                    peerRegister.notifyElectionInProgress(false);
                }
            } catch (Exception e) {
                System.err.println("Node " + nodeId + " failed to notify register that election ended: " + e.getMessage());
            }
            return; // stop propagation
        }

        this.leaderId = leader;
        System.out.println("Node " + nodeId + " acknowledges: Node " + leader + " is the NEW LEADER");
        nextNode.receiveMessage(leader, MessageType.LEADER.name());
    }
}
