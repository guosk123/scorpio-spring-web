/* eslint-disable camelcase */
import { compareUri, getLinkUrl, handleQueryData, handleRefreshPage } from '@/utils/utils';
import { Divider, Tag } from 'antd';
import { connect } from 'dva';
import React, { Fragment, PureComponent } from 'react';
import { Link, withRouter } from 'umi';
import styles from './index.less';
import DynamicDomainTerm from '../DynamicDomainTerm';
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
class DynamicDomain extends PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      inner_host: undefined,
      dynamic_domain: undefined,
    };

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
    const { inner_host, dynamic_domain } = this.state;
    const {
      scenarioTaskModel: { scenarioTaskDetail },
    } = this.props;

    // 根据搜索条件，增加query查询条件
    const query = {};
    if (inner_host) {
      query.inner_host = inner_host;
    }
    if (dynamic_domain) {
      query.dynamic_domain = dynamic_domain;
    }

    this.handleQueryData(
      {
        ...newQuery,
        id: scenarioTaskDetail.id,
        type: scenarioTaskDetail.type,
        sortProperty: 'record_total_hit',
        query,
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

  handleRowClick = (fieldName, fieldValue) => {
    this.setState(
      {
        [fieldName]: fieldValue,
      },
      () => {
        this.queryResult({
          page: 1,
        });
      },
    );
  };

  clearSelected = (e, fieldName) => {
    e.preventDefault();
    this.setState(
      {
        [fieldName]: undefined,
      },
      () => {
        this.queryResult({
          page: 1,
        });
      },
    );
  };

  render() {
    const {
      queryLoading,
      scenarioTaskModel: { scenarioTaskDetail, scenarioTaskResult, pagination },
      location: { query },
    } = this.props;

    const { inner_host, dynamic_domain } = this.state;
    const columns = [
      {
        title: '源IP',
        dataIndex: 'inner_host',
        align: 'center',
      },
      {
        title: '域名',
        dataIndex: 'dynamic_domain',
        align: 'center',
      },
      {
        title: '访问次数',
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
              `/detection/scenario-task/result/metadata-dns?analysisResultId=${
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
      <Fragment>
        {/* 聚合数据 */}
        <DynamicDomainTerm
          onRowClick={this.handleRowClick}
          selectedInnerHost={inner_host || {}}
          selectedDynamicDomain={dynamic_domain || {}}
        />
        {/* 查询条件 */}
        <Divider />
        {/* 上报的统计数据 */}
        <div className={styles.selectedWrap}>
          {inner_host && (
            <Tag color="#2db7f5" closable onClose={(e) => this.clearSelected(e, 'inner_host')}>
              源IP: {inner_host}
            </Tag>
          )}
          {dynamic_domain && (
            <Tag color="#108ee9" closable onClose={(e) => this.clearSelected(e, 'dynamic_domain')}>
              域名: {dynamic_domain}
            </Tag>
          )}
        </div>
        <ResultTable
          loading={queryLoading}
          columns={columns}
          dataSource={scenarioTaskResult}
          pagination={pagination}
          onTableChange={this.handleTableChange}
        />
      </Fragment>
    );
  }
}

export default DynamicDomain;
