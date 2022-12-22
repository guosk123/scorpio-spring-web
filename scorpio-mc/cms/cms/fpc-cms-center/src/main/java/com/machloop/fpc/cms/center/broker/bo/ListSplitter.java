package com.machloop.fpc.cms.center.broker.bo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.common.message.Message;

import com.machloop.alpha.common.Constants;

/**
 * @author guosk
 *
 * create at 2021年11月22日, rocketmq
 */
public class ListSplitter implements Iterator<List<Message>> {

  private int sizeLimit = 4 * Constants.BLOCK_DEFAULT_SIZE * Constants.BLOCK_DEFAULT_SIZE;
  private final List<Message> messages;
  private int currIndex;

  public ListSplitter(List<Message> messages, int maxMessageSize) {
    this.messages = messages;
    this.sizeLimit = maxMessageSize * Constants.BLOCK_DEFAULT_SIZE;
  }

  @Override
  public boolean hasNext() {
    return currIndex < messages.size();
  }

  @Override
  public List<Message> next() {
    int startIndex = getStartIndex();
    int nextIndex = startIndex;
    int totalSize = 0;
    for (; nextIndex < messages.size(); nextIndex++) {
      Message message = messages.get(nextIndex);
      int tmpSize = calcMessageSize(message);
      if (tmpSize + totalSize > sizeLimit) {
        break;
      } else {
        totalSize += tmpSize;
      }
    }
    List<Message> subList = messages.subList(startIndex, nextIndex);
    currIndex = nextIndex;
    return subList;
  }

  private int getStartIndex() {
    Message currMessage = messages.get(currIndex);
    int tmpSize = calcMessageSize(currMessage);
    while (tmpSize > sizeLimit) {
      currIndex += 1;
      Message message = messages.get(currIndex);
      tmpSize = calcMessageSize(message);
    }
    return currIndex;
  }

  private int calcMessageSize(Message message) {
    int tmpSize = message.getTopic().length() + message.getBody().length;
    Map<String, String> properties = message.getProperties();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      tmpSize += entry.getKey().length() + entry.getValue().length();
    }
    tmpSize = tmpSize + 20; // 增加⽇日志的开销20字节
    return tmpSize;
  }
}