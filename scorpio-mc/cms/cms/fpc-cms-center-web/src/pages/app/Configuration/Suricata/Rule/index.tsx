import { getTablePaginationDefaultSettings } from '@/common/app';
import appConfig from '@/common/applicationConfig';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import CustomPagination from '@/components/CustomPagination';
import Import from '@/components/Import';
import InputAndSelect from '@/components/InputAndSelect';
import useQuery from '@/hooks/useQuery';
import type { ConnectState } from '@/models/connect';
import type { IMitreAttack, IRuleClasstype } from '@/pages/app/security/typings';
import { getLinkUrl } from '@/utils/utils';
import {
  DownOutlined,
  ExportOutlined,
  PlusOutlined,
  QuestionCircleOutlined,
  SearchOutlined,
  UploadOutlined,
  UpOutlined,
} from '@ant-design/icons';
import { useSafeState } from 'ahooks';
import {
  Alert,
  Button,
  Col,
  Divider,
  Dropdown,
  Form,
  Input,
  InputNumber,
  Menu,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tooltip,
} from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { stringify } from 'qs';
import { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, useDispatch, useLocation, useSelector } from 'umi';
import {
  RuleDirectionOptions,
  RuleProtocolOptions,
  RuleSignatureSeverityOptions,
  RuleStateOptions,
  RuleTargetOptions,
} from '../common';
import ClasstypeTable from '../components/ClasstypeTable';
import MitreAttackTable from '../components/MitreAttackTable';
import RuleBatch from '../components/RuleBatch';
import {
  changeSuricataRuleState,
  deleteAllSuricataRules,
  deleteSuricataRules,
  disableAllSuricataRules,
  enableAllSuricataRules,
  querySuricataRules,
  querySuricataSource,
} from '../service';
import type { ISuricataRule } from '../typings';
import { ERuleSource } from '../typings';
import styles from './style.less';

const { API_VERSION_PRODUCT_V1, API_BASE_URL } = appConfig;

