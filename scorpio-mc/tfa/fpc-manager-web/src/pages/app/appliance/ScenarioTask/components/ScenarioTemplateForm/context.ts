export const commonFilterFields = [
  {
    title: '源IPv4',
    dataIndex: 'src_ipv4',
    type: 'IPv4',
    operandType: 'IPv4',
    enumValue: [],
  },
  {
    title: '源IPv6',
    dataIndex: 'src_ipv6',
    type: 'IPv6',
    operandType: 'IPv6',
    enumValue: [],
  },
  {
    title: '源端口',
    dataIndex: 'src_port',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '目的IPv4',
    dataIndex: 'dest_ipv4',
    type: 'IPv4',
    operandType: 'IPv4',
    enumValue: [],
  },
  {
    title: '目的IPv6',
    dataIndex: 'dest_ipv6',
    type: 'IPv6',
    operandType: 'IPv6',
    enumValue: [],
  },
  {
    title: '目的端口',
    dataIndex: 'dest_port',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '策略名称',
    dataIndex: 'policy_name',
    enumValue: [],
  },
];

export const networkMetadataFilterField = [
  {
    title: 'URL',
    dataIndex: 'uri',
    enumValue: [],
  },
  {
    title: '请求方法',
    dataIndex: 'method',
    enumValue: [],
  },
  {
    title: 'host',
    dataIndex: 'host',
    enumValue: [],
  },
  {
    title: 'xff',
    dataIndex: 'xff',
    enumValue: [],
  },
  {
    title: '文件传输方式',
    dataIndex: 'file_flag',
    operandType: 'enum',
    enumValue: [
      {
        text: '非文件传输',
        value: '0',
      },
      {
        text: '上传',
        value: '1',
      },
      {
        text: '下载',
        value: '2',
      },
    ],
  },
  {
    title: '传输文件名称',
    dataIndex: 'file_name',
    enumValue: [],
  },
  {
    title: '传输文件类型',
    dataIndex: 'file_type',
    enumValue: [],
  },
];

export const dnsFilterField = [
  {
    title: 'DNS协议返回码',
    dataIndex: 'dns_rcode',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: 'DNS协议返回码名称',
    dataIndex: 'dns_rcode_name',
    enumValue: [],
  },
  {
    title: '域名',
    dataIndex: 'domain',
    enumValue: [],
  },
  {
    title: '域名解析地址IPv4',
    dataIndex: 'domain_ipv4',
    type: 'Array<IPv4>',
    operandType: 'IPv4',
    enumValue: [],
  },
  {
    title: '域名解析地址IPv6',
    dataIndex: 'domain_ipv6',
    type: 'Array<IPv6>',
    operandType: 'IPv6',
    enumValue: [],
  },
];

export const ftpFilterField = [
  {
    title: '登录用户',
    dataIndex: 'user',
    enumValue: [],
  },
];

export const mailFilterField = [
  {
    title: '邮件主题',
    dataIndex: 'subject',
    enumValue: [],
  },
  {
    title: '发件人',
    dataIndex: 'from',
    enumValue: [],
  },
  {
    title: '收件人',
    dataIndex: 'to',
    enumValue: [],
  },
  {
    title: '抄送',
    dataIndex: 'cc',
    enumValue: [],
  },
  {
    title: '密送',
    dataIndex: 'bcc',
    enumValue: [],
  },
  {
    title: '附件名称',
    dataIndex: 'attachment',
    enumValue: [],
  },
];

export const telnetFilterField = [
  {
    title: '登录用户',
    dataIndex: 'username',
    enumValue: [],
  },
  {
    title: '操作命令',
    dataIndex: 'cmd',
    enumValue: [],
  },
];

