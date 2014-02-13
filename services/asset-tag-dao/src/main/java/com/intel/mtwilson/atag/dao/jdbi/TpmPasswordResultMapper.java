/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.dao.jdbi;



import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.model.TpmPassword;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 *
 * @author stdalex
 */
public class TpmPasswordResultMapper implements ResultSetMapper<TpmPassword>  {

    @Override
    public TpmPassword map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        //return new TagValue(rs.getLong("id"), rs.getLong("tagId"), rs.getString("value"));  
        return new TpmPassword(rs.getLong("id"),rs.getString("uuid"),rs.getString("password"));
    }
    
}
