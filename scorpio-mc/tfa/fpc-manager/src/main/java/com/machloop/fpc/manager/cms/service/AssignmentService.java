package com.machloop.fpc.manager.cms.service;

import java.util.List;

import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;

/**
 * @author liyongjun
 *
 * create at 2019年12月16日, fpc-manager
 */
public interface AssignmentService {

  List<AssignResult> assignTask(AssignReply assignReply);
}
