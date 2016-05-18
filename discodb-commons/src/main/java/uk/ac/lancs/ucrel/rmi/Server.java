package uk.ac.lancs.ucrel.rmi;

import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

public interface Server extends Remote {
    boolean isShutdown() throws RemoteException;
    void shutdown() throws RemoteException;
    InsertResult insert() throws RemoteException;
    InsertResult insertLocal() throws RemoteException;
    Result kwic(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int page) throws RemoteException;
    Result list(String searchTerm) throws RemoteException;
    Result it() throws RemoteException;
    Result status() throws RemoteException;
    InsertResult lastInsert() throws RemoteException;
    boolean sendRaw(String filename, byte[] data) throws RemoteException;
    boolean distributeRaw() throws RemoteException;
    boolean sendRawToInsert(String filename, byte[] data) throws RemoteException;
    boolean isAvailable() throws RemoteException;
    void notify(String server) throws RemoteException;
    boolean equals(Server s) throws RemoteException;
    Properties getProperties() throws RemoteException;
}
