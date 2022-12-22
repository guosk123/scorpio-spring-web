package com.machloop.fpc.manager.metadata.dao;

import com.machloop.fpc.manager.metadata.data.ReceiverKafkaDO;

public interface ReceiverKafkaDao {

  ReceiverKafkaDO queryReceiverKafka();

  ReceiverKafkaDO saveOrUpdateReceiverKafka(ReceiverKafkaDO receiverKafkaDO);

}
