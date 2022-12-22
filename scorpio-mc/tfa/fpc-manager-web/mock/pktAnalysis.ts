import { Request, Response } from 'express';
import { v4 as uuidv4 } from 'uuid';

let data = [
  {
    id: 'dasdasawesdfd_s2lals',
    fileName: 'Plugin-1',
    protocol: 'XTest',
    description: '自定义XText',
    createTime: '2022-02-28 12:01:02',
    parseStatus: 'unresolved',
    parseLog: '',
  },
  {
    id: 'dasdasaw22esdfd_s2lals',
    fileName: 'Plugin-2',
    protocol: 'YTest',
    description: '自定义YText',
    createTime: '2022-02-28 12:01:02',
    parseStatus: 'success',
    parseLog: '',
  },
  {
    id: 'dasdasaw22esdfd_s222lals',
    fileName: 'Plugin_3',
    protocol: 'ZTest',
    description: '自定义ZText',
    createTime: '2022-02-28 12:01:02',
    parseStatus: 'error',
    parseLog: 'LOG:错误原因',
  },
];

let pluginCnt = 4;

const getPktAnalysisPlugins = (req: Request, res: Response) => {
  res.json({
    content: data,
    number: 0,
    size: 20,
    totalElements: 3,
    totalPages: 1,
  });
};

const createPktAnalysisPlugin = (req: Request, res: Response) => {
  const { protocol, description } = req.body;
  const id = uuidv4();
  data.push({
    id,
    fileName: `Plugin-${pluginCnt++}`,
    protocol,
    description,
    createTime: '2022-02-28 12:01:02',
    parseStatus: 'unresolved',
    parseLog: '',
  });
  // 模拟10s之后解析结果
  setTimeout(() => {
    const index = data.findIndex((item) => item.id === id);
    data[index] = {
      ...data[index],
      parseStatus: pluginCnt % 2 === 0 ? 'success' : 'error',
    };
  }, 10000);
  res.json({});
};

const deletePktAnalysisPlugin = (req: Request, res: Response) => {
  console.log(req.body, req.params);
  const { id } = req.params;
  if (req.body._method === 'DELETE') {
    // 删除数据
    data = data.filter((item) => item.id !== id);
    res.json({});
  }
};

const downLoadPlugin = (req: Request, res: Response) => {
  res.json({ url: 'http://www.lua.org/ftp/lua-5.4.4.tar.gz' });
};

