/* eslint-disable no-underscore-dangle */
/* eslint-disable guard-for-in */
/* eslint-disable no-restricted-syntax */
// @ts-ignore
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { getDataZoom } from '@/components/ReactECharts';
import type { IPktAnalysisModelState } from '@/models/app/pktAnalysis';
import { getPageQuery } from '@/utils/utils';
import { DownloadOutlined } from '@ant-design/icons';
import { Button, Col, message, Modal, Row, Select, Spin } from 'antd';
import { connect } from 'dva';
import _ from 'lodash';
import React, { Fragment } from 'react';
import VirtualList from 'react-tiny-virtual-list';
import type { Dispatch } from 'redux';
import type { IPktAnalysisSharedProps } from '..';
import { ascii2Char, x2Hex } from '../Hexdump';
import type {
  IConvsTapData,
  IConvsTapResponseData,
  ICustomStatTapData,
  IExpertData,
  IExpertTapResponseData,
  IExportObjectData,
  IExportObjectTapResponseData,
  IFlowSeqData,
  IFlowSeqTapResponseData,
  IFollowData,
  IFollowPayloadData,
  INstatFieldData,
  INstatTableData,
  INstatTapResponseData,
  IRtdStatData,
  IRtdTapResponseData,
  IRtpStreamData,
  IRtpStreamTapResponseData,
  ISrcTapResponseData,
  ISrcTapTableData,
  IStatsTapResponseData,
  ITapResponseData,
  SrcTapTableRowData,
  StatsTapRowData,
} from '../typings';
import type { ISerieItemData } from './components/TapGraph';
import TapGraph from './components/TapGraph';
import TapTable from './components/TapTable';
import TransCode from './components/TransCode';
import styles from './index.less';
import { base64Code } from './utils/base64CodeTrans';
import {
  convFields,
  expertFields,
  exportObjectFields,
  hostFields,
  hostFieldsGeo,
  rtdFields,
  rtpStreamsFields,
  srtFields,
  statFields,
} from './utils/fields';

interface StatTapProps extends IPktAnalysisSharedProps {
  dispatch: Dispatch<any>;
  pktAnalysisModel: IPktAnalysisModelState;
  loading: boolean;
  [propName: string]: any;
}

enum EDataBase {
  'ASCII' = 'ASCII',
  'UTF-8' = 'UTF-8',
  'UTF-16' = 'UTF-16',
  'UTF-32' = 'UTF-32',
  'UTF-16BE' = 'UTF-16BE',
  'URLDECODE' = 'URLDECODE',
  'HEX' = 'HEX',
  'GB2312' = 'GB2312',
  'BASE64' = 'BASE64',
}

interface StatTapStates {
  tableData: any[];
  dataBase: EDataBase;
  followData: any;
  followLoading: any;
}

const schemeCategory10Color = [
  '#1f77b4',
  '#ff7f0e',
  '#2ca02c',
  '#d62728',
  '#9467bd',
  '#8c564b',
  '#e377c2',
  '#7f7f7f',
  '#bcbd22',
  '#17becf',
];
const schemeCategory20Color = [
  '#1f77b4',
  '#aec7e8',
  '#ff7f0e',
  '#ffbb78',
  '#2ca02c',
  '#98df8a',
  '#d62728',
  '#ff9896',
  '#9467bd',
  '#c5b0d5',
  '#8c564b',
  '#c49c94',
  '#e377c2',
  '#f7b6d2',
  '#7f7f7f',
  '#c7c7c7',
  '#bcbd22',
  '#dbdb8d',
  '#17becf',
  '#9edae5',
];

/**
 * 处理数字，保留指定位数的小数
 * @param number 数字
 * @param decimal 保留几位小数
 */
const fixdNumber = (number: number, decimal: number = 2) => {
  if (number === 0) return number;
  return number.toFixed(decimal);
};

