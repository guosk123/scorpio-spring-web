import React, { Fragment, Component, memo } from 'react';
import { Row, Col, Spin } from 'antd';
import { connect } from 'dva';
import _ from 'lodash';
import BaseTable, { unflatten, AutoResizer } from 'react-base-table';
import 'react-base-table/styles.css';

import styles from './index.less';

/**
 * 按主机统计
 */
const innerHostColumns = [
  {
    title: '源IP',
    key: 'inner_host',
    dataKey: 'inner_host',
    width: 300,
  },
  {
    title: '访问域名数',
    key: 'count',
    dataKey: 'count',
    width: 100,
    align: 'center',
    sortable: true,
  },
  {
    title: '访问次数',
    key: 'record_total_hit',
    dataKey: 'record_total_hit',
    width: 100,
    align: 'center',
    sortable: true,
  },
];

/**
 * 按域名统计
 */
const dynamicDomainTermColumns = [
  {
    title: '域名',
    key: 'dynamic_domain',
    dataKey: 'dynamic_domain',
    width: 300,
  },
  {
    title: '访问主机数',
    key: 'count',
    dataKey: 'count',
    width: 100,
    align: 'center',
    sortable: true,
  },
  {
    title: '访问次数',
    key: 'record_total_hit',
    dataKey: 'record_total_hit',
    width: 100,
    align: 'center',
    sortable: true,
  },
];

const TableWrap = memo(
  ({ title, data, columns, loading, sortBy, rowKey, selectedRow, onRowClick, onColumnSort }) => (
    <section className={styles.tableWrap}>
      <div className={styles.tableTitle}>{title}</div>
      <div className={styles.tableContent}>
        <AutoResizer>
          {({ width, height }) => (
            <BaseTable
              headerHeight={36}
              rowHeight={36}
              width={width}
              height={height}
              fixed
              sortBy={sortBy}
              columns={columns}
              rowEventHandlers={{
                onClick: ({ rowData }) =>
                  selectedRow === rowData[rowKey] ? null : onRowClick(rowKey, rowData[rowKey]),
              }}
              rowClassName={({ rowData }) =>
                `${styles.row} ${selectedRow === rowData[rowKey] ? styles.selected : ''}`
              }
              data={unflatten(data)}
              onColumnSort={onColumnSort}
              overlayRenderer={() => {
                if (loading) {
                  return (
                    <div className={styles.loading}>
                      <Spin spinning={loading} />
                    </div>
                  );
                }
                return null;
              }}
              emptyRenderer={() => {
                if (loading) {
                  return null;
                }
                return <div className={styles.emptey}>暂无数据</div>;
              }}
            />
          )}
        </AutoResizer>
      </div>
    </section>
  )
);

@connect(state => {
  const { scenarioTaskModel, loading } = state;
  return {
    scenarioTaskModel,
    queryTermLoading: loading.effects['scenarioTaskModel/queryScenarioTaskDynamicDomainTerms'],
  };
})
class DynamicDomainTerm extends Component {
  constructor(props) {
    super(props);

    this.state = {
      innerHostTerms: [],
      innerHostTermLoading: true,
      innerHostTermSortBy: undefined,

      dynamicDomainTerms: [],
      dynamicDomainTermLoading: true,
      dynamicDomainTermSortBy: undefined,
    };
  }

  componentDidMount() {
    const {
      dispatch,
      scenarioTaskModel: { scenarioTaskDetail },
    } = this.props;

    dispatch({
      type: 'scenarioTaskModel/queryScenarioTaskDynamicDomainTerms',
      payload: {
        id: scenarioTaskDetail.id,
        type: scenarioTaskDetail.type,
        termField: 'inner_host',
      },
    }).then(result => {
      this.setState({
        innerHostTermLoading: false,
        innerHostTerms: result,
      });
    });
    dispatch({
      type: 'scenarioTaskModel/queryScenarioTaskDynamicDomainTerms',
      payload: {
        id: scenarioTaskDetail.id,
        type: scenarioTaskDetail.type,
        termField: 'dynamic_domain',
      },
    }).then(result => {
      this.setState({
        dynamicDomainTermLoading: false,
        dynamicDomainTerms: result,
      });
    });
  }

  shouldComponentUpdate(nextProps, nextState) {
    return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
  }

  handleColumnSort = (dataStateKey, sortStateKey, sortBy) => {
    const order = sortBy.order === 'asc' ? 1 : -1;
    const data = [...this.state[dataStateKey]];
    data.sort((a, b) => (a[sortBy.key] > b[sortBy.key] ? order : -order));
    this.setState({
      [sortStateKey]: sortBy,
      [dataStateKey]: data,
    });
  };

  render() {
    const { selectedInnerHost, selectedDynamicDomain, onRowClick } = this.props;
    const {
      innerHostTerms,
      innerHostTermLoading,
      innerHostTermSortBy,

      dynamicDomainTerms,
      dynamicDomainTermLoading,
      dynamicDomainTermSortBy,
    } = this.state;

    return (
      <Fragment>
        {/* 聚合结果 */}
        <Row gutter={10}>
          <Col span={12}>
            <TableWrap
              title="按主机统计"
              data={innerHostTerms}
              columns={innerHostColumns}
              loading={innerHostTermLoading}
              onRowClick={onRowClick}
              selectedRow={selectedInnerHost}
              sortBy={innerHostTermSortBy}
              onColumnSort={sortBy =>
                this.handleColumnSort('innerHostTerms', 'innerHostTermSortBy', sortBy)
              }
              rowKey="inner_host"
            />
          </Col>
          <Col span={12}>
            <TableWrap
              title="按域名统计"
              data={dynamicDomainTerms}
              columns={dynamicDomainTermColumns}
              loading={dynamicDomainTermLoading}
              onRowClick={onRowClick}
              selectedRow={selectedDynamicDomain}
              sortBy={dynamicDomainTermSortBy}
              onColumnSort={sortBy =>
                this.handleColumnSort('dynamicDomainTerms', 'dynamicDomainTermSortBy', sortBy)
              }
              rowKey="dynamic_domain"
            />
          </Col>
        </Row>
      </Fragment>
    );
  }
}

export default DynamicDomainTerm;
