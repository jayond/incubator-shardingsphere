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

package org.apache.shardingsphere.shardingscaling.postgresql;

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.AbstractJdbcWriter;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.AbstractSqlBuilder;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataManager;

/**
 * postgreSQL writer.
 *
 * @author avalon566
 */
public final class PostgreSQLWriter extends AbstractJdbcWriter {

    public PostgreSQLWriter(final RdbmsConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        super(rdbmsConfiguration, dataSourceManager);
    }
    
    @Override
    protected AbstractSqlBuilder createSqlBuilder(final MetaDataManager metaDataManager) {
        return new PostgreSQLSqlBuilder(metaDataManager);
    }
    
    private static final class PostgreSQLSqlBuilder extends AbstractSqlBuilder {
        
        private PostgreSQLSqlBuilder(final MetaDataManager metaDataManager) {
            super(metaDataManager);
        }
        
        @Override
        public String getLeftIdentifierQuoteString() {
            return "\"";
        }
        
        @Override
        public String getRightIdentifierQuoteString() {
            return "\"";
        }
        
        @Override
        public String buildInsertSql(final String tableName) {
            StringBuilder columns = new StringBuilder();
            StringBuilder holder = new StringBuilder();
            for (String each : this.getMetaDataManager().getTableMetaData(tableName).getColumnNames()) {
                columns.append(String.format("%s%s%s,", getLeftIdentifierQuoteString(), each, getRightIdentifierQuoteString()));
                holder.append("?,");
            }
            columns.setLength(columns.length() - 1);
            holder.setLength(holder.length() - 1);
            String result = String.format("INSERT INTO %s%s%s(%s) VALUES(%s)", getLeftIdentifierQuoteString(), tableName, getRightIdentifierQuoteString(), columns.toString(), holder.toString());
            result += " ON CONFLICT (";
            for (String each : this.getMetaDataManager().getTableMetaData(tableName).getPrimaryKeyColumns()) {
                result += each + ",";
            }
            result = result.substring(0, result.length() - 1) + ") DO NOTHING";
            return result;
        }
    }
}
