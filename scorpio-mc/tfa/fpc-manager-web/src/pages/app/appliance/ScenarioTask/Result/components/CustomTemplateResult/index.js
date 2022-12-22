import { compareUri, getLinkUrl, handleQueryData, handleRefreshPage } from '@/utils/utils';
import { BarChartOutlined } from '@ant-design/icons';
import { Col, Divider, Modal, Row } from 'antd';
import { connect } from 'umi';
import moment from 'moment';
import React, { Fragment, PureComponent } from 'react';
import { Link, withRouter } from 'umi';
import {
  EVAL_FUNCTION_BEACON,
  EVAL_FUNCTION_COUNT,
  EVAL_FUNCTION_SUM,
} from '../../../components/ScenarioTemplateForm';
import { getComputableFieldInfo } from '../../../components/ScenarioTemplateProfile';
import CustomTemplatSliceGroup from '../CustomTemplatSliceGroup';
import { SORT_DIRECTION_ASC, SORT_DIRECTION_DESC } from '../../index';
import ResultTable from '../ResultTable';

const getGroupByInfo = (groupByText) => {
  let groupByJson = {};
  try {
    groupByJson = JSON.parse(groupByText);
  } catch (error) {
    groupByJson = {};
  }
  return groupByJson;
};

@withRouter
@connect((state) => {
  const { scenarioTaskModel, loading } = state;
  return {
    scenarioTaskModel,
    queryLoading: loading.effects['scenarioTaskModel/queryScenarioTaskResults'],
  };
})
class CustomTemplateResult extends PureComponent {
  constructor(props) {
    super(props);
    this.handleRefreshPage = handleRefreshPage.bind(this);
    this.compareUri = compareUri.bind(this);
    this.handleQueryData = handleQueryData.bind(this);
  }

  state = {
    timeSliceDetail: { title: '', data: [], groupByDom: [] },
    isModalVisible: false,
  };

  componentDidMount() {
    this.queryResult();
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    this.compareUri(nextProps, this.queryResult);
  }

  queryResult = (newQuery = {}) => {
    const {
      scenarioTaskModel: { scenarioTaskDetail },
    } = this.props;

    this.handleQueryData(
      {
        ...newQuery,
        sortProperty: newQuery.sortProperty || 'function_result',
        id: scenarioTaskDetail.id,
        type: scenarioTaskDetail.type,
      },
      'scenarioTaskModel/queryScenarioTaskResults',
    );
  };

  handleTableChange = (pageObj, filters, sorter) => {
    const sortOrder = sorter.order || 'ascend';
    const sortProperty = sorter.field;
    this.handleRefreshPage(
      {
        page: pageObj.current || 1,
        pageSize: pageObj.pageSize,
        sortProperty,
        sortDirection: sortOrder === 'descend' ? SORT_DIRECTION_DESC : SORT_DIRECTION_ASC,
      },
      'replace',
    );
  };

  handleShowSliceGroupResult = (resultRecord) => {
    const {
      scenarioTaskModel: {
        scenarioCustomTemplateDetail: { sliceTimeInterval },
      },
    } = this.props;
    const { time_slice_list: timeSliceList, group_by: groupBy } = resultRecord;
    const timeSliceArray = timeSliceList ? timeSliceList.split(',') : [];

    // 构造 title
    const title = `按时间切片（${sliceTimeInterval}s）`;

    const groupByDom = [];
    // 分组信息
    if (groupBy) {
      const {
        src_ip: srcIp,
        dest_ip: destIp,
        src_port: srcPort,
        dest_port: destPort,
      } = getGroupByInfo(groupBy);

      if (srcIp) {
        groupByDom.push(<span>源IP: {srcIp}</span>);
      }
      if (srcPort) {
        groupByDom.push(<span>源端口: {srcPort}</span>);
      }
      if (destIp) {
        groupByDom.push(<span>目的IP: {destIp}</span>);
      }
      if (destPort) {
        groupByDom.push(<span>目的端口: {destPort}</span>);
      }
    }

    this.setState({
      timeSliceDetail: {
        title,
        data: timeSliceArray,
        groupByDom,
      },
      isModalVisible: true,
    });
  };

