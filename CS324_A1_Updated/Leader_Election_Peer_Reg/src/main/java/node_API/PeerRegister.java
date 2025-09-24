package node_API;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
Paul Motufaga - S11213632
Lagilava Paulo - S11210953
Jay Naidu - S11211264
*/
public class PeerRegister extends UnicastRemoteObject implements PeerRegisterInterface {

    private final List<nodeApi> registeredPeers;
    private final Set<Integer> readyPeers = new HashSet<>();
    private final Set<Integer> startVotes = new HashSet<>();
    private boolean electionInProgress = false;
    private Integer electedLeader = null;

    public PeerRegister() throws RemoteException {
        this.registeredPeers = new ArrayList<>();
    }

    
    @Override
    public synchronized void registerPeer(nodeApi peer) throws RemoteException {
        ensureNoElection("Cannot register peer: Election is in progress.");

        final int newId = peer.getNodeId();
        for (nodeApi p : registeredPeers) {
            if (p.getNodeId() == newId) throw new RemoteException("A peer with ID " + newId + " already exists.");
        }

        registeredPeers.add(peer);
        System.out.println("Peer registered with ID: " + newId);

        registeredPeers.sort(Comparator.comparingInt(p -> {
            try { return p.getNodeId(); } catch (RemoteException e) { return Integer.MIN_VALUE; }
        }));

        readyPeers.clear();
        startVotes.clear();

        updateRingTopology();
        printTopology();
    }

    private void updateRingTopology() throws RemoteException {
        if (registeredPeers.isEmpty()) return;

        System.out.println("Ring wiring:");
        for (int i = 0; i < registeredPeers.size(); i++) {
            nodeApi current = registeredPeers.get(i);
            nodeApi next = registeredPeers.get((i + 1) % registeredPeers.size());
            current.setNextNode(next);
            System.out.println("  Node " + current.getNodeId() + " -> Node " + next.getNodeId());
        }
    }

    private void printTopology() throws RemoteException {
        if (registeredPeers.isEmpty()) return;
        StringBuilder sb = new StringBuilder("Topology: ");
        for (int i = 0; i < registeredPeers.size(); i++) sb.append(registeredPeers.get(i).getNodeId()).append(" -> ");
        sb.append(registeredPeers.get(0).getNodeId());
        System.out.println(sb);
    }

    @Override
    public synchronized void notifyElectionInProgress(boolean inProgress) throws RemoteException {
        this.electionInProgress = inProgress;
        if (inProgress) {
            System.out.println("Election is now in progress, blocking peer registration.");
        } else {
            System.out.println("Election has finished, peer registration is now open.");
            if (electedLeader != null) System.out.println(">>> The elected leader is Node " + electedLeader + " <<<");
            readyPeers.clear();
            startVotes.clear();
        }
    }

    @Override
    public synchronized void setElectedLeader(int leaderId) throws RemoteException {
        this.electedLeader = leaderId;
        
    }

    @Override
    public synchronized int getElectedLeader() throws RemoteException {   // <-- add this
        return electedLeader == null ? -1 : electedLeader;
    }


    private void ensureNoElection(String msg) throws RemoteException {
        if (electionInProgress) throw new RemoteException(msg);
    }

    @Override
    public synchronized void markReady(int nodeId) throws RemoteException {
        ensureNoElection("Election in progress; cannot mark READY.");
        if (!isKnownPeerId(nodeId)) throw new RemoteException("Unknown peer ID " + nodeId + " (register first).");
        readyPeers.add(nodeId);
        System.out.println("Peer " + nodeId + " is READY (" + readyPeers.size() + "/" + registeredPeers.size() + ")");
    }

    @Override
    public synchronized boolean allReady() throws RemoteException {
        return !registeredPeers.isEmpty() && readyPeers.size() == registeredPeers.size();
    }

    @Override
    public synchronized void requestStart(int nodeId) throws RemoteException {
        ensureNoElection("Election already running.");
        if (!isKnownPeerId(nodeId)) throw new RemoteException("Unknown peer ID " + nodeId + " (register first).");
        if (!readyPeers.contains(nodeId)) throw new RemoteException("Peer " + nodeId + " must be READY before START.");

        startVotes.add(nodeId);
        System.out.println("Start vote from " + nodeId + " (" + startVotes.size() + "/" + registeredPeers.size() + ")");

        if (startVotes.size() == registeredPeers.size()) {
            electionInProgress = true;
            System.out.println("All peers voted START → starting election on all nodes...");
            for (nodeApi p : registeredPeers) {
                final nodeApi peerRef = p; // CAPTURE!
                new Thread(() -> {
                    try {
                        System.out.println("Launching startElection() on Node " + safeId(peerRef));
                        peerRef.startElection();
                    } catch (Exception e) {
                        System.err.println("Failed to start election on a peer: " + e.getMessage());
                    }
                }, "StartElection-" + safeId(p)).start();
            }
        }
    }

    private boolean isKnownPeerId(int id) throws RemoteException {
        for (nodeApi p : registeredPeers) if (p.getNodeId() == id) return true;
        return false;
    }

    private String safeId(nodeApi p) {
        try { return String.valueOf(p.getNodeId()); } catch (Exception e) { return "?"; }
    }
}
