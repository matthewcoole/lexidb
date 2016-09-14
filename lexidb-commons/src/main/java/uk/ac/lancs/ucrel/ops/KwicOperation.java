package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.Kwic;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface KwicOperation extends Operation {
    void search(String[] searchTerms, int context, int limit, int sortType, int sortPos, boolean reverseOrder, int pageLength) throws RemoteException;

    List<Kwic> it() throws RemoteException;

    int getLength() throws RemoteException;

    long getTime() throws RemoteException;
}
