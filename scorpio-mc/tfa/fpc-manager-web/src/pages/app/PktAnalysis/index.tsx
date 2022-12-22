/* eslint-disable @typescript-eslint/no-use-before-define */
import { ONE_KILO_1024 } from '@/common/dict';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import type { XLSXBookType } from '@/components/ExportXlsx';
import { exportXlsx } from '@/components/ExportXlsx';
import type { IPktAnalysisModelState } from '@/models/app/pktAnalysis';
import type { ConnectState } from '@/models/connect';
import { bytesToSize, parseArrayJson } from '@/utils/utils';
import { DownOutlined, LoadingOutlined, RollbackOutlined } from '@ant-design/icons';
import {
  Button,
  Card,
  Checkbox,
  Col,
  Dropdown,
  Menu,
  message,
  Row,
  Space,
  Spin,
  Tooltip,
} from 'antd';
import { connect } from 'dva';
import React, { useEffect, useMemo, useState } from 'react';
import SplitPane from 'react-split-pane';
import Pane from 'react-split-pane/lib/Pane';
import type { Dispatch } from 'redux';
import { history } from 'umi';
import DecodeTree from './DecodeTree';
import Filter from './Filter';
import FrameList, { tableColumns } from './FrameList';
import Hexdump from './Hexdump';
import styles from './index.less';
import IntervalAnalyse from './IntervalAnalyse';
import Page from './Page';
import { queryPacketAnalysis } from './service';
import StatTap from './StatTap';
import ToolbarMenu from './ToolbarMenu';
import type {
  EPktAnalysisDataSource,
  ICustomStatTapData,
  IFrameData,
  IHexdumpHighlight,
  IProtocolTreeNode,
  TFollowType,
  TFrameColTimeType,
} from './typings';

const FRAME_LIST_HEIGHT_LOCAL_KEY = 'flow-analysis-frame-list-height';
const DECODE_TREE_WIDTH_LOCAL_KEY = 'flow-analysis-decode-tree-width';

/** 在线分析对外暴露的props */
export interface IPktAnalysisSharedProps {
  /** 分析的数据源 */
  sourceType: EPktAnalysisDataSource;
  /** 全流量查询任务的ID */
  taskId?: string;
}

interface IPktAnalysisProps extends IPktAnalysisSharedProps {
  dispatch: Dispatch;

  pktAnalysisModel: IPktAnalysisModelState;
  queryPacketInfoLoading: boolean;
  queryPacketIntervalLoading: boolean;
  queryFrameListLoading: boolean;
  queryProtocolTreeLoading: boolean;

