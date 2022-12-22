import { useState, useEffect, Fragment } from 'react';
import type { IAbnormalEventMessage } from '@/pages/app/configuration/AbnormalEvent/typings';
import { Table } from 'antd';
import { connect } from 'umi';

const SaftyAlertListUi = ({
  totalPages,
  current,
  totalElements,
  pageSize,
  updatePageData,
  abnormalEventMessage
}: {
  totalPages: number,
  current: number,
  totalElements: number,
  pageSize: number
  updatePageData: (page: number, pageSize: number) => void,
  abnormalEventMessage: IAbnormalEventMessage[]
}) => {
  const [safty_alert_column] = useState([
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
      width: 190,
    },
    {
      title: '攻击类型',
      dataIndex: 'attack_type',
      key: 'attack_type',
    },
    {
      title: '攻击详情',
      dataIndex: 'attack_details',
      key: 'attack_details',
    },
    {
      title: '攻击源',
      dataIndex: 'attack_source',
      key: 'attack_source',
    },
    {
      title: '攻击源所属区域',
      dataIndex: 'attack_source_area',
      key: 'attack_source_area',
    },
    {
      title: '攻击目标',
      dataIndex: 'attack_dest',
      key: 'attack_dest',
    },
    {
      title: '攻击目标所属区域',
      dataIndex: 'attack_dest_area',
      key: 'attack_dest_area',
    },
  ]);
  const [safty_alert_data, set_safty_alert_data] = useState([]);
  function changePage(current: number) {
    updatePageData(current - 1, pageSize)
  }

  useEffect(() => {
    getData(abnormalEventMessage, set_safty_alert_data);
  }, [abnormalEventMessage]);


  function getData(data: IAbnormalEventMessage[], data_setter: Function) {
    data_setter(data);
  }

  // 表格分页属性
  const paginationProps = {
    showSizeChanger: false,
    showQuickJumper: true,
    showTotal: () => `共${totalElements}条`,
    pageSize,
    total: totalElements,
    current: current,
    onChange: (current: any) => changePage(current),
  };

  return (
    <Fragment>
      <Table
        size="small"
        bordered
        columns={safty_alert_column}
        dataSource={safty_alert_data}
        pagination={paginationProps}
      />
    </Fragment>
  );
};

export default connect(({ geolocationModel }: any) => {
  return { geolocationModel };
})(SaftyAlertListUi);


