import { abortAjax, enumObj2List } from '@/utils/utils';
import {
  Alert,
  Button,
  Checkbox,
  Col,
  Divider,
  Empty,
  Form,
  Input,
  message,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Tooltip,
  TreeSelect,
} from 'antd';
import { useCallback, useContext, useEffect, useRef, useState } from 'react';
import { connect } from 'dva';
// import { history } from 'umi';
import type { IFileItem } from '../../typing';
import { ETaskMode } from '../../typing';
import { QuestionCircleOutlined, SettingOutlined } from '@ant-design/icons';
import { EditOfflineTabsContext } from '../../OfflineTaskTab';
import { queryOfflineFiles, updatePacpTask } from '../../service';
import { querySendPolicyStateOn } from '@/pages/app/configuration/SendPolicy/service';
import { LoadingOutlined, RedoOutlined } from '@ant-design/icons';
import ConnectCmsState from '@/pages/app/configuration/components/ConnectCmsState';
// import { updatePacpTask } from '../../service';

const FormItem = Form.Item;
const { Option } = Select;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 5 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};

enum EFileType {
  DIR = 'DIR',
  FILE = 'FILE',
}

export const filePathToTree: any = (files: IFileItem[], onlyDir: boolean, path?: string) => {
  return files
    .filter((ele) => {
      return onlyDir ? ele.fileType === EFileType.DIR : true;
    })
    .map((item) => {
      const tmpCompletePath = path ? `${path}/${item.fileName}` : `/${item.fileName}`;
      if (item.child?.length) {
        return {
          ...item,
          title: item.fileName,
          // value: item.key,
          // key: item.key,
          value: item.filePath,
          key: item.filePath,
          completePath: tmpCompletePath,
          children: filePathToTree(item.child, onlyDir, tmpCompletePath),
        };
      }
      return {
        ...item,
        title: item.fileName,
        // value: item.key,
        // key: item.key,
        value: item.filePath,
        key: item.filePath,
        disabled: onlyDir ? false : item.fileType === EFileType.DIR,
        completePath: tmpCompletePath,
      };
    });
};

