package com.machloop.fpc.cms.center.appliance.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.machloop.fpc.cms.center.appliance.vo.PacketAnalysisQueryVO;

/**
 * @author guosk
 *
 * create at 2021年6月7日, fpc-manager
 */
public interface PacketAnalysisService {

  Map<String, Object> queryFlowPackets(PacketAnalysisQueryVO queryVO, HttpServletRequest request);

  Map<String, Object> queryFlowPacketRefines(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request);

  String queryFlowPacketDownloadUrl(PacketAnalysisQueryVO queryVO, HttpServletRequest request);

  void analyzeFlowPacket(PacketAnalysisQueryVO queryVO, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response);

  Map<String, Object> stopSearchFlowPackets(String fpcSerialNumber, String queryId,
      HttpServletRequest request);

  Map<String, Object> stopFlowPacketRefines(String fpcSerialNumber, String queryId,
      HttpServletRequest request);

}
