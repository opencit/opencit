/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.common;

import org.jooq.Record;
import org.jooq.Result;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.*;

/**
 *
 * @author jbuhacoff
 */
public class JooqUtil {
    
    public static void mapResultSet(Result<Record> result, ResultMapper mapper) {
        Record current = null;
        for(Record r : result) {
            if( !mapper.isRelated(current, r) ) {
                current = r;
            }
        }
    }
    
    public static interface ResultMapper<T> {
        /**
         * This method is used to distinguish between two main records. For example when you
         * select a join between "table1" and "table2" where the relationship between them is 1:n,
         * you get one row for every match which means a row from table1 is repeated for each 
         * corresponding row in table2. 
         * In order to "fold" these rows to create objects with relations, we need to identify
         * rows that are
         * @param one may be null
         * @param two may be null
         * @return true iff one and two are both non-null and refer to the same main record
         */
        boolean isRelated(Record one, Record two);
        
        /**
         * Given 
         * @param record
         * @return 
         */
        T create(Record record);
    }
    
    public static class SelectionResultMapper<Selection> implements ResultMapper {
        @Override
        public boolean isRelated(Record one, Record two) {
            return one != null && two != null && one.getValue(MW_TAG_SELECTION.ID).equals(two.getValue(MW_TAG_SELECTION.ID));
        }

        @Override
        public Object create(Record record) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
