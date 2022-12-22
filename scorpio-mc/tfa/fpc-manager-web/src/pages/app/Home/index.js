import { ChartCard } from '@/components/Charts';
import { convertBandwidth, ipV4Regex } from '@/utils/utils';
import { PieChartOutlined, SyncOutlined } from '@ant-design/icons';
import { GridContent } from '@ant-design/pro-layout';
import { Button, Col, Input, message, Modal, Row, Spin, Tooltip } from 'antd';
import { connect } from 'dva';
import numeral from 'numeral';
import React, { PureComponent } from 'react';
import bandwidth from './assets/bandwidth.svg';
import collectIcon from './assets/collect.svg';
import fsIcon from './assets/fs.svg';
import AlarmMetric from './components/AlarmMetric';
import ApplicationFlowTop from './components/ApplicationFlowTop';
import DiskBox from './components/DiskList';
import FlowMetric from './components/FlowMetric';
import PacketStoreMetric from './components/PacketStoreMetric';
import ProtocolFlowTop from './components/ProtocolFlowTop';
import ProtocolPie from './components/ProtocolPie';
import ProtocolTable from './components/ProtocolTable';
import RuntimeEnvironment from './components/RuntimeEnvironment';
import SessionMetricsChart from './components/SessionMetricsChart';
import SystemStateMetrics from './components/SystemStateMetrics';
import styles from './index.less';
import { customSpin } from './utils';

const colProps = {
  xl: 12,
  lg: 24,
};

const chartCardProps = {
  bordered: false,
  bodyStyle: { padding: '20px 10px 8px 24px' },
};

@connect(({ metricModel, appModel, homeModel, loading: { effects } }) => ({
  metricModel,
  homeModel,
  systemRuntime: appModel.systemRuntime,
  queryRuntimeLoading: effects['appModel/queryRuntimeEnvironments'],
  countFlowsLoading: effects['metricModel/countFlowProtocol'],
  queryLatelyBandwidthLoading: effects['homeModel/queryLatelyBandwidth'],
}))
class Home extends PureComponent {
  state = {
    srcIp: '', // 搜索的源ip
    protocolPieModalVisible: false,
  };

  componentDidMount() {
    this.countFlowProtocol();
    this.queryLatelyBandwidth();
    this.queryDisk();
  }

  countFlowProtocol = (srcIp = '') => {
    const { dispatch } = this.props;
    dispatch({
      type: 'metricModel/countFlowProtocol',
      payload: {
        srcIp,
      },
    });
  };