  [propName: string]: any;
}
const PktAnalysis: React.FC<IPktAnalysisProps> = (props) => {
  const {
    dispatch,
    sourceType,
    taskId,
    pktAnalysisModel,
    queryPacketInfoLoading,
    queryPacketIntervalLoading,
    queryFrameListLoading,
    queryProtocolTreeLoading,
  } = props;
  const queryParams = { sourceType, taskId };
  // 被选中的某一行帧的数据
  const [selectedPacket, setSelectedPacket] = useState<IFrameData>({} as IFrameData);
  // 高亮的数据字节
  const [hexdumpHighlight, setHexdumpHighlight] = useState<IHexdumpHighlight[]>([]);

  // 帧列表查询的时间是相对时间还是绝对时间
  const [colTimeType, setColTimeType] = useState<TFrameColTimeType>('relative');

  // 帧列表的高度
  const [frameListHeight, setFrameListHeight] = useState<number | string>(
    localStorage.getItem(FRAME_LIST_HEIGHT_LOCAL_KEY) || '400px',
  );
  // 数据详情区的宽度
  const [decodeTreeWidth, setDecodeTreeWidth] = useState<number | string>(
    localStorage.getItem(DECODE_TREE_WIDTH_LOCAL_KEY) || '60%',
  );

  const [paneHeight, setPaneHeight] = useState(900);

  const { filter: filterInputData } = pktAnalysisModel;

  useEffect(() => {
    queryPacketStatus();
  }, [sourceType, taskId]);

  const framesQueryCols = useMemo(() => {
    if (colTimeType === 'absolute') {
      return {
        column0: 35, // 帧 ID
        column1: 1, // COL_ABS_YMD_TIME
        column2: 39, // 源 IP
        column3: 14, // COL_DEF_DST 目的 IP
        column4: 37, // 协议类型
        column5: 36, // 长度
        column6: 28, // COL_INFO 内容
      };
    }
    return {};
  }, [colTimeType]);

  useEffect(() => {
    queryFrameList(0);
  }, [framesQueryCols]);

  /** 查询 PCAP 文件信息 */
  const queryPacketStatus = () => {
    (
      dispatch({
        type: 'pktAnalysisModel/queryPacketStatus',
        payload: {
          ...queryParams,
        },
      }) as unknown as Promise<any>
    ).then((success: boolean) => {
      if (success) {
        queryFrameList();
      }
    });
  };

  const getFilterParams = () => {
    const { startRelativeTime, endRelativeTime, filter } = pktAnalysisModel;

    let fullFilter = '';
    let timeFilter = '';

    if (startRelativeTime !== undefined && endRelativeTime !== undefined) {
      // 时间过滤条件
      timeFilter = `frame.time_relative >= ${startRelativeTime} && frame.time_relative <= ${endRelativeTime}`;
    }

    // 有过滤条件 && 有时间段
    if (filter && timeFilter) {
      fullFilter = `(${filter}) && (${timeFilter})`;
    }
    // 只有过滤条件
    if (filter && !timeFilter) {
      fullFilter = filter;
    }
    // 只有时间段
    if (!filter && timeFilter) {
      fullFilter = timeFilter;
    }

    return fullFilter;
  };

  // ====数据列表 Start=====
  /**
   * 查询帧列表数据
   * @param skip 从第几行开始查询
   */
  const queryFrameList = (skip?: number) => {
    const fullFilter = getFilterParams();

    (
      dispatch({
        type: 'pktAnalysisModel/queryFrameList',
        payload: {
          ...queryParams,
          filter: fullFilter,
          skip: skip || 0,
          ...framesQueryCols,
        },
      }) as unknown as Promise<any>
    ).then((frameList) => {
      if (frameList.length === 0) {
        setSelectedPacket({} as IFrameData);
        setHexdumpHighlight([]);
        // 清空decodeTree
        dispatch({
          type: 'pktAnalysisModel/clearProtocolTree',
          payload: {},
        });
      }

      if (frameList.length > 0) {
        const curPacket = frameList[0];
        setSelectedPacket(curPacket);
        queryProtocolTree(curPacket.num);
      }
    });
  };

  /**
   * 点击数据列表的某一行
   * @param frame
   */
  const handleClickFrameRow = (frame: IFrameData) => {
    setSelectedPacket(frame);
    setHexdumpHighlight([]);
    queryProtocolTree(frame.num);
  };

  // ====数据列表 End=====

  // ====数据详情（某一条数据包的详情） Start=====
  /**
   * 查询某一帧的数据包详情
   * @param frameNumber 帧序号
   */
  const queryProtocolTree = (frameNumber: number) => {
    dispatch({
      type: 'pktAnalysisModel/queryProtocolTree',
      payload: {
        ...queryParams,
        frame: frameNumber,
        prev_frame: frameNumber - 1,
      },
    });
  };

  /**
   * 协议
   * @param nodeData
   */
  const handleProtocolTreeSelect = (nodeData: IProtocolTreeNode) => {
    const highlight = [] as IHexdumpHighlight[];

    let dsIdx = nodeData.ds;
    if (!dsIdx) {
      dsIdx = 0;
    }

    /* highlight */
    if (nodeData.h) {
      highlight.push({
        tab: dsIdx,
        start: nodeData.h[0],
        end: nodeData.h[0] + nodeData.h[1],
        style: 'selected_bytes',
      });
    }

    /* appendix */
    if (nodeData.i) {
      highlight.push({
        tab: dsIdx,
        start: nodeData.i[0],
        end: nodeData.i[0] + nodeData.i[1],
        style: 'selected_bytes',
      });
    }

    /* protocol highlight */
    if (nodeData.p) {
      let pDsIdx = nodeData.p_ds;
      if (!pDsIdx) {
        pDsIdx = 0;
      }

      highlight.push({
        tab: pDsIdx,
        start: nodeData.p[0],
        end: nodeData.p[0] + nodeData.p[1],
        style: 'selected_proto',
      });
    }

    setHexdumpHighlight(highlight);
  };
  // ====数据详情（某一条数据包的详情） 结束=====

  // ====过滤条件 开始=====
  /** 过滤框内容变化 */
  const handleFilterInputChange = (filter: string) => {
    dispatch({
      type: 'pktAnalysisModel/changeFilter',
      payload: {
        filter,
      },
    });
  };
  /** 过滤 */
  const handleFilter = (filter: string) => {
    handleFilterInputChange(filter);
    // queryFrameList();
  };

  useEffect(() => {
    queryFrameList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterInputData]);

  // ====过滤条件 结束=====

  /** 时间图中的时间发生变化 */
  const handleSelectTimeChange = (relativeTimes: number[]) => {
    dispatch({
      type: 'pktAnalysisModel/changeRelativeTime',
      payload: {
        startRelativeTime: relativeTimes.length === 2 ? relativeTimes[0] : undefined,
        endRelativeTime: relativeTimes.length === 2 ? relativeTimes[1] : undefined,
      },
    });

    queryFrameList();
  };

  /** 切换页码 */
  const handlePageChange = (skip: number) => {
    queryFrameList(skip);
  };

  /** 调整帧列表的容器高度 */
  const handleChangeFrameListShape = (size: [string, string]) => {
    const newFrameListHeight = size[0];
    setFrameListHeight(newFrameListHeight);
    localStorage.setItem(FRAME_LIST_HEIGHT_LOCAL_KEY, newFrameListHeight);
  };

  /** 调整帧详情协议树的容器宽度 */
  const handleChangeDecodeTreeShape = (size: [string, string]) => {
    const newDecodeTreeWidth = size[0];
    setDecodeTreeWidth(newDecodeTreeWidth);
    localStorage.setItem(DECODE_TREE_WIDTH_LOCAL_KEY, newDecodeTreeWidth);
  };

  const statFollowFilter = (currentTap: ICustomStatTapData) => {
    if (!decodeTree) {
      return '';
    }

    if (!decodeTree.fol || decodeTree.fol.length === 0) {
      return '';
    }

    const target: [TFollowType, string] | undefined = decodeTree.fol.find(
      (el) => el && el[0] === currentTap.follow,
    );

    return target ? target[1] : '';
  };

  /** 统计按钮点击 */
  const handleMenuClick = (currentTap: ICustomStatTapData) => {
    const payloadParams: Record<string, any> = {
      ...queryParams,
    };

    let apiType = '';

    if (currentTap.tap) {
      payloadParams.tap0 = currentTap.tap;
      payloadParams.req = 'tap';
      apiType = 'pktAnalysisModel/queryStatTap';
    }
    if (currentTap.follow) {
      // 判断类型，查找 filter
      const filter = statFollowFilter(currentTap);
      if (!filter) {
        message.warning(`所选报文非${currentTap.follow}`);
        return;
      }
      payloadParams.follow = currentTap.follow;
      payloadParams.req = 'follow';
      payloadParams.filter = filter;
      apiType = 'pktAnalysisModel/queryStatFollow';
    }
    if (currentTap.filter) {
      payloadParams.filter = currentTap.filter;
    }

    // 获取数据
    dispatch({
      type: apiType,
      payload: {
        ...payloadParams,
      },
    });

    // 更新统计tap
    dispatch({
      type: 'pktAnalysisModel/changeCurrentTap',
      payload: {
        currentTap,
      },
    });
    // 显示弹出框
    dispatch({
      type: 'pktAnalysisModel/changeModalVisible',
      payload: {
        visible: true,
      },
    });
  };

  const handleExportFrame = (bookType: XLSXBookType) => {
    // 拉取全部的列表进行下载
    console.time('export frame');
    const fullFilter = getFilterParams();
    const messageKey = 'export-message';
    message.loading({ content: '导出中...', key: messageKey, duration: 0 });
    queryPacketAnalysis({
      ...queryParams,
      filter: fullFilter,
      req: 'frames',
      type: 'analyze',
      limit: 0, // 查询全部
      ...framesQueryCols,
    }).then(({ success, result }) => {
      if (!success) {
        message.error({ content: '导出失败', key: messageKey, duration: 2 });
      }
      const allFrameLength: IFrameData[] = parseArrayJson(result);
      if (allFrameLength.length === 0) {
        message.info({ content: '没有需要导出的数据', key: messageKey, duration: 2 });
        return;
      }

      // 标题栏
      const titleList = tableColumns.map((item) => item.title);
      let dataList: string[][] = [];

      const total = allFrameLength.length;
      for (let index = 0; index < total; index += 1) {
        // 内容中大概率不会有 半角逗号， 所以这里的单元格内的内容没有特殊处理
        // 如果需要特殊处理，可以使用 `` 包裹一层
        dataList.push(allFrameLength[index].c);
      }

      exportXlsx([titleList, ...dataList], `frame`, bookType);
      message.success({ content: '导出成功', key: messageKey, duration: 2 });

      // 回收
      dataList = null as any;
      console.timeEnd('export frame');
    });
  };

  const {
    info,
    frameList,
    currentPage,
    intervals,
    intervalTime,
    filterIntervals,
    decodeTree,
    startRelativeTime,
    endRelativeTime,
  } = pktAnalysisModel;

  return (
    <AutoHeightContainer onHeightChange={(h) => setPaneHeight(h)} fixHeight={5}>
      <div style={{ height: paneHeight }} className={styles.wrapper}>
        <Row gutter={10}>
          <Col span={24}>
            {/* 标题 */}
            <div className={styles.headerWrap}>
              {queryPacketInfoLoading ? (
                <Spin style={{ marginLeft: 10 }} indicator={<LoadingOutlined spin />} />
              ) : (
                <>
                  <span className={styles.name}>
                    {info.filename ? (
                      <Tooltip placement="topLeft" title={info.filename}>
                        {info.filename}
                      </Tooltip>
                    ) : (
                      '--'
                    )}
                  </span>
                  <span className={styles.extra}>
                    {bytesToSize(info.filesize || 0, 3, ONE_KILO_1024)} · {info.frames || 0}
                    frameList · {info.duration || 0}seconds
                  </span>
                  <span className={styles.back} onClick={() => history.goBack()}>
                    <RollbackOutlined />
                    返回上一页
                  </span>
                </>
              )}
            </div>
            {/* 过滤条件 */}
            {queryPacketInfoLoading ? (
              <Spin />
            ) : (
              <div style={{ display: 'flex', flexWrap: 'wrap', width: '100%' }}>
                <Filter
                  {...queryParams}
                  onFilter={handleFilter}
                  onFilterChange={handleFilterInputChange}
                  style={{ display: 'flex', flex: 1, whiteSpace: 'nowrap' }}
                />
                <IntervalAnalyse
                  intervals={intervals}
                  intervalTime={intervalTime}
                  startTime={startRelativeTime}
                  endTime={endRelativeTime}
                  loading={queryPacketIntervalLoading}
                  onTimeChange={handleSelectTimeChange}
                />
                {/* 统计菜单 */}
                <ToolbarMenu onClick={handleMenuClick} />
                {/* <div style={{ whiteSpace: 'nowrap' }}> */}
                <Dropdown
                  disabled={frameList.length === 0}
                  overlay={
                    <Menu onClick={(e) => handleExportFrame(e.key as XLSXBookType)}>
                      <Menu.Item key="csv">导出 CSV 文件</Menu.Item>
                      <Menu.Item key="xlsx">导出 Excel 文件</Menu.Item>
                    </Menu>
                  }
                  trigger={['click']}
                >
                  <Button
                    // 当前页没有数据时，表示就没有数据
                    // 所以不再导出全部
                    style={{ margin: '0 3px' }}
                    disabled={frameList.length === 0}
                  >
                    导出 <DownOutlined />
                  </Button>
                </Dropdown>
                <Space align="center">
                  {/* 新增导出按钮 */}
                  {/* 相对时间和绝对时间切换 */}
                  <Checkbox
                    checked={colTimeType === 'absolute'}
                    onChange={(e) => {
                      const checked = e.target.checked;
                      setColTimeType(checked ? 'absolute' : 'relative');
                    }}
                  >
                    <div style={{ whiteSpace: 'nowrap' }}>绝对时间</div>
                  </Checkbox>
                  <Page
                    currentPage={currentPage}
                    intervals={intervals}
                    filterIntervals={filterIntervals}
                    loading={queryFrameListLoading}
                    onPageChange={handlePageChange}
                  />
                </Space>
                {/* </div> */}
              </div>
            )}
          </Col>
        </Row>
        <div style={{ height: 'calc(100% - 80px)' }} className={styles.splitWrap}>
          {/* 列表 */}
          <SplitPane
            split="horizontal"
            // @ts-ignore
            onChange={handleChangeFrameListShape}
            style={{ height: '100%' }}
          >
            <Pane initialSize={frameListHeight} minSize="200px">
              <FrameList
                selectedPacket={selectedPacket}
                frameList={frameList}
                height={
                  typeof frameListHeight === 'string'
                    ? Number.parseInt(frameListHeight, 10)
                    : frameListHeight
                }
                onRowClick={handleClickFrameRow}
                loading={queryPacketInfoLoading || queryFrameListLoading}
                detailLoading={
                  queryPacketInfoLoading || queryFrameListLoading || queryProtocolTreeLoading
                }
              />
            </Pane>
            <Pane minSize="100px">
              <SplitPane
                split="vertical"
                className={styles.splitPane}
                resizerClassName={styles.resizer}
                // @ts-ignore
                onChange={handleChangeDecodeTreeShape}
              >
                <Pane minSize="20%" initialSize={decodeTreeWidth}>
                  <Card className={styles.paneCard}>
                    <DecodeTree
                      decodeData={decodeTree}
                      onTreeClick={handleProtocolTreeSelect}
                      onFilter={handleFilter}
                      loading={
                        queryPacketInfoLoading || queryFrameListLoading || queryProtocolTreeLoading
                      }
                      style={{ height: '100%' }}
                    />
                  </Card>
                </Pane>
                <Pane minSize="20%">
                  <Card className={styles.paneCard}>
                    <Hexdump
                      decodeData={decodeTree}
                      highlights={hexdumpHighlight}
                      loading={
                        queryPacketInfoLoading || queryFrameListLoading || queryProtocolTreeLoading
                      }
                    />
                  </Card>
                </Pane>
              </SplitPane>
            </Pane>
          </SplitPane>
        </div>
        <StatTap {...queryParams} />
      </div>
    </AutoHeightContainer>
  );
};

export default connect(({ pktAnalysisModel, loading: { effects } }: ConnectState) => ({
  pktAnalysisModel,
  queryPacketInfoLoading: effects['pktAnalysisModel/queryPacketStatus'] || false,
  queryPacketIntervalLoading: effects['pktAnalysisModel/queryPacketInterval'] || false,
  queryFrameListLoading: effects['pktAnalysisModel/queryFrameList'] || false,
  queryProtocolTreeLoading: effects['pktAnalysisModel/queryProtocolTree'] || false,
}))(PktAnalysis);
