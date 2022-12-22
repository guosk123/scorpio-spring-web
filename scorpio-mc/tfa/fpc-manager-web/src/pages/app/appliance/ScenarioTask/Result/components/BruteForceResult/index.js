import { compareUri, getLinkUrl, handleQueryData, handleRefreshPage } from '@/utils/utils';
import { connect } from 'dva';
import moment from 'moment';
import React, { PureComponent } from 'react';
import { Link, withRouter } from 'umi';
import { SORT_DIRECTION_ASC, SORT_DIRECTION_DESC } from '../../index';
import ResultTable from '../ResultTable';

@withRouter
@connect((state) => {
  const { scenarioTaskModel, loading } = state;
  return {
    scenarioTaskModel,
    queryLoading: loading.effects['scenarioTaskModel/queryScenarioTaskResults'],
  };
})
class BruteForceResult extends PureComponent {
  constructor(props) {
    super(props);
    this.handleRefreshPage = handleRefreshPage.bind(this);
    this.compareUri = compareUri.bind(this);
    this.handleQueryData = handleQueryData.bind(this);
  }

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

  render() {
    const {
      queryLoading,
      scenarioTaskModel: { scenarioTaskDetail, scenarioTaskResult, pagination },
      location: { query },
    } = this.props;
    const { sortProperty = 'record_total_hit' } = query;

    const columns = [
      {
        title: '主机',
        dataIndex: 'inner_host',
        align: 'center',
        width: 200,
      },
      {
        title: '暴破开始时间',
        dataIndex: 'start_time',
        align: 'center',
        width: 180,
        render: (time) => (time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : ''),
      },
      {
        title: '暴破结束时间',
        dataIndex: 'end_time',
        align: 'center',
        width: 180,
        render: (time) => (time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : ''),
      },
      {
        title: '暴破次数',
        dataIndex: 'record_total_hit',
        sorter: true,
        sortOrder:
          sortProperty === 'record_total_hit' ? `${query.sortDirection || 'desc'}end` : false,
        align: 'center',
      },
      {
        title: '每分钟最大次数',
        sorter: true,
        sortOrder:
          sortProperty === 'record_max_hit_every_1minutes'
            ? `${query.sortDirection || 'desc'}end`
            : false,
        dataIndex: 'record_max_hit_every_1minutes',
        align: 'center',
      },
      {
        title: '每3分钟最大次数',
        sorter: true,
        sortOrder:
          sortProperty === 'record_max_hit_every_3minutes'
            ? `${query.sortDirection || 'desc'}end`
            : false,
        dataIndex: 'record_max_hit_every_3minutes',
        align: 'center',
      },
      {
        title: '操作',
        width: 100,
        align: 'center',
        render: (text, record) => (
          <Link
            to={getLinkUrl(
              `/analysis/security/scenario-task/result/flow-record?analysisResultId=${
                record.id
              }&analysisStartTime=${encodeURIComponent(
                scenarioTaskDetail.analysisStartTime,
              )}&analysisEndTime=${encodeURIComponent(scenarioTaskDetail.analysisEndTime)}`,
            )}
          >
            详情
          </Link>
        ),
      },
    ];

    return (
      <ResultTable
        loading={queryLoading}
        columns={columns}
        dataSource={scenarioTaskResult}
        pagination={pagination}
        sortDirections={['ascend', 'descend']}
        onTableChange={this.handleTableChange}
      />
    );
  }
}

export default BruteForceResult;