export const sslFilterField = [
  {
    title: '服务器名称',
    dataIndex: 'server_name',
    enumValue: [],
  },
  {
    title: '客户端指纹',
    dataIndex: 'ja3_client',
    enumValue: [],
  },
  {
    title: '服务端指纹',
    dataIndex: 'ja3_server',
    enumValue: [],
  },
  {
    title: 'SSL版本',
    dataIndex: 'version',
    enumValue: [],
  },
  {
    title: '证书发布者',
    dataIndex: 'issuer',
    enumValue: [],
  },
  {
    title: '证书使用者',
    dataIndex: 'common_name',
    enumValue: [],
  },
];

export const recordFilterField = [
  {
    title: '持续时间(ms)',
    dataIndex: 'duration',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '接口名称',
    dataIndex: 'interface',
    enumValue: [],
  },
  {
    title: '源IPv4',
    dataIndex: 'ipv4_initiator',
    type: 'IPv4',
    operandType: 'IPv4',
    enumValue: [],
  },
  {
    title: '源IPv6',
    dataIndex: 'ipv6_initiator',
    type: 'IPv6',
    operandType: 'IPv6',
    enumValue: [],
  },
  {
    title: '源端口',
    dataIndex: 'port_initiator',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '目的IPv4',
    dataIndex: 'ipv4_responder',
    type: 'IPv4',
    operandType: 'IPv4',
    enumValue: [],
  },
  {
    title: '目的IPv6',
    dataIndex: 'ipv6_responder',
    type: 'IPv6',
    operandType: 'IPv6',
    enumValue: [],
  },
  {
    title: '目的端口',
    dataIndex: 'port_responder',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '源MAC',
    dataIndex: 'ethernet_initiator',
    enumValue: [],
  },
  {
    title: '目的MAC',
    dataIndex: 'ethernet_responder',
    enumValue: [],
  },
  {
    title: '正向字节数',
    dataIndex: 'upstream_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '反向字节数',
    dataIndex: 'downstream_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '总字节数',
    dataIndex: 'total_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '正向包数',
    dataIndex: 'upstream_packets',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '反向包数',
    dataIndex: 'downstream_packets',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '总包数',
    dataIndex: 'total_packets',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '正向payload字节数',
    dataIndex: 'upstream_payload_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '反向payload字节数',
    dataIndex: 'downstream_payload_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: 'payload总字节数',
    dataIndex: 'total_payload_bytes',
    operandType: 'number',
    enumValue: [],
  },
  {
    title: '网络层协议',
    dataIndex: 'ethernet_protocol',
    operandType: 'enum',
    enumValue: [
      {
        text: 'IPv4',
        value: 'IPv4',
      },
      {
        text: 'IPv6',
        value: 'IPv6',
      },
      {
        text: 'OTHER',
        value: 'OTHER',
      },
    ],
  },
  {
    title: '传输层协议',
    dataIndex: 'ip_protocol',
    operandType: 'enum',
    enumValue: [
      {
        text: 'ICMP',
        value: 'icmp',
      },
      {
        text: 'IGMP',
        value: 'igmp',
      },
      {
        text: 'GGP',
        value: 'ggp',
      },
      {
        text: 'IPIP',
        value: 'ipip',
      },
      {
        text: 'STREAM',
        value: 'stream',
      },
      {
        text: 'TCP',
        value: 'tcp',
      },
      {
        text: 'CBT',
        value: 'cbt',
      },
      {
        text: 'EGP',
        value: 'egp',
      },
      {
        text: 'IGP',
        value: 'igp',
      },
      {
        text: 'BBN_RCC',
        value: 'bbn_rcc',
      },
      {
        text: 'NVPII',
        value: 'nvpii',
      },
      {
        text: 'PUP',
        value: 'pup',
      },
      {
        text: 'ARGUS',
        value: 'argus',
      },
      {
        text: 'EMCON',
        value: 'emcon',
      },
      {
        text: 'XNET',
        value: 'xnet',
      },
      {
        text: 'CHAOS',
        value: 'chaos',
      },
      {
        text: 'UDP',
        value: 'udp',
      },
      {
        text: 'MULTIPLEXING',
        value: 'multiplexing',
      },
      {
        text: 'DCNMEAS',
        value: 'dcnmeas',
      },
      {
        text: 'HMP',
        value: 'hmp',
      },
      {
        text: 'PRM',
        value: 'prm',
      },
      {
        text: 'IDP',
        value: 'idp',
      },
      {
        text: 'RDP',
        value: 'rdp',
      },
      {
        text: 'IRT',
        value: 'irt',
      },
      {
        text: 'TP',
        value: 'tp',
      },
      {
        text: 'BULK',
        value: 'bulk',
      },
      {
        text: 'MFE-NSP',
        value: 'mfe-nsp',
      },
      {
        text: 'MERIT',
        value: 'merit',
      },
      {
        text: 'DCCP',
        value: 'dccp',
      },
      {
        text: '3PC',
        value: '3pc',
      },
      {
        text: 'IDPR',
        value: 'idpr',
      },
      {
        text: 'XTP',
        value: 'xtp',
      },
      {
        text: 'DDP',
        value: 'ddp',
      },
      {
        text: 'CMTP',
        value: 'cmtp',
      },
      {
        text: 'TPPP',
        value: 'tppp',
      },
      {
        text: 'IL',
        value: 'il',
      },
      {
        text: 'SDRP',
        value: 'sdrp',
      },
      {
        text: 'IDRP',
        value: 'idrp',
      },
      {
        text: 'RSVP',
        value: 'rsvp',
      },
      {
        text: 'GRE',
        value: 'gre',
      },
      {
        text: 'DSR',
        value: 'dsr',
      },
      {
        text: 'BNA',
        value: 'bna',
      },
      {
        text: 'ESP',
        value: 'esp',
      },
      {
        text: 'AH',
        value: 'ah',
      },
      {
        text: 'I-NSLP',
        value: 'i-nslp',
      },
      {
        text: 'SWIPE',
        value: 'swipe',
      },
      {
        text: 'NARP',
        value: 'narp',
      },
      {
        text: 'MOBILE',
        value: 'mobile',
      },
      {
        text: 'TLSP',
        value: 'tlsp',
      },
      {
        text: 'ICMPV6',
        value: 'icmpv6',
      },
      {
        text: 'CFTP',
        value: 'cftp',
      },
      {
        text: 'SAT-EXPAK',
        value: 'sat-expak',
      },
      {
        text: 'KRYPTOLAN',
        value: 'kryptolan',
      },
      {
        text: 'RVD',
        value: 'rvd',
      },
      {
        text: 'IPPC',
        value: 'ippc',
      },
      {
        text: 'SAT-MON',
        value: 'sat-mon',
      },
      {
        text: 'VISA',
        value: 'visa',
      },
      {
        text: 'IPCV',
        value: 'ipcv',
      },
      {
        text: 'CPNX',
        value: 'cpnx',
      },
      {
        text: 'CPHB',
        value: 'cphb',
      },
      {
        text: 'WSN',
        value: 'wsn',
      },
      {
        text: 'PVP',
        value: 'pvp',
      },
      {
        text: 'BR-SAT-MON',
        value: 'br-sat-mon',
      },
      {
        text: 'SUN-ND',
        value: 'sun-nd',
      },
      {
        text: 'WB-MON',
        value: 'wb-mon',
      },
      {
        text: 'WB-EXPAK',
        value: 'wb-expak',
      },
      {
        text: 'ISO-IP',
        value: 'iso-ip',
      },
      {
        text: 'VMTP',
        value: 'vmtp',
      },
      {
        text: 'SVMTP',
        value: 'svmtp',
      },
      {
        text: 'VINES',
        value: 'vines',
      },
      {
        text: 'TTP',
        value: 'ttp',
      },
      {
        text: 'NSFNET-IGP',
        value: 'nsfnet-igp',
      },
      {
        text: 'DGP',
        value: 'dgp',
      },
      {
        text: 'TCF',
        value: 'tcf',
      },
      {
        text: 'EIGRP',
        value: 'eigrp',
      },
      {
        text: 'OSPF',
        value: 'ospf',
      },
      {
        text: 'SPRITE',
        value: 'sprite',
      },
      {
        text: 'LARP',
        value: 'larp',
      },
      {
        text: 'MTP',
        value: 'mtp',
      },
      {
        text: 'AX.25',
        value: 'ax.25',
      },
      {
        text: 'IPINIP',
        value: 'ipinip',
      },
      {
        text: 'MICP',
        value: 'micp',
      },
      {
        text: 'SCC-SP',
        value: 'scc-sp',
      },
      {
        text: 'ETHERIP',
        value: 'etherip',
      },
      {
        text: 'ENCAP',
        value: 'encap',
      },
      {
        text: 'GMTP',
        value: 'gmtp',
      },
      {
        text: 'IFMP',
        value: 'ifmp',
      },
      {
        text: 'PNNI',
        value: 'pnni',
      },
      {
        text: 'PIM',
        value: 'pim',
      },
      {
        text: 'ARIS',
        value: 'aris',
      },
      {
        text: 'SCPS',
        value: 'scps',
      },
      {
        text: 'QNX',
        value: 'qnx',
      },
      {
        text: 'A/N',
        value: 'a/n',
      },
      {
        text: 'IPCOMP',
        value: 'ipcomp',
      },
      {
        text: 'SNP',
        value: 'snp',
      },
      {
        text: 'COMPAQ',
        value: 'compaq',
      },
      {
        text: 'IPX',
        value: 'ipx',
      },
      {
        text: 'VRRP',
        value: 'vrrp',
      },
      {
        text: 'PGM',
        value: 'pgm',
      },
      {
        text: 'L2TP',
        value: 'l2tp',
      },
      {
        text: 'DDX',
        value: 'ddx',
      },
      {
        text: 'IATP',
        value: 'iatp',
      },
      {
        text: 'STP',
        value: 'stp',
      },
      {
        text: 'SRP',
        value: 'srp',
      },
      {
        text: 'UTI',
        value: 'uti',
      },
      {
        text: 'SMP',
        value: 'smp',
      },
      {
        text: 'SM',
        value: 'sm',
      },
      {
        text: 'PTP',
        value: 'ptp',
      },
      {
        text: 'ISIS',
        value: 'isis',
      },
      {
        text: 'FIRE',
        value: 'fire',
      },
      {
        text: 'CRTP',
        value: 'crtp',
      },
      {
        text: 'CRUDP',
        value: 'crudp',
      },
      {
        text: 'SSCOPMCE',
        value: 'sscopmce',
      },
      {
        text: 'IPLT',
        value: 'iplt',
      },
      {
        text: 'SPS',
        value: 'sps',
      },
      {
        text: 'PIPE',
        value: 'pipe',
      },
      {
        text: 'SCTP',
        value: 'sctp',
      },
      {
        text: 'FC',
        value: 'fc',
      },
      {
        text: 'RSVPE2EI',
        value: 'rsvpe2ei',
      },
      {
        text: 'MIPV6',
        value: 'mipv6',
      },
      {
        text: 'UDPLITE',
        value: 'udplite',
      },
      {
        text: 'MPLS-IN-IP',
        value: 'mpls-in-ip',
      },
      {
        text: 'MANET',
        value: 'manet',
      },
      {
        text: 'HIP',
        value: 'hip',
      },
      {
        text: 'SHIM6',
        value: 'shim6',
      },
      {
        text: 'WESP',
        value: 'wesp',
      },
      {
        text: 'ROHC',
        value: 'rohc',
      },
      {
        text: 'AX/4000',
        value: 'ax/4000',
      },
      {
        text: 'NCS_HEARBEAT',
        value: 'ncs_hearbeat',
      },
      {
        text: 'OTHER',
        value: 'other',
      },
    ],
  },
];

