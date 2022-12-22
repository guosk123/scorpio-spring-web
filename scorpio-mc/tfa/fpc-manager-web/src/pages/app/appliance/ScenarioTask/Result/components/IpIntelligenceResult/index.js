import { compareUri, getLinkUrl, handleQueryData, handleRefreshPage } from '@/utils/utils';
import { connect } from 'dva';
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
class IpIntelligenceResult extends PureComponent {
  constructor(props) {
    super(props);
    this.handleRefreshPage = handleRefreshPage.bind(this);
    this.compareUri = compareUri.bind(this);
    this.handleQueryData = handleQueryData.bind(this);
  }

  componentDidMount() {
    this.queryResult();
  }

  componentWillReceiveProps(nextProps) {
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
        sortProperty: 'record_total_hit',
      },
      'scenarioTaskModel/queryScenarioTaskResults',
    );
  };

  handleTableChange = (pageObj, filters, sorter) => {
    const sortOrder = sorter.order || 'ascend';
    this.handleRefreshPage(
      {
        page: pageObj.current || 1,
        pageSize: pageObj.pageSize,
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

    const columns = [
      {
        title: 'IP地址',
        dataIndex: 'ip_address',
        align: 'center',
      },
      {
        title: '数量',
        dataIndex: 'record_total_hit',
        sorter: true,
        sortOrder: `${query.sortDirection || 'desc'}end`,
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
        onTableChange={this.handleTableChange}
      />
    );
  }
}

export default IpIntelligenceResult;