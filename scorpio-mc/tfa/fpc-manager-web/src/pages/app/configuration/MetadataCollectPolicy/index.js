import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { getLinkUrl } from '@/utils/utils';
import { PlusOutlined } from '@ant-design/icons';
import { Button, Col, Divider, Popconfirm, Row, Table } from 'antd';
import { Fragment, PureComponent } from 'react';
import { connect, Link } from 'umi';
import styles from './index.less';
import { METADATA_COLLECT_LEVEL_MAP } from './typings';

@connect(({ metadatCollectPolicyModel, metadataModel: { metadataProtocolMap }, loading }) => ({
  metadatCollectPolicyModel,
  metadataProtocolMap,
  loading: loading.models.monitor,
}))
class CollectPolicyList extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'metadatCollectPolicyModel/query',
    });
  }

  handleDelete = ({ id }) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'metadatCollectPolicyModel/delete',
      payload: {
        id,
      },
    });
  };

  handleChangeState = (id, newState) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'metadatCollectPolicyModel/changeState',
      payload: {
        id,
        state: newState,
      },
    });
  };

  render() {
    const {
      metadatCollectPolicyModel: { list },
      metadataProtocolMap,
    } = this.props;

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        align: 'center',
      },
      {
        title: 'IP/IP段',
        dataIndex: 'ipAddress',
        align: 'center',
        width: 250,
        render: (text) => text || 'ALL',
      },
      {
        title: '协议',
        dataIndex: 'l7ProtocolId',
        align: 'center',
        render: (l7ProtocolId) => {
          if (!l7ProtocolId) {
            return '';
          }

          const label = [];
          l7ProtocolId.split(',').forEach((id) => {
            const name = metadataProtocolMap[id] ? metadataProtocolMap[id].nameText : id;
            label.push(name);
          });

          return label.join(',');
        },
      },
      {
        title: '级别',
        dataIndex: 'level',
        align: 'center',
        render: (level) => METADATA_COLLECT_LEVEL_MAP[level] || '低',
      },
      {
        title: '状态',
        dataIndex: 'state',
        align: 'center',
        render: (state) => {
          if (!state) return '--';
          return state === BOOL_YES ? (
            <span className={styles.enable}>已启用</span>
          ) : (
            <span className={styles.disabled}>已禁用</span>
          );
        },
      },
      {
        title: '操作',
        align: 'center',
        dataIndex: 'action',
        width: 150,
        render: (text, record) => {
          const { id, state } = record;
          const stateText = state === BOOL_YES ? '禁用' : '启用';
          const isOpen = state === BOOL_YES;
          const className = state === BOOL_YES ? 'disabled' : 'enable';

          return (
            <Fragment>
              <Link to={getLinkUrl(`/configuration/netflow/metadata/collect-policy/${id}/update`)}>
                编辑
              </Link>
              <Divider type="vertical" />
              <Popconfirm
                title="确定删除吗？"
                disabled={isOpen}
                onConfirm={() => this.handleDelete(record)}
              >
                <a className={isOpen ? 'disabled' : undefined}>删除</a>
              </Popconfirm>
              <Divider type="vertical" />
              <Popconfirm
                title={`确定${stateText}吗？`}
                onConfirm={() => this.handleChangeState(id, isOpen ? BOOL_NO : BOOL_YES)}
              >
                <a className={styles[className]}>{stateText}</a>
              </Popconfirm>
            </Fragment>
          );
        },
      },
    ];

    return (
      <>
        <Row className="mb-10">
          <Col span={10} />
          <Col span={14}>
            <div style={{ textAlign: 'right' }}>
              <Link to={getLinkUrl('/configuration/netflow/metadata/collect-policy/create')}>
                <Button icon={<PlusOutlined />} type="primary">
                  新建
                </Button>
              </Link>
            </div>
          </Col>
        </Row>
        <Table
          bordered
          size="small"
          rowKey="id"
          columns={columns}
          loading={false}
          pagination={false}
          dataSource={list}
        />
      </>
    );
  }
}

export default CollectPolicyList;