const previewPlugin = (req: Request, res: Response) => {
  res.json({
    code: `-- Spark dissector Lua plugin for Wireshark
    -- Built for TU Dresden, RNP 2017 Exercise 2
    -- Copyright 2017 Dominik Pataky <dominik.pataky@tu-dresden.de>
    -- Licensed under GPLv3, see LICENSE
    
    -- to be run with "wireshark -X lua_script:spark_dissector.lua"
    
    -- create protocol and its fields
    local NAME = "IOSP"
    local PORT_SDK = 1100
    local PORT_GATEWAY = 1101
    p_iosp = Proto(NAME, "IOSP Protocol")
    
    local iosp_msg = ProtoField.none("iosp.msg", "iosp-message-header")
    local iosp_msg_srcid = ProtoField.uint32("iosp.msg.srcid", "msg.srcid", base.HEX)
    local iosp_msg_dstid = ProtoField.uint32("iosp.msg.dstid", "msg.dstid", base.HEX)
    local iosp_msg_msglen = ProtoField.uint32("iosp.msg.msglen", "msg.msglen", base.DEC)
    local iosp_msg_subnum = ProtoField.uint16("iosp.msg.subnum", "msg.subnum", base.DEC)
    local iosp_msg_more = ProtoField.uint16("iosp.msg.more", "msg.more", base.HEX)
    local iosp_msg_seqnum = ProtoField.uint64("iosp.msg.seqnum", "msg.seqnum", base.HEX)
    
    local iosp_avps = ProtoField.none("iosp.avp", "iosp-avps")
    local iosp_avp = ProtoField.none("iosp.avp", "iosp-avp")
    local iosp_avp_type = ProtoField.uint32("iosp.avp.type", "avp.type", base.DEC)
    local iosp_avp_len = ProtoField.uint32("iosp.avp.len", "avp.len", base.DEC)
    local iosp_avp_data = ProtoField.string("iosp.avp.data", "avp.data")
    
    p_iosp.fields = {iosp_msg, iosp_msg_srcid, iosp_msg_dstid, iosp_msg_msglen, iosp_msg_subnum, iosp_msg_more, iosp_msg_seqnum, iosp_avps, iosp_avp, iosp_avp_type, iosp_avp_len, iosp_avp_data }
    -- TLV 格式数据  不同  T 的含义
    local attrType = {
     [1]  =  "	AVP_TYPE_FILE_CONTENT_REQ ",
     [2]  =  "	AVP_TYPE_FILE_CONTENT_RES ",
     [3]  =  "	AVP_TYPE_FILE_METADATA_REQ ",
     [4]  =  "	AVP_TYPE_FILE_METADATA_RES ",
     [5]  =  "	AVP_TYPE_FILE_WRITE_REQ ",
     [6]  =  "	AVP_TYPE_FILE_WRITE_RES ",
     [7]  =  "	AVP_TYPE_FILE_ALL_INFO_REQ ",
     [8]  =  "	AVP_TYPE_FILE_ALL_INFO_RES ",
     [9]  =  "	AVP_TYPE_FILE_QUERY_META_REQ ",
     [10] =   "	AVP_TYPE_FILE_QUERY_META_RES ",
     [11] =   "	AVP_TYPE_FILE_QUERY_ALL_INFO_REQ ",
     [12] =   "	AVP_TYPE_FILE_QUERY_ALL_INFO_RES ",
     [13] =   "	AVP_TYPE_ERROR_RES ",
     [14] =   "	AVP_TYPE_FAULT_RES ",
     [15] =   "	AVP_TYPE_FILE_QUERY_ANALYS_RPT_REQ ",
     [16] =   "	AVP_TYPE_FILE_QUERY_ANALYS_RPT_RES ",
     [17] =   "	AVP_TYPE_CLIENT_AUTH_REQ ",
     [18] =   "	AVP_TYPE_CLIENT_AUTH_RES ",
     [19] =   "	AVP_TYPE_COND_IDX_REQ ",
     [20] =   "	AVP_TYPE_COND_IDX_RES ",
     [21] =   "	AVP_TYPE_OBJ_LABEL_UPDATE_REQ ",
     [22] =   "	AVP_TYPE_OBJ_LABEL_UPDATE_RES ",
     [23] =   "	AVP_TYPE_SUBSCRIBE_META_REQ ",
     [24] =   "	AVP_TYPE_SUBSCRIBE_META_RES ",
     [25] =   "	AVP_TYPE_SUBSCRIBE_ALL_INFO_REQ ",
     [26] =   "	AVP_TYPE_SUBSCRIBE_ALL_INFO_RES ",
     [27] =   "	AVP_TYPE_SUBSCRIBE_CANCEL ",
     [28] =   "	AVP_TYPE_SUBSCRIBE_CANCEL_RES ",
     [29] =   "	AVP_TYPE_SUBSCRIBE_FINISH ",
     [30] =   "	AVP_TYPE_SUBSCRIBE_WAIT ",
     [31] =   "	AVP_TYPE_SUBSCRIBE_MULTI_CONN ",
     [32] =   "	AVP_TYPE_SUBSCRIBE_MAX_LIMIT ",
     [33] =   "	AVP_TYPE_FILE_SLICE_WRITE_MSG ",
     [34] =   "	AVP_TYPE_BIG_FILE_READ_REQ ",
     [35] =   "	AVP_TYPE_BIG_FILE_READ_RES ",
     [36] =   "	AVP_TYPE_FILE_WRITE_HEALCHK_REQ ",
     [37] =   "	AVP_TYPE_FILE_WRITE_HEALCHK_RES ",
     [38] =   "	AVP_TYPE_FILE_ALL_INFO_HEALCHK_REQ ",
     [39] =   "	AVP_TYPE_FILE_ALL_INFO_HEALCHK_RES "
    }
    local msgHdr_len = 24
    local avpHdr_len = 8
    -- dissector function
    function p_iosp.dissector(tvb, pinfo, tree)
      -- validate packet length is adequate, otherwise quit
      local pkt_len = tvb:len()
      if pkt_len < msgHdr_len then return end
      
      pinfo.cols.protocol = p_iosp.name
      
      -- 如果不为0、1、2，则表示是data 
      local srcid=tvb:range(0,4):uint()
      if (srcid ~= 0) and (srcid ~= 1) and (srcid ~=2) then
      tree:add(iosp_avp_data, tvb:range(0,pkt_len))
      return
      end
      
      local dstid=tvb:range(4,4):uint()
      if (dstid ~= 0) and (dstid ~= 1) and (dstid ~=2) then
      tree:add(iosp_avp_data, tvb:range(0,pkt_len))
      return
      end
      
      local msglen=tvb:range(8,4):uint()
      if msglen == 0 then
      tree:add(iosp_avp_data, tvb:range(0,pkt_len))
      return
      end
      
      -- create subtree iosp-msg
      local portal_tree = tree:add(iosp_msg, tvb:range(0,msgHdr_len))
      local offset=0
      portal_tree:add(iosp_msg_srcid, tvb:range(offset,4))
      offset = offset + 4
      
      portal_tree:add(iosp_msg_dstid, tvb:range(offset,4))
      offset = offset + 4
      
      portal_tree:add(iosp_msg_msglen, tvb:range(offset,4))
      offset = offset + 4
      
      portal_tree:add(iosp_msg_subnum, tvb:range(offset,2))
      local attrnums = tvb:range(offset,2):uint()
      offset = offset + 2
      
      portal_tree:add(iosp_msg_more, tvb:range(offset,2))
      offset = offset + 2
      
      portal_tree:add(iosp_msg_seqnum, tvb:range(offset,8))
      offset = offset + 8
      
      pkt_len = pkt_len - msgHdr_len
      local attrs_tree = tree:add(iosp_avps, tvb:range(offset,pktlen))
        for i = 1,attrnums do
          if pkt_len < avpHdr_len then return end
        
            local tlv_tree = attrs_tree:add(iosp_avp, tvb:range(offset,avpHdr_len))
            tlv_tree:append_text("("..tostring(i)..")")                    -- 设置第N个属性
            local tlv_type_tree = tlv_tree:add(iosp_avp_type,tvb:range(offset,4))
            local tlv_type_v = tvb:range(offset,4):uint()         -- 获取type的值
            tlv_type_tree:append_text(attrType[tlv_type_v])        -- 设置type的含义
            offset = offset + 4
            tlv_tree:add(iosp_avp_len,tvb:range(offset,4))
            value_len = tvb:range(offset,4):uint()
            offset = offset + 4
        
        pkt_len = pkt_len - avpHdr_len
        
        -- 被分片了
        if value_len > pkt_len then
            tlv_tree:add(iosp_avp_data, tvb:range(offset,pkt_len))
              return
            end
        
            tlv_tree:add(iosp_avp_data,tvb:range(offset,value_len))
            offset = offset + value_len
        
        pkt_len = pkt_len - value_len
        end
    
    end
    
    -- Initialization routine
    function p_iosp.init()
    end
    
    
    local tcp_table = DissectorTable.get("tcp.port")
    if tcp_table ~= nil then
         tcp_table:add(PORT_SDK, p_iosp)
       tcp_table:add(PORT_GATEWAY, p_iosp)
    end
    `,
  });
};
export default {
  // 获取所有插件
  'GET /api/webapi/fpc-v1/appliance/pktanalysis/plugins': getPktAnalysisPlugins,
  // 新建插件
  'POST /api/webapi/fpc-v1/appliance/pktanalysis/plugins': createPktAnalysisPlugin,
  // 删除插件
  'POST /api/webapi/fpc-v1/appliance/pktanalysis/plugins/:id': deletePktAnalysisPlugin,
  // 下载插件
  'GET /api/webapi/fpc-v1/appliance/pktanalysis/plugins/:id/file-download': downLoadPlugin,
  /** 预览 */
  'GET /api/webapi/fpc-v1/appliance/pktanalysis/plugins/:id/file-preview': previewPlugin,
};
