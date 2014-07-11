/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.resource;

import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcFilterCriteria;
import com.intel.mtwilson.rpc.v2.model.RpcCollection;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractResource;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.launcher.ws.ext.V2;
//import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import com.intel.mtwilson.rpc.v2.model.RpcLocator;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import com.intel.mtwilson.v2.rpc.jdbi.MyJdbi;
import com.intel.mtwilson.v2.rpc.jdbi.RpcDAO;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/rpcs")
public class RpcRepository implements DocumentRepository<Rpc, RpcCollection, RpcFilterCriteria, RpcLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RpcRepository.class);
    private static final HashMap<UUID, Rpc> data = new HashMap<UUID, Rpc>(); 

    @Override
    public RpcCollection search(RpcFilterCriteria criteria) {
        log.debug("Search id {} name {}", criteria.id, criteria.nameEqualTo);
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
        
        RpcCollection rpcs = new RpcCollection();
        if( criteria.id != null ) {
            Rpc item = dao.findStatusById(criteria.id);
            rpcs.getDocuments().add(item);
        }
        else if( criteria.nameEqualTo != null ) {
            List<Rpc> items = dao.findStatusByName(criteria.nameEqualTo);
            for(Rpc item : items) {
                rpcs.getDocuments().add(item);
            }
        }
        else if( criteria.status != null ) {
            List<Rpc> items = dao.findStatusByStatus(criteria.status.toString());
            for(Rpc item : items) {
                rpcs.getDocuments().add(item);
            }
        }
        return rpcs;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }
    /*
    private Rpc convert(RpcPriv from) {
            Rpc rpc = new Rpc();
            rpc.setId(from.getId());
            rpc.setName(from.getName());
            rpc.setStatus(from.getStatus());
            rpc.setCurrent(from.getCurrent());
            rpc.setMax(from.getMax());
            return rpc;
    }*/

    @Override
    public Rpc retrieve(RpcLocator locator) {
        log.debug("Retrieve id {}", locator.id);
        if (locator.id == null) {
            return null;
        }
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
            Rpc item = dao.findStatusById(locator.id);
        return item;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    public RpcPriv retrieveInput(RpcLocator locator) {
        if (locator.id == null) {
            return null;
        }
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
            RpcPriv item = dao.findById(locator.id);
        return item;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    public RpcPriv retrieveOutput(RpcLocator locator) {
        if (locator.id == null) {
            return null;
        }
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
            RpcPriv item = dao.findById(locator.id);
        return item;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    /**
     * Clients are not allowed to create rpc status resources - these are
     * automatically created when the RPC api itself is invoked with some input.
     * But the AsyncRpc class needs to create the rpc status records using
     * create(), and the executor will need to update their progress with
     * store()
     *
     * @param item
     */
    @Override
    public void create(Rpc item) {
        throw new UnsupportedOperationException("Use the RPC interface to make RPC requests"); // only AsyncRpc is allowed to make requests; it uses the create(RpcPriv item) method
    }
    public void create(RpcPriv item) {
        log.debug("Create id {}", item.getId());
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
           dao.insert(item.getId(), item.getName(), item.getInput(), item.getOutput(), item.getStatus().name(), item.getCurrent(), item.getMax());
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    /**
     * Clients are not allowed to update rpc status resources - these are
     * automatically updated by the server while the RPC method is running or
     * after it completes.
     *
     * @param item
     */
    @Override
    public void store(Rpc item) {
        throw new UnsupportedOperationException("Use the RPC interface to make RPC requests"); // AsyncPriv for creating requests and RpcInvoker for updating them use store(RpcPriv item)
     }
    public void store(RpcPriv item) {
        log.debug("Store id {}", item.getId());
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
           dao.update(item.getId(), item.getName(), item.getInput(), item.getOutput(), item.getStatus().name(), item.getCurrent(), item.getMax());
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    @Override
    public void delete(RpcLocator locator) {
        if( locator.id == null ) { return; }
        UUID uuid = locator.id;
        RpcDAO dao = null;
        try {
         dao = MyJdbi.rpc();
         dao.delete(uuid);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
           if( dao != null ) { dao.close(); }
        }
    }

    
    @Override
    public void delete(RpcFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