  queryLatelyBandwidth = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'homeModel/queryLatelyBandwidth',
      payload: {},
    });
  };

  queryDisk = () => {
    const { dispatch } = this.props;
    dispatch({ type: 'deviceDiskModel/queryDeviceDisks' });
  };

  // =====协议分布统计=====
  handleOpenModal = () => {
    this.setState({
      protocolPieModalVisible: true,
    });
    this.countFlowProtocol();
  };

  handleCloseModal = () => {
    this.setState({
      protocolPieModalVisible: false,
    });

    this.setState({ srcIp: '' });
  };

  handleSearchFlowsProtocol = (srcIp) => {
    // 检查是否是正确的ip地址
    if (srcIp && !ipV4Regex.test(srcIp)) {
      message.warn('请输入正确的IP地址');
      return;
    }

    this.setState({ srcIp });
    this.countFlowProtocol(srcIp);
  };

  render() {
    const { protocolPieModalVisible, srcIp } = this.state;

    const {
      metricModel: { flowProtocolCount, flowProtocol },
      systemRuntime,
      homeModel: { currentAvgBitsps },
      countFlowsLoading,
      queryRuntimeLoading,
      queryLatelyBandwidthLoading,
    } = this.props;

    const chartList = [
      {
        title: '接口流量',
        colProps,
        component: <FlowMetric />,
      },
      {
        title: '协议流量统计',
        colProps,
        component: <ProtocolFlowTop />,
      },
      {
        title: '会话统计',
        colProps,
        component: <SessionMetricsChart />,
      },
      {
        title: '应用流量统计',
        colProps,
        component: <ApplicationFlowTop />,
      },
      {
        title: '系统状态',
        colProps: { span: 24 },
        component: <SystemStateMetrics />,
      },
    ];

    // 磁盘使用率
    let systemFsUsageText = '--';
    if (systemRuntime.systemFsUsedRatio) {
      systemFsUsageText = `${systemRuntime.systemFsUsedRatio}%`;
    }

    return (
      <GridContent style={{ padding: 20, background: '#f0f2f5' }} className={styles.home}>
        <Row gutter={10} style={{ marginBottom: 10 }}>
          <Col span={12}>
            <RuntimeEnvironment chartProps={chartCardProps} />
          </Col>
          <Col span={12}>
            <PacketStoreMetric chartProps={chartCardProps} />
          </Col>
        </Row>
        <Row gutter={10} style={{ marginBottom: 10 }}>
          <Col span={6}>
            <ChartCard
              {...chartCardProps}
              title="系统分区使用率"
              avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={fsIcon} />}
              total={() => (queryRuntimeLoading ? customSpin : systemFsUsageText)}
              // action={
              //   <Tooltip title="点击刷新" onClick={this.queryRuntimeEnvironments}>
              //     <Icon type="sync" style={{ fontSize: 16 }} />
              //   </Tooltip>
              // }
            />
          </Col>
          <Col span={6}>
            <ChartCard
              {...chartCardProps}
              title="应用层协议采集数量"
              avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={collectIcon} />}
              total={() =>
                countFlowsLoading ? (
                  customSpin
                ) : (
                  <span style={{ fontSize: 26 }}>{numeral(flowProtocolCount).format('0,0')}</span>
                )
              }
              action={
                <Tooltip title="查看协议分布统计" onClick={this.handleOpenModal}>
                  <PieChartOutlined style={{ fontSize: 16 }} /> 统计
                </Tooltip>
              }
            />
          </Col>
          <Col span={6}>
            <ChartCard
              {...chartCardProps}
              title="最近30秒平均总带宽"
              avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={bandwidth} />}
              total={() =>
                queryLatelyBandwidthLoading ? (
                  customSpin
                ) : (
                  <span>{convertBandwidth(currentAvgBitsps)}</span>
                )
              }
              action={
                <Tooltip title="点击刷新" onClick={this.queryLatelyBandwidth}>
                  <SyncOutlined style={{ fontSize: 16 }} />
                </Tooltip>
              }
            />
          </Col>
          <Col span={6}>
            <AlarmMetric chartProps={chartCardProps} />
          </Col>
        </Row>
        <Row gutter={10}>
          {chartList.map((chart) => (
            <Col {...chart.colProps} key={chart.title}>
              {chart.component}
            </Col>
          ))}
          <Col span={24}>
            <DiskBox />
          </Col>
        </Row>
        <Modal
          visible={protocolPieModalVisible}
          width={800}
          destroyOnClose
          title="各协议数量占比统计"
          onCancel={this.handleCloseModal}
          keyboard={false}
          maskClosable={false}
          footer={[
            <Button key="close-button" type="primary" onClick={this.handleCloseModal}>
              关闭
            </Button>,
          ]}
        >
          <div className={styles.search}>
            <Input.Search
              placeholder="请输出源IP进行查询"
              onSearch={(value) => this.handleSearchFlowsProtocol(value)}
              enterButton
            />
          </div>
          <div className={styles.chartWrap}>
            <Spin spinning={countFlowsLoading}>
              <ProtocolPie data={flowProtocol} srcIp={srcIp} />
              <ProtocolTable data={flowProtocol} srcIp={srcIp} />
            </Spin>
          </div>
        </Modal>
      </GridContent>
    );
  }
}

export default Home;
