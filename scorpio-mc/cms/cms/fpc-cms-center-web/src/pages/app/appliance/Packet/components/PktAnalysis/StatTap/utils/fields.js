export const convFields = {
  saddr: 'Address A|地址A',
  sport: 'Port A|端口A',
  daddr: 'Address B|地址B',
  dport: 'Port B|端口B',
  _packets: 'Packets|数据包',
  _bytes: 'Bytes|字节',
  txf: 'Packets A → B|数据包A → B',
  txb: 'Bytes A → B|字节A → B',
  rxf: 'Packets A ← B|数据包A ← B',
  rxb: 'Bytes A ← B|字节A ← B',
  start: 'Rel start|相对开始时间',
  _duration: 'Duration|持续时间',
  _rate_tx: 'bps A → B|带宽A → B',
  _rate_rx: 'bps A ← B|带宽A ← B',
};

export const hostFields = {
  host: 'Address|地址',
  port: 'Port|端口',
  _packets: 'Packets|数据包',
  _bytes: 'Bytes|字节',
  txf: 'TX Packets|发送数据包',
  txb: 'TX Bytes|发送字节数',
  rxf: 'RX Packets|接收数据包',
  rxb: 'RX Bytes|接收字节数',
};

export const hostFieldsGeo = {
  ...hostFields,
  geoip_country: 'GeoIP Country',
  geoip_city: 'GeoIP City',
  geoip_org: 'GeoIP ORG',
  geoip_isp: 'GeoIP ISP',
  geoip_as: 'GeoIP AS',
  geoip_lat: 'GeoIP Lat',
  geoip_lon: 'GeoIP Lon',
};

export const srtFields = {
  n: 'Procedure',
  num: 'Calls',
  _min: 'Min SRT [ms]',
  _max: 'Max SRT [ms]',
  _avg: 'Avg SRT [ms]',
};

export const rtdFields = {
  type: 'Type',
  num: 'Messages',
  _min: 'Min SRT [ms]',
  _max: 'Max SRT [ms]',
  _avg: 'AVG SRT [ms]',
  min_frame: 'Min in Frame',
  max_frame: 'Max in Frame',

  /* optional */
  open_req: 'Open Requests',
  disc_rsp: 'Discarded Responses',
  req_dup: 'Duplicated Requests',
  rsp_dup: 'Duplicated Responses',
};

export const statFields = {
  name: 'Topic / Item',
  count: 'Count',
  avg: 'Average',
  min: 'Min val',
  max: 'Max val',
  rate: 'Rate [ms]',
  perc: 'Percent',
  burstcount: 'Burst count',
  burstrate: 'Burst rate',
  bursttime: 'Burst start',
};

export const rtpStreamsFields = {
  saddr: 'Src addr|源地址',
  sport: 'Src port|源端口',
  daddr: 'Dst addr|目的地址',
  dport: 'Dst port|目的端口',
  _ssrc: 'SSRC',
  payload: 'Payload|载荷',
  pkts: 'Packets|数据包数量',
  _lost: 'Lost|丢弃',
  max_delta: 'Max Delta [ms]|最大Delta[ms]',
  max_jitter: 'Max Jitter [ms]|最大抖动[ms]',
  mean_jitter: 'Mean Jitter [ms]|平均抖动[ms]',
  _pb: 'Pb?',
};

export const expertFields = {
  f: 'No|序号',
  s: 'Severity|严重',
  g: 'Group|分组',
  p: 'Protocol|协议',
  m: 'Summary|概要',
};

export const exportObjectFields = {
  download: 'download|下载', // 下载
  // pkt: 'Packet number',
  hostname: 'Hostname|主机名',
  type: 'Content Type|内容类型',
  filename: 'Filename|文件名',
  len: 'Length|文档大小',
};
