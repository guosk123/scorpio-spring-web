package com.scorpio.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XmlTests {

  private static final String CLICKHOUSE_SERVER_NAME = "clickhouse_servers";
  private static final String CH_STATS_SERVER_NAME = "ch_stats_servers";

  private static final String SHARD_ATTRIBUTE = "node_ip";

  // 0 不加密； 1 加密
  private static final String SECURE = "1";

  private static final String FORMAT_ENCODE = "UTF-8";
  private static final int FORMAT_INDENT_SIZE = 4;

  public static void main(String[] args) throws DocumentException {
    String host = "10.0.0.121";

    try {
      String configFilePath = "/opt/machloop/config/clickhouse/config.xml";
      if (!Paths.get(configFilePath).toFile().exists()) {
        System.out.println("configuration file not found.");
        return;
      }

      /*SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(new File(configFilePath));
      
      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        System.out.println("configuration file content parsing failed.");
        return;
      }
      
      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        System.out.println("configuration item [remote_servers] not found.");
        remoteServers = root.addElement("remote_servers");
        remoteServers.addAttribute("incl", "clickhouse_remote_servers");
      }
      
      // clickhouse_servers集群添加shard
      Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
      if (clickhouseServers == null) {
        // 集群不存在则创建
        clickhouseServers = remoteServers.addElement(CLICKHOUSE_SERVER_NAME);
      }
      // 先删除节点，防止重复
      removeShardElement(clickhouseServers, host);
      // 添加新的节点
      clickhouseServers.add(buildShardElement(host, "9440"));
      
      // ch_stats_servers集群添加shard
      Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
      if (chStatsServers == null) {
        // 集群不存在则创建
        chStatsServers = remoteServers.addElement(CH_STATS_SERVER_NAME);
      }
      // 先删除节点，防止重复
      removeShardElement(chStatsServers, host);
      // 添加新的节点
      chStatsServers.add(buildShardElement(host, "9441"));
      
      // 指定XML输出样式
      OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
      xmlFormat.setEncoding(FORMAT_ENCODE);
      xmlFormat.setIndentSize(FORMAT_INDENT_SIZE);
      
      XMLWriter writer = new XMLWriter(new FileWriter(new File(configFilePath)), xmlFormat);
      writer.write(document);
      writer.close();*/


      SAXReader saxReader = new SAXReader(); // 用来读取xml文档
      Document document = saxReader.read(new File(configFilePath));

      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        System.out.println("configuration file content parsing failed.");
        return;
      }

      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        System.out.println("configuration item [remote_servers] not found.");
        return;
      }

      // clickhouse_servers集群删除shard
      Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
      if (removeShardElement(clickhouseServers, host) == 0) {
        // clickhouse_servers集群内已经不存在节点，则删除集群
        remoteServers.remove(clickhouseServers);
      }

      // ch_stats_servers集群删除shard
      Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
      if (removeShardElement(chStatsServers, host) == 0) {
        // ch_stats_servers集群内已经不存在节点，则删除集群
        remoteServers.remove(chStatsServers);
      }

      XMLWriter writer = new XMLWriter(new FileWriter(new File(configFilePath)));
      writer.write(document);
      writer.close();

    } catch (IOException e) {
      e.printStackTrace();
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
  private static Element buildShardElement(String host, String port) {
    Element replicaElement = DocumentHelper.createElement("replica");

    Element hostElement = replicaElement.addElement("host");
    hostElement.setText(host);

    Element portElement = replicaElement.addElement("port");
    portElement.setText(port);

    Element userElement = replicaElement.addElement("user");
    userElement.setText("clickhouse");

    Element passwordElement = replicaElement.addElement("password");
    passwordElement.setText("Machloop@123");

    Element secureElement = replicaElement.addElement("secure");
    secureElement.setText(SECURE);

    Element shardElement = DocumentHelper.createElement("shard");
    shardElement.addAttribute(SHARD_ATTRIBUTE, host);
    shardElement.add(replicaElement);

    return shardElement;
  }

  /**
   * 移除节点
   * @param servers 集群节点
   * @param host 移除主机IP
   * @return 集群最终剩余节点数量
   */
  private static int removeShardElement(Element servers, String host) {
    if (servers == null) {
      return -1;
    }

    Iterator<Element> iterator = servers.elementIterator();
    while (iterator.hasNext()) {
      Element shardElement = iterator.next();

      Attribute nodeIp = shardElement.attribute(SHARD_ATTRIBUTE);
      if (nodeIp != null && host.equals(nodeIp.getValue())) {
        servers.remove(shardElement);
      }
    }

    return servers.elements().size();
  }

}
