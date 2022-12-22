package com.machloop.fpc.cms.center.broker.service.local;

import java.util.List;

import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;

/**
 * @author liyongjun
 *
 * create at 2019年12月16日, fpc-manager
 */
public interface ReceiveService {

  List<AssignResult> assignTask(AssignReply assignReply);
}
