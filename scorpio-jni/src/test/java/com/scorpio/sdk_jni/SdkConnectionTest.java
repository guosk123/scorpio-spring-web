package com.machloop.iosp.sdk_jni;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.machloop.iosp.sdk.Constants.Sort;
import com.machloop.iosp.sdk.CursorFullObjectResult;
import com.machloop.iosp.sdk.CursorMetadataResult;
import com.machloop.iosp.sdk.FullObject;
import com.machloop.iosp.sdk.Metadata;
import com.machloop.iosp.sdk.SearchCondition;
import com.machloop.iosp.sdk.WriteFullObject;
import com.machloop.iosp.sdk.highlevel.AnalysResult;
import com.machloop.iosp.sdk.highlevel.IospConnection;
import com.machloop.iosp.sdk.subscribe.ConsumeContext;
import com.machloop.iosp.sdk.subscribe.ExceptionHandler;
import com.machloop.iosp.sdk.subscribe.MetadataListener;
import com.machloop.iosp.sdk.subscribe.ObjectListener;
import com.machloop.iosp.sdk.subscribe.SubscribeObjectTask;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IospConnectionTest {

  private static IospConnection conn;
  private static IospConnection connByteBuffer;

  private static String writeObjectId;
  private static String writeLabel = "test_label";
  private static String writeSite = "test_site";
  private static byte[] writeContent = "this is test content".getBytes();
  private static String writeName = "test.txt";
  private static Date createTime;

  private final static String ZONE = "mzy";
  private final static String IP = "10.0.0.181";
  private final static short PORT = 1100;
  private final static String CLIENTID = "C5";
  private final static String CLIENTTOKEN = "@MHGGBF8XjYzk@irTwsTmT@MAsbX5G-E";


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    System.setProperty("iosp.sdk.logpath", "/home/sdktest");
  }

  @Before
  public void setUp() throws Exception {
    conn = new IospConnection(IP, PORT, CLIENTID, CLIENTTOKEN, false);
    connByteBuffer = new IospConnection(IP, PORT, CLIENTID, CLIENTTOKEN, true);

    conn.connect();
    connByteBuffer.connect();
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
    connByteBuffer.close();
  }

  @Test
  public void test1Write() throws IOException {
    // write
    createTime = new Date();
    WriteFullObject o = WriteFullObject.create(ZONE, writeName, ByteBuffer.wrap(writeContent))
        .setLabel(writeLabel).setSite(writeSite).setCreateTime(createTime).build();
    writeObjectId = conn.write(o);
    Assert.assertNotNull(writeObjectId);
  }

  @Test
  public void test2Read() throws IOException {
    // read
    FullObject rr = conn.read(writeObjectId);
    checkFullObject(rr);
  }

  @Test
  public void test3ReadMetadata() throws IOException {
    // read
    Metadata rr = conn.readMetadata(writeObjectId);
    checkMetadata(rr);
  }

  @Test
  public void test4Search() throws IOException {
    for (SearchCondition condition : conditions()) {
      CursorFullObjectResult rr = conn.search(condition, Sort.UNSORT, 1000, 0);
      Assert.assertTrue(rr.getObjects().length == 1);
      Assert.assertNotNull(rr.getDatanodeMsg());
      checkFullObject(rr.getObjects()[0]);

      CursorFullObjectResult rr2 = conn.search(condition, Sort.CREATE_TIME_ASC, 1000, 0);
      Assert.assertTrue(rr2.getObjects().length == 1);
      Assert.assertNotNull(rr2.getDatanodeMsg());
      checkFullObject(rr2.getObjects()[0]);

      CursorFullObjectResult rr3 = conn.search(condition, Sort.CREATE_TIME_DESC, 1000, 0);
      Assert.assertTrue(rr3.getObjects().length == 1);
      Assert.assertNotNull(rr3.getDatanodeMsg());
      checkFullObject(rr3.getObjects()[0]);
    }
  }

  @Test
  public void test5SearchMetadata() throws IOException {
    for (SearchCondition condition : conditions()) {
      CursorMetadataResult rr = conn.searchMetadata(condition, Sort.UNSORT, 1000, 0);
      Assert.assertTrue(rr.getMetadatas().length == 1);
      Assert.assertNotNull(rr.getDatanodeMsg());
      checkMetadata(rr.getMetadatas()[0]);

      CursorMetadataResult rr2 = conn.searchMetadata(condition, Sort.CREATE_TIME_ASC, 1000, 0);
      Assert.assertTrue(rr2.getMetadatas().length == 1);
      Assert.assertNotNull(rr2.getDatanodeMsg());
      checkMetadata(rr2.getMetadatas()[0]);

      CursorMetadataResult rr3 = conn.searchMetadata(condition, Sort.CREATE_TIME_DESC, 1000, 0);
      Assert.assertTrue(rr3.getMetadatas().length == 1);
      Assert.assertNotNull(rr3.getDatanodeMsg());
      checkMetadata(rr3.getMetadatas()[0]);
    }
  }

  @Test
  public void test6SearchAnalys() throws IOException {
    for (SearchCondition condition : conditions()) {
      AnalysResult rr = conn.searchAnalys(condition);
      Assert.assertNotNull(rr.getAnalysMsg());
      Assert.assertNotNull(rr.getDatanodeMsg());
      System.out.println(rr.getAnalysMsg());
    }
  }

  @Test
  public void test7SearchWriteDisk() throws IOException {
    for (SearchCondition condition : conditions()) {
      new File("/home/out/0/" + writeObjectId).deleteOnExit();
      conn.searchAndWriteDisk(condition, "/home/out", 1000);
      Assert.assertTrue(new File("/home/out/0/" + writeObjectId).exists());
      Assert.assertEquals(new File("/home/out/0/" + writeObjectId).length(), writeContent.length);
    }
  }

  @Test
  public void test8ModifyLabel() throws IOException {
    System.out.println("write object id: " + writeObjectId);
    conn.modifyLabel(writeObjectId, "newLabel");
    FullObject newrr = conn.read(writeObjectId);
    Assert.assertEquals("newLabel", newrr.getLabel());
  }

  @Test
  public void test9BigFile() throws IOException {
    {
      FullObject fullObject = conn.read("Z76_300000023a2_f328");
      Assert.assertNull(fullObject.getContent());
      Assert.assertNull(fullObject.getContentByteArray());
      Assert.assertEquals(fullObject.getObjectSize(), -1);
      System.out.println("content: " + fullObject.getContent() + ", byte: "
          + fullObject.getContentByteArray() + ", size: " + fullObject.getObjectSize());

      SearchCondition condition = SearchCondition
          .create(ZONE, fullObject.getCreateTimeDate(), fullObject.getCreateTimeDate())
          .setLabel(fullObject.getLabel()).setSite(fullObject.getSite()).build();
      CursorFullObjectResult r = conn.search(condition, Sort.CREATE_TIME_ASC, 1000, 0);
      Assert.assertEquals(r.getObjects().length, 1);
      Assert.assertNull(r.getObjects()[0].getContent());
      Assert.assertNull(r.getObjects()[0].getContentByteArray());
      Assert.assertEquals(r.getObjects()[0].getObjectSize(), -1);
    }
    {
      FullObject fullObject = conn.read("Z76_300000023a2_f325");
      Assert.assertNotNull(fullObject.getContent());
      Assert.assertNotNull(fullObject.getContentByteArray());
      Assert.assertNotEquals(fullObject.getObjectSize(), -1);
      System.out.println("content: " + fullObject.getContent() + ", byte: "
          + fullObject.getContentByteArray() + ", size: " + fullObject.getObjectSize());

      SearchCondition condition = SearchCondition
          .create(ZONE, fullObject.getCreateTimeDate(), fullObject.getCreateTimeDate())
          .setLabel(fullObject.getLabel()).setSite(fullObject.getSite()).build();
      CursorFullObjectResult r = conn.search(condition, Sort.CREATE_TIME_ASC, 1000, 0);
      Assert.assertEquals(r.getObjects().length, 1);
      Assert.assertNotNull(r.getObjects()[0].getContent());
      Assert.assertNotNull(r.getObjects()[0].getContentByteArray());
      Assert.assertNotEquals(r.getObjects()[0].getObjectSize(), -1);
    }

  }

  @Test
  public void test10Subscribe() throws IOException {
    List<SearchCondition> conditions = conditions();
    SubscribeObjectTask subscribe = conn.subscribe(conditions.get(0), 0, new ObjectListener() {

      @Override
      public void consume(FullObject[] objects, ConsumeContext context) {
        if (objects.length > 0) {
          checkFullObject(objects[0]);
        }
      }
    });

    subscribe.setExceptionHandler(new ExceptionHandler() {

      @Override
      public boolean onRuningFailure(Exception e) {
        System.out.println("出现异常...");

        return false;
      }
    });

    subscribe.start();
  }

  @Test
  public void test11SubscribeMetadata() throws IOException {
    List<SearchCondition> conditions = conditions();
    conn.subscribeMetadata(conditions.get(0), 0, new MetadataListener() {

      @Override
      public void consume(Metadata[] metadatas, ConsumeContext context) {
        if (metadatas.length > 0) {
          checkMetadata(metadatas[0]);
        }
      }
    }).start();
  }

  private static void checkFullObject(FullObject rr) {
    Assert.assertEquals(rr.getLabel(), writeLabel);
    Assert.assertEquals(rr.getSite(), writeSite);
    Assert.assertEquals(rr.getObjectName(), writeName);
    Assert.assertEquals(rr.getZone(), ZONE);
    Assert.assertEquals(rr.getObjectId(), writeObjectId);
    Assert.assertArrayEquals(rr.getContentByteArray(), writeContent);
    Assert.assertEquals(rr.getCreateTimeDate(), new Date(createTime.getTime() / 1000 * 1000));
    Assert.assertEquals(rr.getCreateTime(), (createTime.getTime() / 1000));
    Assert.assertEquals(rr.getObjectSize(), writeContent.length);
  }

  private static void checkMetadata(Metadata rr) {
    Assert.assertEquals(rr.getLabel(), writeLabel);
    Assert.assertEquals(rr.getSite(), writeSite);
    Assert.assertEquals(rr.getObjectName(), writeName);
    Assert.assertEquals(rr.getZone(), ZONE);
    Assert.assertEquals(rr.getObjectId(), writeObjectId);
    Assert.assertEquals(rr.getCreateTimeDate(), new Date(createTime.getTime() / 1000 * 1000));
    Assert.assertEquals(rr.getCreateTime(), (createTime.getTime() / 1000));
    Assert.assertEquals(rr.getObjectSize(), writeContent.length);
  }

  private static List<SearchCondition> conditions() {
    List<SearchCondition> l = new ArrayList<>();
    l.add(SearchCondition.create(ZONE, createTime, createTime).setLabel(writeLabel)
        .setSite(writeSite).build());
    l.add(SearchCondition.create(ZONE, createTime, createTime).setSite(writeSite).build());
    l.add(SearchCondition.create(ZONE, createTime, createTime).setLabel(writeLabel).build());
    l.add(SearchCondition.create(ZONE, createTime, createTime).build());
    return l;
  }
}