const SuricataRuleList = () => {
  const {
    query: { sid: sidParams },
  } = useLocation() as unknown as { query: { sid: number } };

  const [form] = Form.useForm();

  // 筛选条件变化时，查询规则
  const [searchParams, setSearchParams] = useState<Record<string, any>>();
  // 保存筛选条件，用户可以根据条件进行批量操作
  const [batchSearch, setBatchSearch] = useState<Record<string, any>>({});

  const [tableData, setTableData] = useState<ISuricataRule[]>([]);
  const [tableLoading, setTableLoading] = useState(false);
  const [tableHeight, setTableHeight] = useState(0);
  const [queryCount, setQueryCount] = useState(0);
  const [currentTacticId, setCurrentTacticId] = useState<string>();
  const [formExpand, setFormExpand] = useState(false);
  const [sources, setSources] = useSafeState<Record<string, string>>({});
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: getTablePaginationDefaultSettings().defaultPageSize!,
  });
  const [total, setTotal] = useState(0);

  const [mitreVisible, setMitreVisible] = useState(false);
  const [classtypeVisible, setClasstypeVisible] = useState(false);
  const [batchVisible, setBatchVisible] = useState(false);
  const [selectedRules, setSelectedRules] = useState<ISuricataRule[]>([]);

  const [initPage, setInitPage] = useState(1);

  const [importRuleCondition, setImportRuleCondition] = useState<{
    source?: string;
    classtypeId?: string;
  }>({});

  const dispatch = useDispatch<Dispatch>();
  // const [isReady, setIsReady] = useState(false);
  const classtypeList = useSelector<ConnectState, IRuleClasstype[]>(
    (state) => state.suricataModel.classtypes,
  );

  const mitreAttacks = useSelector<ConnectState, IMitreAttack[]>(
    (state) => state.suricataModel.mitreAttackList,
  );

  const mitreAttackTree = useMemo(() => {
    const tmp = mitreAttacks
      .filter((item) => !item.parentId)
      .map((item) => {
        return { label: item.name, value: item.id, children: [] as any[] };
      });

    mitreAttacks.forEach((item) => {
      if (item.parentId) {
        const parentIndex = tmp.findIndex((att) => att.value === item.parentId);
        if (parentIndex !== -1) {
          tmp[parentIndex].children.push({ label: item.name, value: item.id });
        }
      }
    });

    return tmp;
  }, [mitreAttacks]);

  const mitreDict = useSelector<ConnectState, Record<string, IMitreAttack>>(
    (state) => state.suricataModel.mitreDict,
  );

  const classtypeDict = useSelector<ConnectState, Record<string, IRuleClasstype>>(
    (state) => state.suricataModel.classtypeDict,
  );

  const importSuricataRuleLoading = useSelector<ConnectState, any>(
    (state) => state.loading.effects['suricataModel/importSuricataRule'],
  );

  const getFormSearch = () => {
    const params: Record<string, any> = form.getFieldsValue();
    const fields = Object.keys(params).filter((field) => params[field]);
    return fields.reduce((prev, currentField) => {
      return {
        ...prev,
        [currentField]:
          currentField === 'classtypeIds' ? params[currentField].join(',') : params[currentField],
      };
    }, {});
  };

  useEffect(() => {
    dispatch({ type: 'suricataModel/querySuricataMitreAttack' });
    dispatch({ type: 'suricataModel/querySuricataRuleClasstype' });
    querySuricataSource().then(({ success, result }) => {
      if (success) {
        setSources(result);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initPage]);

  useEffect(() => {
    form.setFieldsValue({
      sid: sidParams,
    });
  }, [form, sidParams]);

  const columns: ColumnProps<ISuricataRule>[] = useMemo(
    () => [
      {
        title: '协议',
        dataIndex: 'protocol',
        width: 100,
        align: 'center',
      },
      {
        title: '源IP',
        align: 'center',
        dataIndex: 'srcIp',
        width: 100,
      },
      {
        title: '源端口',
        align: 'center',
        dataIndex: 'srcPort',
        width: 100,
      },
      {
        title: '目的IP',
        align: 'center',
        dataIndex: 'destIp',
        width: 100,
      },
      {
        title: '目的端口',
        dataIndex: 'destPort',
        align: 'center',
        width: 100,
      },
      {
        title: '方向',
        dataIndex: 'direction',
        align: 'center',
        render: (_, record) => {
          const { direction } = record;
          return RuleDirectionOptions.find((item) => item.value === direction)?.label;
        },
      },
      {
        title: '规则ID',
        dataIndex: 'sid',
        align: 'center',
        width: 100,
      },
      {
        title: '规则描述',
        dataIndex: 'msg',
        align: 'center',
        width: 250,
      },
      {
        title: '规则分类',
        dataIndex: 'classtypeId',

        align: 'center',

        render: (_, record) => {
          if (record.classtypeId) {
            return classtypeDict[record.classtypeId]?.name || '-';
          }
          return '-';
        },
        width: 150,
      },
      {
        title: '战术分类',
        dataIndex: 'mitreTacticId',
        align: 'center',
        render: (_, record) => {
          if (record.mitreTacticId) {
            return mitreDict[record.mitreTacticId]?.name;
          }
          return '-';
        },
        width: 150,
      },
      {
        title: '技术分类',
        dataIndex: 'mitreTechniqueId',
        align: 'center',
        render: (_, record) => {
          if (record.mitreTechniqueId) {
            return mitreDict[record.mitreTechniqueId]?.name;
          }
          return '-';
        },
        width: 150,
      },
      {
        title: 'CVE',
        dataIndex: 'cve',
        align: 'center',
        ellipsis: true,
        width: 100,
      },
      {
        title: 'CNNVD',
        dataIndex: 'cnnvd',
        align: 'center',
        width: 100,
        ellipsis: true,
      },
      {
        title: '优先级',
        dataIndex: 'priority',
        align: 'center',
        width: 100,
      },
      {
        title: '严重级别',
        dataIndex: 'signatureSeverity',
        align: 'center',
        width: 100,
        render: (_, record) => {
          const { signatureSeverity } = record;
          return RuleSignatureSeverityOptions.find((item) => item.value === signatureSeverity)
            ?.label;
        },
      },
      {
        title: '应用目标',
        dataIndex: 'target',
        align: 'center',
        width: 100,
        render: (_, record) => {
          const { target } = record;
          return RuleTargetOptions.find((item) => item.value === target)?.label;
        },
      },
      {
        title: '状态',
        dataIndex: 'state',
        align: 'center',
        width: 100,

        render: (_, record) => {
          const { state } = record;
          return RuleStateOptions.find((item) => item.value === state)?.label;
        },
      },
      {
        title: '来源',
        dataIndex: 'source',
        align: 'center',
        width: 100,
        render: (_, record) => {
          const { source } = record;
          return sources[source] || '';
        },
      },
      {
        title: '操作',
        key: 'option',
        align: 'center',
        fixed: 'right',
        width: 100,
        render: (_, record) => {
          const { source, sid, id } = record;

          return (
            <Button
              type="link"
              size="small"
              // 系统内置不可编辑,但是sid为1的除外
              disabled={source === ERuleSource.系统内置 && id !== '1'}
              onClick={() => {
                history.push(
                  getLinkUrl(`/configuration/safety-analysis/suricata/rule/${sid}/update`),
                );
              }}
            >
              编辑
            </Button>
          );
        },
      },
    ],
    [classtypeDict, mitreDict, sources],
  );

  const handleFinish = () => {
    setSearchParams(getFormSearch());
  };

  useEffect(() => {
    setTableLoading(true);
    querySuricataRules({
      page: pagination.current - 1,
      pageSize: pagination.pageSize,
      ...searchParams,
    }).then((res) => {
      const { success, result } = res;
      if (success) {
        setTableData(result.content);
        setTotal(result.totalElements);
      }
      setTableLoading(false);
    });
  }, [searchParams, queryCount, pagination.pageSize, pagination]);

  const handleRuleDelete = () => {
    deleteSuricataRules(selectedRules.map((rule) => rule.sid)).then(() => {
      // 删除之后刷新页面
      setQueryCount((prev) => prev + 1);
      setSelectedRules([]);
    });
  };

  const handleRuleEnable = () => {
    changeSuricataRuleState({ sids: selectedRules.map((rule) => rule.sid), state: true }).then(
      () => {
        setQueryCount((prev) => prev + 1);
      },
    );
  };

  const handleRuleDisable = () => {
    changeSuricataRuleState({ sids: selectedRules.map((rule) => rule.sid), state: false }).then(
      () => {
        setQueryCount((prev) => prev + 1);
      },
    );
  };

  const handleHeightChange = (height: number) => {
    setTableHeight(height);
  };

  const handleBatch = () => {
    setBatchSearch(getFormSearch);
    setBatchVisible(true);
  };

  const refreshPage = () => {
    setQueryCount((prev) => prev + 1);
    setInitPage((prev) => prev + 1);
  };

  const [enableAllLoading, handleEnableAll] = useQuery(() => {
    return enableAllSuricataRules().then(() => {
      setQueryCount((prev) => prev + 1);
    });
  });

  const [disableAllLoading, handleDisableAll] = useQuery(() => {
    return disableAllSuricataRules().then(() => {
      setQueryCount((prev) => prev + 1);
    });
  });

  const [deleteAllLoading, handleDelAll] = useQuery(async () => {
    return deleteAllSuricataRules().then(() => {
      setQueryCount((prev) => prev + 1);
    });
  });

  return (
    <div className={styles.ruleList}>
      <AutoHeightContainer
        autoHeight={true}
        onHeightChange={handleHeightChange}
        headerRender={
          <>
            <Form
              form={form}
              onFinish={handleFinish}
              wrapperCol={{ span: 18 }}
              labelCol={{ span: 6 }}
              layout="horizontal"
            >
              <Row gutter={24}>
                <Col span={6}>
                  <Form.Item name="srcIp" label="源IP">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item name="srcPort" label="源端口">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item name="destIp" label="目的IP">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item name="destPort" label="目的端口">
                    <Input />
                  </Form.Item>
                </Col>
              </Row>

              {formExpand && (
                <Row gutter={24}>
                  <Col span={6}>
                    <Form.Item name="protocol" label="协议">
                      <Select options={RuleProtocolOptions} allowClear={true} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="direction" label="方向">
                      <Select options={RuleDirectionOptions} allowClear={true} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="sid" label="规则ID">
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="classtypeIds" label="规则分类">
                      <Select
                        mode="multiple"
                        options={classtypeList.map((item) => ({
                          label: item.name,
                          value: item.id,
                        }))}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item label="ATT&CK分类">
                      <Input.Group>
                        <Form.Item name="mitreTacticIds" noStyle>
                          <Select
                            style={{ width: '50%' }}
                            options={mitreAttackTree}
                            value={currentTacticId}
                            onChange={(value) => {
                              setCurrentTacticId(value);
                              form.setFieldsValue({
                                mitreTechniqueIds: undefined,
                              });
                            }}
                            allowClear
                          />
                        </Form.Item>
                        <Form.Item name="mitreTechniqueIds" noStyle>
                          <Select
                            disabled={currentTacticId === '0' || !currentTacticId}
                            style={{ width: '50%' }}
                            options={
                              mitreAttackTree.find((item) => item.value === currentTacticId)
                                ?.children
                            }
                            allowClear
                          />
                        </Form.Item>
                      </Input.Group>
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="cve" label="CVE编号">
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="cnnvd" label="CNNVD编号">
                      <Input />
                    </Form.Item>
                  </Col>

                  <Col span={6}>
                    <Form.Item name="priority" label="优先级">
                      <InputNumber min={0} max={255} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="signatureSeverity" label="严重级别">
                      <Select options={RuleSignatureSeverityOptions} allowClear={true} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="target" label="受害方">
                      <Select options={RuleTargetOptions} allowClear={true} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="state" label="状态">
                      <Select options={RuleStateOptions} allowClear={true} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item label="来源" name="source">
                      <Select
                        options={Object.keys(sources).map((key) => {
                          return {
                            label: sources[key],
                            value: key,
                          };
                        })}
                        allowClear
                      />
                    </Form.Item>
                  </Col>
                </Row>
              )}

              <Row justify="space-between" gutter={24}>
                <Col span={18} offset={6}>
                  <Row justify="end">
                    <Space align="start">
                      <Button
                        icon={formExpand === true ? <UpOutlined /> : <DownOutlined />}
                        onClick={() => setFormExpand((prev) => !prev)}
                      />
                      <Button key="batch" onClick={handleBatch} disabled={selectedRules.length > 0}>
                        批量操作
                      </Button>
                      <Button key="att&ck" onClick={() => setMitreVisible(true)}>
                        ATT&CK分类
                      </Button>
                      <Input.Group compact style={{ whiteSpace: 'nowrap' }}>
                        <Button
                          style={{ display: 'inline-block' }}
                          onClick={() => setClasstypeVisible(true)}
                        >
                          规则分类
                        </Button>
                        <Tooltip title="新建">
                          <Button
                            type="primary"
                            icon={
                              <PlusOutlined
                                onClick={() =>
                                  history.push(
                                    '/configuration/safety-analysis/suricata/rule/classtype/create',
                                  )
                                }
                              />
                            }
                          />
                        </Tooltip>
                      </Input.Group>
                      <Divider />
                      <Input.Group compact style={{ whiteSpace: 'nowrap' }}>
                        <Popconfirm
                          title="是否确认全部启用"
                          key="allEnabled"
                          onConfirm={handleEnableAll}
                        >
                          <Button loading={enableAllLoading}>全部启用</Button>
                        </Popconfirm>
                        <Popconfirm
                          title="是否确认全部启用"
                          key="allDisabled"
                          onConfirm={handleDisableAll}
                        >
                          <Button loading={disableAllLoading}>全部停用</Button>
                        </Popconfirm>
                        <Popconfirm title="是否删除全部" key="allDelete" onConfirm={handleDelAll}>
                          <Button loading={deleteAllLoading} danger>
                            全部删除
                          </Button>
                        </Popconfirm>
                      </Input.Group>
                      <Input.Group compact>
                        <Dropdown
                          key="import"
                          overlay={
                            <Menu>
                              <Menu.Item key="rule">
                                <Import
                                  loading={importSuricataRuleLoading}
                                  modalTitle="规则导入"
                                  buttonText="检测规则"
                                  extraData={importRuleCondition}
                                  description={
                                    <Space>
                                      <InputAndSelect
                                        style={{ width: 130 }}
                                        options={Object.keys(sources)
                                          .map((key) => {
                                            return {
                                              label: sources[key],
                                              value: key,
                                            };
                                          })
                                          .filter((item) => item.value !== ERuleSource.系统内置)}
                                        placeholder="来源"
                                        onChange={(value) => {
                                          setImportRuleCondition((prev) => {
                                            return {
                                              ...prev,
                                              source: value,
                                            };
                                          });
                                        }}
                                      />
                                      <Select
                                        style={{ width: 160 }}
                                        placeholder="规则分类"
                                        options={classtypeList.map((item) => ({
                                          label: item.name,
                                          value: item.id,
                                        }))}
                                        onChange={(value) => {
                                          setImportRuleCondition((prev) => {
                                            return {
                                              ...prev,
                                              classtypeId: value,
                                            };
                                          });
                                        }}
                                      />
                                    </Space>
                                  }
                                  buttonProps={{
                                    type: 'text',
                                    size: 'small',
                                    block: true,
                                  }}
                                  importFunc="suricataModel/importSuricataRule"
                                  acceptFileTypes={['csv', 'rules']}
                                  tempDownloadUrl="/suricata/rules/as-template"
                                  importSuccessCallback={() => {
                                    refreshPage();
                                    setImportRuleCondition({});
                                  }}
                                />
                              </Menu.Item>
                              <Menu.Item key="classtype">
                                <Import
                                  loading={importSuricataRuleLoading}
                                  modalTitle="规则分类导入"
                                  buttonText="规则分类"
                                  buttonProps={{
                                    type: 'text',
                                    size: 'small',
                                    block: true,
                                  }}
                                  importFunc="suricataModel/importSuricataClasstype"
                                  tempDownloadUrl="/suricata/rule-classtypes/as-template"
                                  importSuccessCallback={() => {
                                    dispatch({ type: 'suricataModel/querySuricataRuleClasstype' });
                                  }}
                                />
                              </Menu.Item>
                            </Menu>
                          }
                        >
                          <Button>
                            <UploadOutlined /> 导入
                          </Button>
                        </Dropdown>
                        <Dropdown
                          key="export"
                          overlay={
                            <Menu>
                              <Menu.Item key="rule">
                                <Button
                                  type="text"
                                  size="small"
                                  block={true}
                                  onClick={() => {
                                    const search = getFormSearch();
                                    window.open(
                                      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/suricata/rules/as-export?${stringify(
                                        search,
                                      )}`,
                                    );
                                  }}
                                >
                                  检测规则
                                </Button>
                              </Menu.Item>
                              <Menu.Item key="classtype">
                                <Button
                                  type="text"
                                  size="small"
                                  block={true}
                                  onClick={() => {
                                    window.open(
                                      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes/as-export`,
                                    );
                                  }}
                                >
                                  规则分类
                                </Button>
                              </Menu.Item>
                            </Menu>
                          }
                        >
                          <Button>
                            <ExportOutlined /> 导出
                          </Button>
                        </Dropdown>
                      </Input.Group>
                      <Button
                        icon={<SearchOutlined />}
                        type="primary"
                        key="query"
                        htmlType="submit"
                      >
                        查询
                      </Button>
                      <Button
                        key="new"
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={() => {
                          history.push(`/configuration/safety-analysis/suricata/rule/create`);
                        }}
                      >
                        新增
                      </Button>
                    </Space>
                  </Row>
                </Col>
              </Row>
            </Form>
            {selectedRules.length > 0 && (
              <Alert
                style={{ marginBottom: 4 }}
                type="info"
                message={
                  <Space size={16}>
                    <Popconfirm
                      title="确定启用吗？"
                      onConfirm={() => handleRuleEnable()}
                      disabled={selectedRules.length === 0}
                      icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                    >
                      <a>批量启用</a>
                    </Popconfirm>
                    <Popconfirm
                      title="确定停用吗？"
                      onConfirm={() => handleRuleDisable()}
                      disabled={selectedRules.length === 0}
                      icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                    >
                      <a style={{ color: 'red' }}>批量停用</a>
                    </Popconfirm>
                    {selectedRules.filter((rule) => rule.source === ERuleSource.系统内置).length <
                      1 && (
                      <a
                        onClick={() => {
                          setBatchSearch({ sids: selectedRules.map((rule) => rule.sid) });
                          setBatchVisible(true);
                        }}
                      >
                        批量修改
                      </a>
                    )}
                    <a
                      onClick={() => {
                        const search = { sids: selectedRules.map((rule) => rule.sid).join(',') };
                        window.open(
                          `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/suricata/rules/as-export?${stringify(
                            search,
                          )}`,
                        );
                      }}
                    >
                      批量导出
                    </a>
                    {selectedRules.filter((rule) => rule.source === ERuleSource.系统内置).length <
                      1 && (
                      <Popconfirm
                        title="确定删除吗？"
                        onConfirm={() => handleRuleDelete()}
                        disabled={selectedRules.length === 0}
                        icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                      >
                        <a style={{ color: 'red' }}>批量删除</a>
                      </Popconfirm>
                    )}
                    <a
                      onClick={() => {
                        setSelectedRules([]);
                      }}
                    >
                      取消选择
                    </a>
                    <span>系统内置规则不可修改！</span>
                  </Space>
                }
              />
            )}
          </>
        }
      >
        <Table<ISuricataRule>
          bordered
          size="small"
          columns={columns}
          dataSource={tableData}
          loading={tableLoading}
          scroll={{ x: 'max-content', y: tableHeight - 75 }}
          rowSelection={{
            type: 'checkbox',
            selectedRowKeys: selectedRules.map((rule) => rule.sid),
            onChange: (_, rows: ISuricataRule[]) => {
              setSelectedRules(rows);
            },
          }}
          rowKey="sid"
          pagination={false}
        />
        <CustomPagination
          currentPage={pagination.current}
          pageSize={pagination.pageSize}
          total={total}
          onChange={function (currentPage: number, pageSize: number): void {
            setPagination((prev) => {
              return {
                ...prev,
                current: currentPage,
                pageSize: pageSize,
              };
            });
          }}
        />
      </AutoHeightContainer>
      {/* att&ck分类 */}
      <Modal
        visible={mitreVisible}
        width={800}
        title="Mitre分类"
        footer={
          <Button
            onClick={() => {
              setMitreVisible(false);
            }}
          >
            关闭
          </Button>
        }
        onCancel={() => {
          setMitreVisible(false);
        }}
      >
        <MitreAttackTable />
      </Modal>
      {/* 规则分类 */}
      <Modal
        visible={classtypeVisible}
        width={1000}
        title={'规则分类'}
        destroyOnClose
        footer={
          <Button
            onClick={() => {
              setClasstypeVisible(false);
            }}
          >
            关闭
          </Button>
        }
        onCancel={() => {
          setClasstypeVisible(false);
        }}
      >
        <ClasstypeTable />
      </Modal>
      <Modal
        visible={batchVisible}
        width={800}
        title="批量操作"
        destroyOnClose
        footer={null}
        closable
        onCancel={() => setBatchVisible(false)}
      >
        <RuleBatch
          search={batchSearch}
          sources={sources}
          onFinish={() => {
            setBatchVisible(false);
            refreshPage();
          }}
        />
      </Modal>
    </div>
  );
};

export default SuricataRuleList;
