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

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.accumulo.core.bloomfilter.BloomFilter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.keyfunctor.RowFunctor;
import org.apache.accumulo.core.security.Authorizations;

import jcifs.smb.SmbFile;

/**
 *
 * @author vagrant
 */
public class CreateNewURIIndexTest {
    public static void main(final String[] args) throws Exception {
        final String url13 = "smb://192.168.33.30/share/3";
        final SmbFile file13 = new SmbFile(url13);
        ObjectInputStream in13 = null;
        BloomFilter instance13 = null;
        try {
            in13 = new ObjectInputStream(file13.getInputStream());
            instance13 = (BloomFilter)in13.readObject();
        } finally {
            if (in13 != null) {
                in13.close();
            }
//            file3.close();
        }

        final String url15 = "smb://192.168.33.50/share/5";
        final SmbFile file15 = new SmbFile(url15);
//        final FileInputStream file5 = new FileInputStream("/home/vagrant/share/5");
        ObjectInputStream in15 = null;
        BloomFilter instance15 = null;
        try {
            in15 = new ObjectInputStream(file15.getInputStream());

            // Method for deserialization of object
            instance15 = (BloomFilter)in15.readObject();
        } finally {
            if (in15 != null) {
                in15.close();
            }
//            file5.close();
        }

        final long start = System.currentTimeMillis();

        final long phase2 = System.currentTimeMillis();
        System.out.println(phase2 - start);

        final List<String> overlap13 = new ArrayList<String>();
        final List<String> overlap15 = new ArrayList<String>();
        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String tableNewURI13 = "new_URI_index_13";
        final String tableNewURI15 = "new_URI_index_15";
        final String zkServer1 = "192.168.33.10:2181";
        final String userName = "root";
        final String passWord = "root";

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(userName, new PasswordToken(passWord));

        final Scanner scan1 = conn1.createScanner(tableURI, new Authorizations());

        final Iterator<Entry<Key, Value>> iterator1 = scan1.iterator();
        while (iterator1.hasNext()) {
            final Entry<Key, Value> entry1 = iterator1.next();
            final Key accumuloKey = entry1.getKey();
            final RowFunctor rowFunctor = new RowFunctor();
            final org.apache.hadoop.util.bloom.Key key1 = rowFunctor.transform(accumuloKey);
            if (instance13.membershipTest(key1)) {
                overlap13.add(key1.toString());
            }
            if (instance15.membershipTest(key1)) {
                overlap15.add(key1.toString());
            }
        }
        final TableOperations ops = conn1.tableOperations();
        if (!ops.exists(tableNewURI13)) {
            ops.create(tableNewURI13);
        }
        if (!ops.exists(tableNewURI15)) {
            ops.create(tableNewURI15);
        }
        TestUtils.addURIs(overlap13, conn1, tableNewURI13);
        TestUtils.addURIs(overlap15, conn1, tableNewURI15);

        final long end = System.currentTimeMillis();

        System.out.println(end - phase2);
    }
}