  render() {
    const {
      queryLoading,
      scenarioTaskModel: { scenarioTaskResult, pagination, scenarioCustomTemplateDetail },
      location: { query },
    } = this.props;
    const { sortProperty = 'function_result' } = query;

    const {
      avgTimeInterval,
      sliceTimeInterval,
      function: functionJson,
    } = scenarioCustomTemplateDetail;

    const columns = [
      {
        title: '分组',
        dataIndex: 'group_by',
        align: 'center',
        width: 200,
        render: (groupBy) => {
          if (!groupBy) {
            return '未分组';
          }

          const {
            src_ip: srcIp,
            dest_ip: destIp,
            src_port: srcPort,
            dest_port: destPort,
          } = getGroupByInfo(groupBy);

          return (
            <Row style={{ textAlign: 'left', margin: '0 auto', padding: '0px 10px' }}>
              {srcIp && <Col>源IP: {srcIp}</Col>}
              {srcPort && <Col>源端口: {srcPort}</Col>}
              {destIp && <Col>目的IP: {destIp}</Col>}
              {destPort && <Col>目的端口: {destPort}</Col>}
            </Row>
          );
        },
      },
      {
        title: '数据开始时间',
        dataIndex: 'record_start_time',
        align: 'center',
        width: 180,
        render: (time) => (time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : ''),
      },
      {
        title: '数据结束时间',
        dataIndex: 'record_end_time',
        align: 'center',
        width: 180,
        render: (time) => (time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : ''),
      },
      {
        title: () => {
          let evalFunction = {};
          try {
            evalFunction = JSON.parse(functionJson);
          } catch (error) {
            evalFunction = {};
          }
          const { name: functionName, params: functionParams = {} } = evalFunction;
          if (functionName === EVAL_FUNCTION_COUNT) {
            return functionName;
          }
          if (functionName === EVAL_FUNCTION_SUM) {
            const sumFieldText = getComputableFieldInfo(functionParams.field).label;
            return `${functionName}(字段: ${sumFieldText})`;
          }
          if (functionName === EVAL_FUNCTION_BEACON) {
            return `${functionName}(阈值: ${functionParams.numberThreshold})`;
          }
          return '';
        },
        dataIndex: 'function_result',
        sorter: true,
        sortOrder:
          sortProperty === 'function_result' ? `${query.sortDirection || 'desc'}end` : false,
        align: 'center',
      },
      {
        title: `按时间平均（${avgTimeInterval}s）`,
        dataIndex: 'time_avg_hit',
        sorter: true,
        width: 180,
        sortOrder: sortProperty === 'time_avg_hit' ? `${query.sortDirection || 'desc'}end` : false,
        align: 'center',
      },
      {
        title: '操作',
        width: 120,
        align: 'center',
        render: (text, record) => {
          const { dataSource } = scenarioCustomTemplateDetail;
          const endTime = moment(record.record_end_time).add(1, 'seconds').format();
          const queryParms = `analysisResultId=${record.id}&analysisStartTime=${encodeURIComponent(
            record.record_start_time,
          )}&analysisEndTime=${encodeURIComponent(endTime)}`;
          return (
            <Fragment>
              {/* 根据数据源跳转至不同的地方 */}
              {dataSource === 'flow-log-record' && (
                <Link to={getLinkUrl(`/analysis/security/scenario-task/result/flow-record?${queryParms}`)}>
                  详情
                </Link>
              )}

              {dataSource === 'http' && (
                <Link
                  to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-http?${queryParms}`)}
                >
                  详情
                </Link>
              )}

              {dataSource === 'dns' && (
                <Link to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-dns?${queryParms}`)}>
                  详情
                </Link>
              )}

              {dataSource === 'ftp' && (
                <Link to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-ftp?${queryParms}`)}>
                  详情
                </Link>
              )}

              {dataSource === 'mail' && (
                <Link
                  to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-mail?${queryParms}`)}
                >
                  详情
                </Link>
              )}

              {dataSource === 'telnet' && (
                <Link
                  to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-telnet?${queryParms}`)}
                >
                  详情
                </Link>
              )}

              {dataSource === 'ssl' && (
                <Link to={getLinkUrl(`/analysis/security/scenario-task/result/metadata-ssl?${queryParms}`)}>
                  详情
                </Link>
              )}

              <Fragment>
                <Divider type="vertical" />
                {sliceTimeInterval > 0 ? (
                  <a onClick={() => this.handleShowSliceGroupResult(record)}>分片结果</a>
                ) : (
                  <span>未分片</span>
                )}
              </Fragment>
            </Fragment>
          );
        },
      },
    ];

    const {isModalVisible, timeSliceDetail} = this.state
    const {data, groupByDom, title} = timeSliceDetail

    return (
      <Fragment>
        <ResultTable
          loading={queryLoading}
          columns={columns}
          dataSource={scenarioTaskResult}
          pagination={pagination}
          sortDirections={['ascend', 'descend']}
          onTableChange={this.handleTableChange}
        />
        <Modal
          title={title}
          onCancel={() => {
            this.setState({ isModalVisible: false });
          }}
          visible={isModalVisible}
          maskClosable={false}
          footer={false}
          width={'80%'}
        >
          <Fragment>
            {groupByDom.map((item, index) => (
              <Fragment>
                {item}
                {index !== groupByDom.length - 1 && <Divider type="vertical" />}
              </Fragment>
            ))}
            <CustomTemplatSliceGroup
              title=""
              categories={[]}
              data={data.map((item) => +item)}
            />
          </Fragment>
        </Modal>
      </Fragment>
    );
  }
}

export default CustomTemplateResult;
