/*
 * Licensed to Aduna under one or more contributor license agreements.
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD
 * License (the "License"); you may not use this file except in compliance
 * with the License. See the LICENSE.txt file distributed with this work
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.federation;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;

/**
 *
 * @author vagrant
 */
public class LoadDataTimeTest {
    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String zkServer1 = "192.168.33.50:2181";
        final String userName = "root";
        final String passWord = "root";

//        final Iterator<Entry<Key, Value>> iterator1;
//        final Iterator<Entry<Key, Value>> iterator2;

        final OverlapList ol1 = new OverlapList(zkServer1, instanceName);
        ol1.createConnection(userName, passWord);
        ol1.selectTable(tableURI);

        final Scanner sc1 = ol1.createScanner();
        final Iterator<Entry<Key, Value>> iterator1 = sc1.iterator();
        long count = 0;
        while (iterator1.hasNext()) {
            count++;
            iterator1.next();
        }
        System.out.println(count);
    }
}