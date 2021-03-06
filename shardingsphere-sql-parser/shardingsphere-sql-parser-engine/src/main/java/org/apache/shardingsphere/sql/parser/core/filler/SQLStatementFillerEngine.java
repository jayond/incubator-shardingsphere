/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.core.filler;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.sql.parser.core.rule.registry.ParseRuleRegistry;
import org.apache.shardingsphere.sql.parser.core.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.AbstractSQLStatement;

import java.util.Collection;

/**
 * SQL statement filler engine.
 */
@RequiredArgsConstructor
public final class SQLStatementFillerEngine {
    
    private final ParseRuleRegistry parseRuleRegistry;
    
    private final String databaseTypeName;
    
    /**
     * Fill SQL statement.
     *
     * @param sqlSegments SQL segments
     * @param parameterMarkerCount parameter marker count
     * @param rule SQL statement rule
     * @return SQL statement
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SQLStatement fill(final Collection<SQLSegment> sqlSegments, final int parameterMarkerCount, final SQLStatementRule rule) {
        SQLStatement result = rule.getSqlStatementClass().newInstance();
        Preconditions.checkArgument(result instanceof AbstractSQLStatement, "%s must extends AbstractSQLStatement", result.getClass().getName());
        ((AbstractSQLStatement) result).setParametersCount(parameterMarkerCount);
        result.getAllSQLSegments().addAll(sqlSegments);
        for (SQLSegment each : sqlSegments) {
            Optional<SQLSegmentFiller> filler = parseRuleRegistry.findSQLSegmentFiller(databaseTypeName, each.getClass());
            if (filler.isPresent()) {
                filler.get().fill(each, result);
            }
        }
        return result;
    }
}
