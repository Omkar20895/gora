/**
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
package org.apache.gora.mongodb.store;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.gora.examples.generated.Employee;
import org.apache.gora.examples.generated.WebPage;
import org.apache.gora.mongodb.GoraMongodbTestDriver;
import org.apache.gora.mongodb.utils.BSONDecorator;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreTestBase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.Collections;

import org.apache.avro.util.Utf8;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;

public abstract class TestMongoStore extends DataStoreTestBase {

  private int keySequence;

  @Deprecated
  @Override
  protected DataStore<String, Employee> createEmployeeDataStore()
          throws IOException {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @Override
  protected DataStore<String, WebPage> createWebPageDataStore()
          throws IOException {
    throw new UnsupportedOperationException();
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    keySequence = 1;
  }

  public GoraMongodbTestDriver getTestDriver() {
    return (GoraMongodbTestDriver) testDriver;
  }

  @Ignore("Skip until GORA-66 is fixed: need better semantic for end/start keys")
  @Override
  public void testDeleteByQueryFields() throws IOException {
    // Skip until GORA-66 is fixed: need better semantic for end/start keys
  }

  @Ignore("Skip until GORA-66 is fixed: need better semantic for end/start keys")
  @Override
  public void testQueryKeyRange() throws Exception {
    // Skip until GORA-66 is fixed: need better semantic for end/start keys
  }

  @Ignore("MongoStore doesn't support 3 types union field yet")
  @Override
  public void testGet3UnionField() throws Exception {
    // MongoStore doesn't support 3 types union field yet
  }

  @Test
  public void testFromMongoList_null() throws Exception {
    MongoStore store = new MongoStore();
    BasicDBObject noField = new BasicDBObject();
    String field = "myField";
    Object item = store.fromMongoList(field, null, new BSONDecorator(noField),
            null);
    assertNotNull(item);
  }

  @Test
  public void testFromMongoList_empty() throws Exception {
    MongoStore store = new MongoStore();
    String field = "myField";
    BasicDBObject emptyField = new BasicDBObject(field, new BasicDBList());
    Object item = store.fromMongoList(field, null,
            new BSONDecorator(emptyField), null);
    assertNotNull(item);
  }

  @Test
  public void testFromMongoMap_null() throws Exception {
    MongoStore store = new MongoStore();
    BasicDBObject noField = new BasicDBObject();
    String field = "myField";
    Object item = store.fromMongoMap(field, null, new BSONDecorator(noField),
            null);
    assertNotNull(item);
  }

  @Test
  public void testFromMongoMap_empty() throws Exception {
    MongoStore store = new MongoStore();
    String field = "myField";
    BasicDBObject emptyField = new BasicDBObject(field, new BasicDBObject());
    Object item = store.fromMongoMap(field, null,
            new BSONDecorator(emptyField), null);
    assertNotNull(item);
  }

  @Test
  public void testResultProgress() throws Exception {
    Query<String, WebPage> q;

    // empty
    q = webPageStore.newQuery();
    assertProgress(q, 1);

    addWebPage();

    // one
    q = webPageStore.newQuery();
    assertProgress(q, 0, 1);

    addWebPage();

    // two
    q = webPageStore.newQuery();
    assertProgress(q, 0, 0.5f, 1);

    addWebPage();

    // three
    q = webPageStore.newQuery();
    assertProgress(q, 0, 0.33f, 0.66f, 1);
  }

  @Test
  public void testResultProgressWithLimit() throws Exception {
    for (int i = 0; i < 5; i++) {
      addWebPage();
    }
    Query<String, WebPage> q = webPageStore.newQuery();
    q.setLimit(2);

    assertProgress(q, 0, 0.5f, 1);
  }

  private void assertProgress(Query<String, WebPage> q, float... progress) throws Exception {
    Result<String, WebPage> r = webPageStore.execute(q);
    int i = 0;
    do {
      assertEquals(progress[i++], r.getProgress(), 0.01f);
    } while (r.next());
    r.close();
  }

  private void addWebPage() {
    String key = String.valueOf(keySequence++);
    WebPage p1 = webPageStore.newPersistent();
    p1.setUrl(new Utf8(key));
    p1.setHeaders(Collections.singletonMap((CharSequence) "header", (CharSequence) "value"));
    webPageStore.put(key, p1);
  }
}
