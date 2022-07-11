package com.scorpio.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author guosk
 *
 * create at 2022年1月21日, alpha-zurich-rest
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class XmlTest {

  private static final String filePath = "G:\\测试目录\\clickhouse\\config\\config_bak.xml";

  private static final String clickhouse_server_name = "clickhouse_servers";
  private static final String ch_stats_server_name = "ch_stats_servers";

  private static final String clickhouse_port = "9440";
  private static final String ch_stats_port = "9441";

  private static final String user = "clickhouse";
  private static final String password = "Machloop@123";
  private static final String secure = "1";

  private static final String format_encode = "UTF-8";
  private static final int format_indent_size = 4;


  public static void main(String[] args) throws DocumentException {
    // 场景：新增设备（添加节点）、设备IP变更（修改节点）、删除设备（删除节点）
    // 新增节点：判断是否有集群节点，没有则先创建集群节点；最终在集群节点下添加shard节点
    // 修改节点：判断是否有集群节点，没有则先创建集群节点；判断shard节点是否存在，不存在则创建，存在则修改IP
    // 删除节点：判断是否有集群节点，不存在则结束；存在则判断是否存在shard节点，存在则删除，不存在则结束；删除后判断集群节点内是否还有子节点，如果没有则删除集群节点

    // 新增、修改、删除时 ，都需要注意同步修改两个集群信息，集群名称：clickhouse_servers ch_stats_servers

    SAXReader saxReader = new SAXReader(); // 用来读取xml文档

    Document document = saxReader.read(new File(filePath));
    Element root = document.getRootElement();

    System.out.println(root.getName());

    Element element = root.element("remote_servers");
    System.out.println(element == null);

    System.out.println(element.getName());
    System.out.println(element.attribute("incl").getValue());

    Element test = element.addElement("test");
    Element shard = DocumentHelper.createElement("shard");
    Element replica = DocumentHelper.createElement("replica");

    Element host = replica.addElement("host");
    host.setText("1.1.1.1");

    Element port = replica.addElement("port");
    port.setText("22");

    Element user = replica.addElement("user");
    user.setText("adm");

    Element password = replica.addElement("password");
    password.setText("Machloop@123");

    shard.add(replica);
    shard.addAttribute("node_ip", "1.1.1.1");
    test.add(shard);

    try {
      OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
      xmlFormat.setEncoding(format_encode);
      xmlFormat.setIndentSize(format_indent_size);

      XMLWriter writer = new XMLWriter(new FileWriter(new File(filePath)), xmlFormat);
      writer.write(document);
      writer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void addNode() {
    // 新增节点：判断是否有集群节点，没有则先创建集群节点；最终在集群节点下添加shard节点
    String host = "10.0.0.121";

    try {
      SAXReader saxReader = new SAXReader(); // 用来读取xml文档

      Document document = saxReader.read(new File(filePath));

      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        // TODO xml文件内容缺失严重
        return;
      }

      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        // TODO xml文件内缺失集群配置根节点
        remoteServers = root.addElement("remote_servers");
        remoteServers.addAttribute("incl", "clickhouse_remote_servers");
      }

      // clickhouse_servers集群添加shard
      Element clickhouseServers = remoteServers.element(clickhouse_server_name);
      if (clickhouseServers == null) {
        // 集群不存在则创建
        clickhouseServers = remoteServers.addElement(clickhouse_server_name);
      }
      // 先删除，防止重复
      removeShardElement(clickhouseServers, host);
      clickhouseServers.add(buildShardElement(host, clickhouse_port));

      // ch_stats_servers集群添加shard
      Element chStatsServers = remoteServers.element(ch_stats_server_name);
      if (chStatsServers == null) {
        // 集群不存在则创建
        chStatsServers = remoteServers.addElement(ch_stats_server_name);
      }
      // 先删除，防止重复
      removeShardElement(chStatsServers, host);
      chStatsServers.add(buildShardElement(host, ch_stats_port));

      // 指定XML输出样式
      OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
      xmlFormat.setEncoding(format_encode);
      xmlFormat.setIndentSize(format_indent_size);

      XMLWriter writer = new XMLWriter(new FileWriter(new File(filePath)), xmlFormat);
      writer.write(document);
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void updateNode() {
    // 修改节点：判断是否有集群节点，没有则先创建集群节点；判断shard节点是否存在，不存在则创建，存在则修改IP
    String sourceHost = "10.0.0.122";
    String targetHost = "10.0.0.121";

    try {
      SAXReader saxReader = new SAXReader(); // 用来读取xml文档

      Document document = saxReader.read(new File(filePath));

      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        // TODO xml文件内容缺失严重
        return;
      }

      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        // TODO xml文件内缺失集群配置根节点
        remoteServers = root.addElement("remote_servers");
        remoteServers.addAttribute("incl", "clickhouse_remote_servers");
      }

      // clickhouse_servers集群变更shard
      Element clickhouseServers = remoteServers.element(clickhouse_server_name);
      if (clickhouseServers == null) {
        // 不存在则创建
        clickhouseServers = remoteServers.addElement(clickhouse_server_name);
        clickhouseServers.add(buildShardElement(targetHost, clickhouse_port));
      } else {
        // 存在则先删除原有节点，再新建节点
        removeShardElement(clickhouseServers, sourceHost);
        removeShardElement(clickhouseServers, targetHost);
        clickhouseServers.add(buildShardElement(targetHost, clickhouse_port));
      }

      // ch_stats_servers集群变更shard
      Element chStatsServers = remoteServers.element(ch_stats_server_name);
      if (chStatsServers == null) {
        // 不存在则创建
        chStatsServers = remoteServers.addElement(ch_stats_server_name);
        chStatsServers.add(buildShardElement(targetHost, ch_stats_port));
      } else {
        // 存在则先删除原有节点，再新建节点
        removeShardElement(chStatsServers, sourceHost);
        removeShardElement(chStatsServers, targetHost);
        chStatsServers.add(buildShardElement(targetHost, ch_stats_port));
      }

      // 指定XML输出样式
      OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
      xmlFormat.setEncoding(format_encode);
      xmlFormat.setIndentSize(format_indent_size);

      XMLWriter writer = new XMLWriter(new FileWriter(new File(filePath)), xmlFormat);
      writer.write(document);
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void deleteNode() {
    // 删除节点：判断是否有集群节点，不存在则结束；存在则判断是否存在shard节点，存在则删除，不存在则结束；删除后判断集群节点内是否还有子节点，如果没有则删除集群节点
    String host = "10.0.0.121";

    try {
      SAXReader saxReader = new SAXReader(); // 用来读取xml文档

      Document document = saxReader.read(new File(filePath));

      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        // TODO xml文件内容缺失严重
        return;
      }

      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        // TODO xml文件内缺失集群配置根节点
        return;
      }

      // clickhouse_servers集群删除shard
      Element clickhouseServers = remoteServers.element(clickhouse_server_name);
      if (removeShardElement(clickhouseServers, host) == 0) {
        remoteServers.remove(clickhouseServers);
      }

      // ch_stats_servers集群删除shard
      Element chStatsServers = remoteServers.element(ch_stats_server_name);
      if (removeShardElement(chStatsServers, host) == 0) {
        remoteServers.remove(chStatsServers);
      }

      XMLWriter writer = new XMLWriter(new FileWriter(new File(filePath)));
      writer.write(document);
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    }
  }

  /**
   * 构建shard
   * @param host 主机IP 
   * @param port 端口
   * @return 
   */
  private Element buildShardElement(String host, String port) {
    Element replicaElement = DocumentHelper.createElement("replica");

    Element hostElement = replicaElement.addElement("host");
    hostElement.setText(host);

    Element portElement = replicaElement.addElement("port");
    portElement.setText(port);

    Element userElement = replicaElement.addElement("user");
    userElement.setText(user);

    Element passwordElement = replicaElement.addElement("password");
    passwordElement.setText(password);

    Element secureElement = replicaElement.addElement("secure");
    secureElement.setText(secure);

    Element shardElement = DocumentHelper.createElement("shard");
    shardElement.addAttribute("node_ip", host);
    shardElement.add(replicaElement);

    return shardElement;
  }

  /**
   * 移除节点
   * @param servers 集群节点
   * @param host 移除主机IP
   * @return 集群最终剩余节点数量
   */
  private int removeShardElement(Element servers, String host) {
    if (servers == null) {
      return -1;
    }

    Iterator<Element> iterator = servers.elementIterator();
    while (iterator.hasNext()) {
      Element shardElement = iterator.next();

      Attribute nodeIp = shardElement.attribute("node_ip");
      if (nodeIp != null && StringUtils.equals(nodeIp.getValue(), host)) {
        servers.remove(shardElement);
      }
    }

    return servers.elements().size();
  }

}
