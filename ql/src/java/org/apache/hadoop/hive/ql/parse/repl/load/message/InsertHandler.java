/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.parse.repl.load.message;

import org.apache.hadoop.hive.metastore.messaging.InsertMessage;
import org.apache.hadoop.hive.ql.exec.Task;
import org.apache.hadoop.hive.ql.parse.SemanticException;

import java.io.Serializable;
import java.util.List;

class InsertHandler extends AbstractMessageHandler {
  @Override
  public List<Task<? extends Serializable>> handle(Context withinContext)
      throws SemanticException {
    InsertMessage insertMessage = deserializer.getInsertMessage(withinContext.dmd.getPayload());
    String actualDbName =
        withinContext.isDbNameEmpty() ? insertMessage.getDB() : withinContext.dbName;
    String actualTblName =
        withinContext.isTableNameEmpty() ? insertMessage.getTable() : withinContext.tableName;

    Context currentContext = new Context(withinContext, actualDbName, actualTblName);
    // Piggybacking in Import logic for now
    TableHandler tableHandler = new TableHandler();
    List<Task<? extends Serializable>> tasks = tableHandler.handle(currentContext);
    readEntitySet.addAll(tableHandler.readEntities());
    writeEntitySet.addAll(tableHandler.writeEntities());
    databasesUpdated.putAll(tableHandler.databasesUpdated);
    tablesUpdated.putAll(tableHandler.tablesUpdated);
    return tasks;
  }
}