function CreateForm() {
  const [form] = Form.useForm();
  const [dirMode, setDirMode] = useState(false);
  const [editTabObj] = useContext(EditOfflineTabsContext);

  const [treeData, setTreeData] = useState<any>([]);
  const [treeDataLoading, setTreeDataLoading] = useState(false);
  const [searchFileName, setSearchFileName] = useState<string | undefined>();
  const fileNameTimer = useRef<any>();

  const [sendPolicies, setSendPolicies] = useState<any[]>([]);
  const [policyLoading, setPolicyLoading] = useState<boolean>(false);

  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  const fetchPolicies = async () => {
    setPolicyLoading(true);
    const { success, result } = await querySendPolicyStateOn();
    if (success) {
      setSendPolicies(result);
    }
    setPolicyLoading(false);
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  useEffect(() => {
    abortAjax(['/appliance/packet-file-directory']);
    window.clearTimeout(fileNameTimer.current);
    fileNameTimer.current = setTimeout(() => {
      setTreeDataLoading(true);
      queryOfflineFiles({
        filename: searchFileName,
        count: 1000,
        type: dirMode ? EFileType.DIR : EFileType.FILE,
      }).then((res) => {
        const { success, result } = res;
        setTreeDataLoading(false);
        if (success) {
          result.disabled = true;
          setTreeData([result]);
        }
      });
    }, 500);
  }, [dirMode, searchFileName]);

  const onFinish = useCallback((params: any) => {
    // postPacpTask(e);是否忽略包的时间戳（未选中时，文件中包的时间戳小于已分析的时间戳，此包将不处理；选中时时间戳被忽略，将由使用分析时的时间）
    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        updatePacpTask({
          ...params,
          sendPolicyIds: params?.sendPolicyIds?.join(',') || '',
          filePath: JSON.stringify(
            Array.isArray(params.filePath) ? params.filePath : [params.filePath],
          ),
          configuration: JSON.stringify({
            localTimeAction: params.ignorePacketTimeStamp ? true : false,
            localCacheAction: params.joinLocalCache ? true : false,
          }),
        }).then((res) => {
          if (res.success) {
            // Modal.success({
            //   keyboard: false,
            //   title: '保存成功',
            //   okText: '返回列表页',
            //   onOk: () => {
            //     history.goBack();
            //   },
            // });
            message.info('新建成功');
          }
        });
      },
    });
  }, []);

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <Form form={form} onFinish={onFinish}>
        <FormItem
          {...formItemLayout}
          label="离线分析任务名"
          name="name"
          // extra="设备名称主要用于显示，便于管理"
          rules={[
            { required: true, message: '请填写离线分析任务名' },
            { max: 32, message: '最多可输入32个字符' },
          ]}
        >
          <Input placeholder="请填写离线分析任务名" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="任务模式"
          name="mode"
          // extra="设备名称主要用于显示，便于管理"
          rules={[
            {
              required: true,
              message: '请选择任务模式',
            },
          ]}
        >
          <Select
            placeholder="请选择任务模式"
            onChange={(e) => {
              setDirMode(!e.includes('MULTIPLE_FILES'));
              form.setFieldsValue({
                // 清空数据源
                filePath: undefined,
              });
            }}
          >
            {enumObj2List(ETaskMode).map((item) => (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        </FormItem>
        <Form.Item label="外发策略" {...formItemLayout}>
          <Space>
            <Form.Item name="sendPolicyIds" noStyle>
              <Select
                mode="multiple"
                placeholder="请选择外发策略"
                style={{ width: '45vw' }}
                dropdownRender={(menu) => {
                  return (
                    <>
                      {menu}
                      <Divider style={{ margin: '0px' }} />
                      <Button
                        style={{ margin: '5px' }}
                        onClick={() => {
                          fetchPolicies();
                        }}
                        size="small"
                        type="link"
                      >
                        刷新
                      </Button>
                    </>
                  );
                }}
                disabled={cmsConnectFlag}
              >
                {sendPolicies.map((policy) => {
                  return <Option value={policy.id}>{policy.name}</Option>;
                })}
              </Select>
            </Form.Item>
            <Button
              type="link"
              onClick={() => {
                window.open('/#/configuration/third-party/send-policy/create');
              }}
              disabled={cmsConnectFlag}
            >
              新建外发策略
            </Button>
          </Space>
        </Form.Item>

        <FormItem
          {...formItemLayout}
          label={
            <>
              <Tooltip title="当前文件列表只展示部分结果，未展示出的文件请输入关键字搜索">
                选择需要分析的文件
                <QuestionCircleOutlined />
              </Tooltip>
            </>
          }
          name="filePath"
          // extra="设备名称主要用于显示，便于管理"
          rules={[
            {
              required: true,
              message: '选择需要分析的文件',
            },
          ]}
        >
          <TreeSelect
            placeholder="当前文件列表只展示部分结果，未展示出的文件请输入关键字搜索"
            treeData={filePathToTree(treeData, dirMode)}
            onSearch={(text) => {
              setSearchFileName(text);
            }}
            treeDefaultExpandAll
            treeCheckable={!dirMode}
            showSearch
            notFoundContent={(() => {
              return treeDataLoading ? (
                <Spin tip="Loading...">
                  <Alert
                    message="正在读取文件目录"
                    description="当前文件数量较大，获取文件目录时间可能为数分钟，建议清理目录中不需要的文件"
                    type="info"
                  />
                </Spin>
              ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
              );
            })()}
          />
        </FormItem>
        <Row>
          <Col span={5} />
          <Col span={19}>
            <Form.Item
              {...formItemLayout}
              name="ignorePacketTimeStamp"
              valuePropName="checked"
              rules={[]}
            >
              <Checkbox>
                {
                  '忽略包的时间戳（未选中时，文件中包的时间戳小于已分析的时间戳，此包将不处理；选中时时间戳被忽略，将使用分析时的时间）'
                }
              </Checkbox>
            </Form.Item>
          </Col>
        </Row>
        <Row>
          <Col span={5} />
          <Col span={19}>
            <Form.Item {...formItemLayout} name="joinLocalCache" valuePropName="checked" rules={[]}>
              <Checkbox>{'数据包本地缓存'}</Checkbox>
            </Form.Item>
          </Col>
        </Row>
        <FormItem wrapperCol={{ span: 12, offset: 4 }} style={{ textAlign: 'center' }}>
          <Button className="mr-10" type="primary" htmlType="submit" loading={false}>
            保存
          </Button>
          <Button
            onClick={() => {
              // history.goBack();
              editTabObj.current.remove(editTabObj.current.state.activeKey);
            }}
          >
            关闭
          </Button>
        </FormItem>
      </Form>
    </>
  );
}
export default connect()(CreateForm);