class StatTap extends React.PureComponent<StatTapProps, StatTapStates> {
  constructor(props: StatTapProps) {
    super(props);

    this.state = {
      tableData: [],
      followData: [],
      followLoading: false,
      dataBase: EDataBase.ASCII,
    };
  }

  handleCloseModal = () => {
    this.updateModalVisible(false);
  };

  afterModalClose = () => {
    this.updateCurrentTap(undefined);
  };

  updateCurrentTap = (currentTap: ICustomStatTapData | undefined) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'pktAnalysisModel/changeModalVisible',
      payload: {
        currentTap,
      },
    });
  };

  updateModalVisible = (visible: boolean) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'pktAnalysisModel/changeModalVisible',
      payload: {
        visible,
      },
    });
  };

  renderTaps = (taps: any[]) => {
    return taps.map((tap) => {
      const { type } = tap;
      if (type === 'stats') {
        return this.renderStatTap(tap);
      }
      if (type === 'conv') {
        return this.renderConvTap(tap);
      }
      if (type === 'host') {
        return this.renderHostTap(tap);
      }
      if (type === 'srt') {
        return this.renderSrtTap(tap);
      }
      if (type === 'rtd') {
        return this.renderRtdTap(tap);
      }
      if (type === 'nstat') {
        return this.renderNstatTap(tap);
      }
      if (type === 'rtp-streams') {
        return this.renderRtpStreamTap(tap);
      }
      if (type === 'flow') {
        return this.renderFlowSeqTap(tap);
      }
      // 专家信息
      if (type === 'expert') {
        return this.renderExpertTap(tap);
      }
      if (type === 'eo') {
        return this.renderExportObjectTap(tap);
      }

      return 'render';
    });
  };

  // stats 类型
  renderStatTap = (tap: IStatsTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const result: any[] = [];

    const flattenStat = (stats: StatsTapRowData[], level: number = 0) => {
      for (let i = 0; i < stats.length; i += 1) {
        const statItem = stats[i];
        statItem.level = level;

        for (const col in statFields) {
          let value = statItem[col];
          if (value === undefined) {
            value = '-';
          } else if (col === 'perc') {
            // 百分比添加 %
            value += '%';
          }
          statItem[col] = value;
        }
        result.push(statItem);
        if (statItem.sub) {
          flattenStat(statItem.sub, level + 1);
        }
      }
    };

    // 表格
    // @ts-ignore
    const _data = _.cloneDeep(tap.stats);
    flattenStat(_data);

    // 图表只取 Total Packets
    const tgStat = tap.stats[0];
    const seriesData = [];
    const categories = [];
    if (tgStat && tgStat.sub) {
      for (let i = 0; i < tgStat.sub.length; i += 1) {
        const item = tgStat.sub[i];
        categories.push(item.name);
        seriesData.push({
          name: item.name,
          value: item.count,
          perc: item.perc,
        });
      }
    }

    const chartOption: ECOption = {
      title: {
        text: tgStat.name,
        left: 'center',
      },
      xAxis: {
        type: 'category',
        data: categories,
      },
      yAxis: {
        minInterval: 1, // 不显示小数
      },
      grid: { top: 40 },
      dataZoom: getDataZoom(categories.length),
      tooltip: {
        formatter(params: any) {
          const point = params[0];
          return `<b>${point.name}</b><br/>${point.seriesName}: ${point.value}（${point.data.perc}%）`;
        },
      },
      legend: {
        show: false,
      },
      series: [
        {
          type: 'bar',
          name: '数量',
          barMaxWidth: 20,
          data: seriesData,
        },
      ],
    };

    return (
      <Fragment>
        <ReactECharts option={chartOption} opts={{ height: 300 }} />
        <TapTable title={`${currentTap.name_zh}`} fields={statFields} data={result} />
      </Fragment>
    );
  };

  /**
   * Conversations
   */
  renderConvTap = (tap: IConvsTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const _data = _.cloneDeep(tap.convs);

    // 图表统计信息
    const chartCategories = [];
    const rxFramesCount = [];
    const txFramesCount = [];
    const rxBytesCount = [];
    const txBytesCount = [];

    for (let i = 0; i < _data.length; i += 1) {
      const conv: IConvsTapData = _data[i];
      if (conv.sport) {
        conv._sname = `${conv.saddr}:${conv.sport}`;
        conv._dname = `${conv.daddr}:${conv.dport}`;
      } else {
        conv._sname = conv.saddr;
        conv._dname = conv.daddr;
      }

      conv._name = `${conv._sname} <===>${conv._dname}`;

      conv._packets = conv.rxf + conv.txf;
      conv._bytes = conv.rxb + conv.txb;
      const _duration = conv.stop - conv.start;
      conv._duration = fixdNumber(_duration);
      conv._rate_tx = fixdNumber((8 * conv.txb) / _duration);
      conv._rate_rx = fixdNumber((8 * conv.rxb) / _duration);
      conv._filter = conv.filter;

      // 图表参数
      chartCategories.push(conv._name);
      rxFramesCount.push(conv.rxf);
      txFramesCount.push(conv.txf);

      rxBytesCount.push(conv.rxb);
      txBytesCount.push(conv.txb);
    }

    const graphFramesData: ISerieItemData[] = [
      {
        name: 'RX frames',
        type: 'bar',
        data: rxFramesCount,
      },
      {
        name: 'TX frames',
        type: 'bar',
        data: txFramesCount,
      },
    ];
    const graphBytesData: ISerieItemData[] = [
      {
        name: 'RX bytes',
        type: 'bar',
        data: rxBytesCount,
      },
      {
        name: 'TX bytes',
        type: 'bar',
        data: txBytesCount,
      },
    ];

    const colSpan = chartCategories.length >= 8 ? 24 : 12;

    // 根据实际情况，删除表格所需展示的列
    const _convFields = { ...convFields };
    if (
      currentTap.tap === 'conv:Ethernet' ||
      currentTap.tap === 'conv:IEEE 802.11' ||
      currentTap.tap === 'conv:IPv4' ||
      currentTap.tap === 'conv:IPv6'
    ) {
      // @ts-ignore
      delete _convFields.sport;
      // @ts-ignore
      delete _convFields.dport;
    }

    return (
      <Fragment>
        <Row gutter={10}>
          <Col span={colSpan}>
            <TapGraph
              title={`${currentTap.name_zh} - 数据包数`}
              categories={chartCategories}
              data={graphFramesData}
            />
          </Col>
          <Col span={colSpan}>
            <TapGraph
              type="bytes"
              title={`${currentTap.name_zh} - 字节数`}
              categories={chartCategories}
              data={graphBytesData}
            />
          </Col>
        </Row>
        <TapTable title={currentTap.name_zh} fields={_convFields} data={_data} />
      </Fragment>
    );
  };

  /**
   * Host
   */
  renderHostTap = (tap: ITapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const _data = _.cloneDeep(tap.hosts);
    // 图表统计信息
    const chartCategories = [];
    const rxFramesCount = [];
    const txFramesCount = [];
    const rxBytesCount = [];
    const txBytesCount = [];

    for (let i = 0; i < _data.length; i += 1) {
      const host = _data[i];
      if (host.port) {
        host._name = `${host.host}:${host.port}`;
      } else {
        host._name = host.host;
      }
      host.key = host._name;
      host._packets = host.rxf + host.txf;
      host._bytes = host.rxb + host.txb;
      host._filter = host.filter;

      // 图表参数
      chartCategories.push(host._name);
      rxFramesCount.push(host.rxf);
      txFramesCount.push(host.txf);

      rxBytesCount.push(host.rxb);
      txBytesCount.push(host.txb);
    }

    const graphFramesData: ISerieItemData[] = [
      {
        name: 'RX frames',
        type: 'bar',
        data: rxFramesCount,
      },
      {
        name: 'TX frames',
        type: 'bar',
        data: txFramesCount,
      },
    ];
    const graphBytesData: ISerieItemData[] = [
      {
        name: 'RX bytes',
        type: 'bar',
        data: rxBytesCount,
      },
      {
        name: 'TX bytes',
        type: 'bar',
        data: txBytesCount,
      },
    ];

    const colSpan = chartCategories.length >= 8 ? 24 : 12;

    // 根据实际情况，删除表格所需展示的列
    const _hostFields = _data.geoip ? { ...hostFieldsGeo } : { ...hostFields };
    if (
      currentTap.tap === 'endpt:Ethernet' ||
      currentTap.tap === 'endpt:IEEE 802.11' ||
      currentTap.tap === 'endpt:IPv4' ||
      currentTap.tap === 'endpt:IPv6'
    ) {
      // @ts-ignore
      delete _hostFields.port;
    }

    return (
      <Fragment>
        <Row gutter={10}>
          <Col span={colSpan}>
            <TapGraph
              title={`${currentTap.name_zh} - 数据包数`}
              categories={chartCategories}
              data={graphFramesData}
            />
          </Col>
          <Col span={colSpan}>
            <TapGraph
              type="bytes"
              title={`${currentTap.name_zh} - 字节数`}
              categories={chartCategories}
              data={graphBytesData}
            />
          </Col>
        </Row>
        <TapTable title={currentTap.name_zh} fields={_hostFields} data={_data} />
      </Fragment>
    );
  };

  /**
   * Service Response Time
   */
  renderSrtTap = (tap: ISrcTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const srtTables: ISrcTapTableData[] = _.cloneDeep(tap.tables);
    const _talbedata: { title: string; data: any[] }[] = [];
    for (let i = 0; i < srtTables.length; i += 1) {
      const rows = srtTables[i].r;
      const filter = srtTables[i].f;

      const _data = [];

      for (let j = 0; j < rows.length; j += 1) {
        const row: SrcTapTableRowData = _.cloneDeep(rows[j]);
        row._min = fixdNumber(row.min * 1000.0);
        row._max = fixdNumber(row.max * 1000.0);
        row._avg = fixdNumber((row.tot / row.num) * 1000);

        if (filter) {
          row._filter = `${filter} == ${row.idx}`;
        }

        _data.push(row);
      }

      _talbedata.push({
        title: `(${tap.tap})${srtTables[i].n}`,
        data: _data,
      });
    }
    return (
      <Fragment>
        {_talbedata.map((table) => (
          <TapTable
            title={`${currentTap.name_zh}${table.title}`}
            fields={srtFields}
            data={table.data}
          />
        ))}
      </Fragment>
    );
  };

  /**
   * Response Time Delay
   */
  renderRtdTap = (tap: IRtdTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const rtdStats = _.cloneDeep(tap.stats);
    for (let i = 0; i < rtdStats.length; i += 1) {
      const row: IRtdStatData = rtdStats[i];

      row._min = fixdNumber(row.min * 1000.0);
      row._max = fixdNumber(row.max * 1000.0);
      row._avg = fixdNumber((row.tot / row.num) * 1000.0);
    }

    // TODO: open_req
    if (tap.open_req !== undefined) {
      // TODO:
    }

    return (
      <TapTable
        title={`${currentTap.name_zh}（${currentTap.tap}）`}
        fields={rtdFields}
        data={rtdStats}
      />
    );
  };

  /**
   * nstat
   */
  renderNstatTap = (tap: INstatTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const nstatFields: INstatFieldData[] = tap.fields;
    const nstatTables: INstatTableData[] = tap.tables;

    // 表格表头
    const fields = {};
    for (let i = 0; i < nstatFields.length; i += 1) {
      fields[`${i}`] = nstatFields[i].c;
    }
    // 内容
    return nstatTables.map((table) => (
      <TapTable title={`${currentTap.name_zh}（${table.t}）`} fields={fields} data={table.i} />
    ));
  };

  /**
   * RTP streams
   */
  renderRtpStreamTap = (tap: IRtpStreamTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;

    const streams = _.cloneDeep(tap.streams);
    for (let i = 0; i < streams.length; i += 1) {
      const stream: IRtpStreamData = streams[i];

      stream._ssrc = `0x${x2Hex(stream.ssrc, 0)}`;
      stream._pb = stream.problem ? 'X' : '';

      const lost = stream.expectednr - stream.totalnr;
      stream._lost = `${lost}(${100 * (lost / stream.expectednr)}%)`;

      let ipstr = 'ip';
      if (stream.ipver === 6) {
        ipstr = 'ipv6';
      }

      const rtp_str = `${stream.saddr}_${stream.sport}_${stream.daddr}_${stream.dport}_${x2Hex(
        stream.ssrc,
        0,
      )}`;

      stream._analyse = `rtp-analyse:${rtp_str}`;
      stream._download = `rtp:${rtp_str}`;
      stream._play = stream._download;
      stream._play_descr = `[${stream.saddr}]:${stream.sport} -> [${stream.daddr}]:${stream.dport} SSRC: ${stream._ssrc} ${stream.payload}`;

      stream._filter =
        `(${ipstr}.src == ${stream.saddr} && udp.srcport == ${stream.sport} && ${ipstr}.dst == ${stream.daddr} && udp.dstport == ${stream.dport} && ` +
        `rtp.ssrc == ${stream._ssrc})`;
    }

    return (
      <TapTable
        title={`${currentTap.name_zh}（${streams.length}）`}
        fields={rtpStreamsFields}
        data={streams}
      />
    );
  };

  renderFlowSeqTap = (tap: IFlowSeqTapResponseData) => {
    const { nodes, flows } = tap;

    if (nodes.length === 0 || flows.length === 0) {
      return <div>暂无数据</div>;
    }

    const svgWidth = Math.max(2000, 120 + nodes.length * 300);
    const listHeight = 'calc(100vh - 160px)';
    const colors = nodes.length <= 10 ? schemeCategory10Color : schemeCategory20Color;
    const basePosX = nodes.length > 5 ? 300 : 300 + 150 * (5 - nodes.length);

    return (
      <div className={styles.flowSeqWrap} style={{ height: 'calc(100vh - 180px)' }}>
        {/* 标题 */}
        <span>时间</span>
        <span style={{ marginLeft: 68 }}>时间差</span>
        {/* 节点 */}
        <div style={{ width: svgWidth, position: 'relative', marginTop: -16 }}>
          {nodes.map((node, index) => {
            const posX = 200 + basePosX * index;
            const endY = 30 + flows.length * 80;

            return (
              <section
                className={styles.node}
                key={node}
                style={{ position: 'absolute', left: posX }}
              >
                <span className={styles.nodeHost}>{node}</span>
                <span className={styles.nodeLine} style={{ height: endY - 20 }} />
              </section>
            );
          })}
          <div className={styles.innerWrap} style={{ position: 'relative', top: 30 }}>
            <VirtualList
              height={listHeight}
              itemCount={flows.length}
              itemSize={30}
              overscanCount={50}
              renderItem={({ index, style }) => {
                const flow: IFlowSeqData = flows[index];
                const nn = flow.n;
                const lineWidth = Math.abs(nn[1] - nn[0]) * basePosX;
                const lineLeft = Math.min(nn[0], nn[1]) * basePosX;
                // 计算时间查
                let diff: any = 0;
                if (index > 0) {
                  const prevFlow: IFlowSeqData = flows[index - 1];
                  diff = parseFloat((+flow?.t - +prevFlow?.t).toFixed(8));
                }

                return (
                  <section className={styles.seq} key={flow.t} style={{ ...style }}>
                    <span
                      className={styles.seqTime}
                      style={{ position: 'absolute', left: 0, bottom: 4 }}
                    >
                      {flow.t}
                    </span>
                    {/* 时间差 */}
                    {index > 0 && (
                      <span
                        className={styles.diff}
                        style={{ position: 'absolute', left: 90, bottom: 4 }}
                      >
                        {diff}
                      </span>
                    )}
                    <span
                      title={flow.c}
                      className={styles.seqLabel}
                      style={{
                        position: 'absolute',
                        left: lineLeft + 220,
                        width: lineWidth - 40,
                        textAlign: 'center',
                      }}
                    >
                      {flow.c}
                    </span>
                    <span
                      className={`${styles.seqLine} ${nn[1] > nn[0] ? styles.end : styles.start}`}
                      style={{
                        position: 'absolute',
                        bottom: 12,
                        width: lineWidth,
                        height: 1,
                        left: lineLeft + 200,
                        // fix: 节点超出20个后，会出现没有颜色的情况
                        // TODO: 以后可以提供很多个颜色
                        borderTop: `1px solid ${colors[nn[0]] || colors[0]}`,
                      }}
                    >
                      <span className={styles.arrow} />
                    </span>
                  </section>
                );
              }}
            />
          </div>
        </div>
      </div>
    );
  };

  renderExpertTap = (tap: IExpertTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;
    const details = _.cloneDeep(tap.details);

    for (let i = 0; i < details.length; i += 1) {
      const detail: IExpertData = details[i];
      if (detail.s) {
        detail.trClassName = `expertColor_${detail.s}`;
      }
    }

    return (
      <TapTable
        title={`${currentTap.name_zh}（${details.length}）`}
        fields={expertFields}
        data={details}
      />
    );
  };

  renderFollow = (followData: IFollowData & { defPayloads: any }) => {
    const serverToClient = `${followData.shost}:${followData.sport}-->${followData.chost}:${followData.cport}`;
    const clientToServer = `${followData.chost}:${followData.cport} --> ${followData.shost}:${followData.sport}`;

    // const serverToClientString = serverToClient + '(' + followData.sbytes + ' bytes';
    // const clientToServerString = clientToServer + '(' + followData.cbytes + ' bytes';

    if (followData.payloads) {
      const { payloads } = followData;
      const fullFollowList: {
        key: string;
        dom: React.ReactNode;
      }[] = [];
      if (this.state.dataBase === EDataBase.ASCII || this.state.dataBase === EDataBase['UTF-8']) {
        // 拼接在一起
        followData.defPayloads.map((payload: IFollowPayloadData) => {
          // 解码
          const decodeString = base64Code().decode(payload.d);
          // const decodeString = window.atob(payload.d);

          // 用于显示的文本
          let followText = '';
          if (this.state.dataBase === EDataBase.ASCII) {
            for (let index = 0; index < decodeString.length; index++) {
              followText += ascii2Char(decodeString[index].charCodeAt(0), true);
            }
          } else {
            followText = decodeString;
          }
          // TODO: 虚拟化处理，由于这里不是固定高度的，虚拟化处理并不好做
          const hasServer = payload.s !== undefined;

          fullFollowList.push({
            key: hasServer ? serverToClient : clientToServer,
            dom: (
              <span className={hasServer ? styles.followServer : styles.followClient}>
                {followText}
              </span>
            ),
          });
        });
      } else {
        payloads.map((payload: IFollowPayloadData) => {
          // 用于显示的文本
          const followText = payload.d;
          const hasServer = payload.s !== undefined;
          fullFollowList.push({
            key: hasServer ? serverToClient : clientToServer,
            dom: (
              <span className={hasServer ? styles.followServer : styles.followClient}>
                {followText}
              </span>
            ),
          });
        });
      }

      return (
        <pre className={`${styles.followContent}`}>{fullFollowList.map((row) => row.dom)}</pre>
      );
    }
    return <div>暂无数据</div>;
  };

  handleDownloadObject = (object: IExportObjectData) => {
    const { sourceType, taskId, dispatch } = this.props;

    if (!object) {
      message.error('下载失败');
      return;
    }

    const query = {
      type: 'download',
      parameter: JSON.stringify({
        req: 'download',
        token: object._download,
      }),
    };

    const pageParams = getPageQuery();
    if (pageParams.startTime) {
      // @ts-ignore
      query.startTime = pageParams.startTime;
    }
    if (pageParams.endTime) {
      // @ts-ignore
      query.endTime = pageParams.endTime;
    }

    dispatch({
      type: 'pktAnalysisModel/downloadFlowFile',
      payload: {
        sourceType,
        taskId,
        ...query,
      },
    });
  };

  renderExportObjectTap = (tap: IExportObjectTapResponseData) => {
    const {
      pktAnalysisModel: { currentTap },
    } = this.props;
    const objects = _.cloneDeep(tap.objects);

    for (let i = 0; i < objects.length; i += 1) {
      const detail: IExportObjectData = objects[i];
      // @ts-ignore
      detail.download = (
        <span className={styles.downloadIcon} onClick={() => this.handleDownloadObject(detail)}>
          <DownloadOutlined />
        </span>
      );
    }

    return (
      <TapTable
        title={`${currentTap.name_zh}（${objects.length}）`}
        fields={exportObjectFields}
        data={objects}
      />
    );
  };

  render() {
    const {
      pktAnalysisModel: { currentTap, taps, follow, statModalVisible },
      loading,
    } = this.props;

    const { followData, followLoading, dataBase } = this.state;

    return (
      <div className={styles.pageWrap}>
        {statModalVisible && (
          <div>
            <TransCode
              payloadParams={{ payloads: follow.payloads, type: dataBase }}
              onCodeTrans={(data: any) => {
                this.setState({ followData: data, followLoading: false });
              }}
              onLoading={(loadings: boolean) => {
                this.setState({ followLoading: loadings });
              }}
            />
            <Modal
              className={styles.fullscreenModal}
              title={currentTap && currentTap.name_zh}
              width="80%"
              // closable={false}
              destroyOnClose
              maskClosable={false}
              // keyboard={false}
              afterClose={this.afterModalClose}
              visible={statModalVisible}
              onCancel={this.handleCloseModal}
              bodyStyle={{ paddingTop: 10, paddingBottom: 10 }}
              footer={
                <div className={styles.modalFooter}>
                  {currentTap.follow && (
                    <div className={styles.baseSelect}>
                      <span>数据展示为</span>
                      <Select
                        style={{ width: 140 }}
                        value={this.state.dataBase}
                        onChange={(v) =>
                          this.setState({
                            dataBase: v,
                          })
                        }
                      >
                        {Object.keys(EDataBase).map((item) => {
                          return (
                            <Select.Option key={item} value={item}>
                              {item}
                            </Select.Option>
                          );
                        })}
                      </Select>
                    </div>
                  )}
                  <Button
                    key="close"
                    style={{ color: 'inherit' }}
                    type="link"
                    onClick={this.handleCloseModal}
                  >
                    关闭
                  </Button>
                </div>
              }
            >
              {loading || followLoading ? (
                <div className={styles.loading}>
                  <Spin />
                </div>
              ) : (
                [
                  currentTap.tap && this.renderTaps(taps),
                  currentTap.follow &&
                    this.renderFollow({
                      ...follow,
                      defPayloads: follow.payloads || [],
                      payloads: followData,
                    }),
                ]
              )}
            </Modal>
          </div>
        )}
      </div>
    );
  }
}

export default connect(
  ({
    pktAnalysisModel,
    loading,
  }: {
    pktAnalysisModel: IPktAnalysisModelState;
    loading: {
      effects: Record<string, boolean>;
    };
  }) => ({
    pktAnalysisModel,
    loading:
      loading.effects['pktAnalysisModel/queryStatTap'] ||
      loading.effects['pktAnalysisModel/queryStatFollow'] ||
      false,
  }),
)(StatTap);
