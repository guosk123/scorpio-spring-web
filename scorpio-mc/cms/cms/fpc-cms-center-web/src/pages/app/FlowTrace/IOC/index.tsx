import { domainReg, isCidrWithoutType, isIpAddress, isUrl } from '@/utils/utils';
import { Button, Col, Divider, Form, Input, message, Row, Tabs, TreeSelect } from 'antd';
import type { ChangeEventHandler, ReactNode } from 'react';
import { createContext, useEffect, useMemo, useState } from 'react';
import { EMetadataProtocol } from '../../appliance/Metadata/typings';
import { queryNetWorkTree } from '../../Network/service';
import type { INetworkTreeItem } from '../../Network/typing';
import DeferedFlowRecord from './components/DeferedFlowRecord';
import DNS from './components/Metadata/DNS';
import File from './components/Metadata/File';
import HTTP from './components/Metadata/HTTP';
import Mail from './components/Metadata/Mail';
import SSL from './components/Metadata/SSL';
import styles from './index.less';

const { TabPane } = Tabs;

export const FLOWRECORD = 'flow-record';
export const METADATA_PREFIX = 'metadata';

const isDomain = (v: string) => {
  return domainReg.test(v);
};

const md5_reg = /^[A-Fa-f0-9]{32}$/;
const sha1_reg = /^[A-Fa-f0-9]{40}$/;
const sha256_reg = /^[A-Fa-f0-9]{64}$/;

export const isMd5 = (ja3: string) => {
  return md5_reg.test(ja3);
};

export const isSha1 = (v: string) => {
  return sha1_reg.test(v);
};

export const isSha256 = (v: string) => {
  return sha256_reg.test(v);
};

export const isFileName = (v: string) => {
  return isSha256(v) || isSha1(v) || isMd5(v) || !/[\\\\/:*?\"<>|]/.test(v);
};

type SearchFieldKey = 'flow-record' | `metadata-${EMetadataProtocol}`;

export enum ETabs {
  'ipAddress' = 'ipAddress',
  'domain' = 'domain',
  'mail' = 'mail',
  'url' = 'url',
  'ja3' = 'ja3',
  'certificateFingerprint' = 'certificateFingerprint',
  'fileName' = 'fileName',
}

const tabs = [
  { key: ETabs.ipAddress, title: 'IP地址' },
  { key: ETabs.domain, title: '域名' },
  { key: ETabs.mail, title: '邮箱' },
  { key: ETabs.url, title: 'URL' },
  { key: ETabs.ja3, title: 'JA3' },
  { key: ETabs.certificateFingerprint, title: '证书指纹' },
  { key: ETabs.fileName, title: '文件名' },
];

const validator: Record<
  ETabs,
  { method: (search: string) => boolean; message: string; placeholder?: string }
> = {
  [ETabs.ipAddress]: {
    method: (ip: string) => {
      return isIpAddress(ip) || isCidrWithoutType(ip);
    },
    message: '请填写合法的ipv4或者ipv6地址，或者ip地址段',
  },
  [ETabs.domain]: { method: isDomain, message: '请填写合规的域名，example: baidu.com' },
  [ETabs.mail]: {
    method: (value) => /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(value),
    message: '请填写合法的邮箱地址，我们会使用模糊匹配进行检索',
    placeholder: '请输入邮箱，以进行模糊搜索',
  },
  [ETabs.url]: { method: isUrl, message: '请填写合法url, example: http://baidu.com' },
  [ETabs.ja3]: { method: isMd5, message: '请输入32位的16进制串' },
  [ETabs.certificateFingerprint]: {
    method: isSha1,
    message: '请输入40位16进制串',
  },
  [ETabs.fileName]: {
    method: isFileName,
    message: '可输入md5/sha1/sha256/文件名，文件名不能包含/:*?"<>|中的任意字符',
  },
};

export const tabContent: Record<ETabs, ReactNode[]> = {
  [ETabs.ipAddress]: [<DeferedFlowRecord key="flow-record" />],
  [ETabs.domain]: [<HTTP key="http" />, <DNS key="dns" />, <SSL key="ssl" />],
  [ETabs.mail]: [<Mail key="mail" />],
  [ETabs.url]: [<HTTP key="http" />],
  [ETabs.ja3]: [<SSL key="ssl" />],
  [ETabs.certificateFingerprint]: [<SSL key="ssl" />],
  [ETabs.fileName]: [<File key="file" />],
};

export const searchField: Record<ETabs, Partial<Record<SearchFieldKey, string[]>>> = {
  [ETabs.ipAddress]: { [FLOWRECORD]: ['ip_initiator', 'ip_responder'] },
  [ETabs.domain]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.HTTP}`]: ['host'],
    [`${METADATA_PREFIX}-${EMetadataProtocol.SSL}`]: ['server_name'],
    [`${METADATA_PREFIX}-${EMetadataProtocol.DNS}`]: ['domain'],
  },
  [ETabs.mail]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.MAIL}`]: ['from', 'to'],
  },
  [ETabs.url]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.HTTP}`]: ['uri'],
  },
  [ETabs.ja3]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.SSL}`]: ['ja3_client', 'ja3_server'],
  },
  [ETabs.certificateFingerprint]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.SSL}`]: ['server_certs_sha1'],
  },
  [ETabs.fileName]: {
    [`${METADATA_PREFIX}-${EMetadataProtocol.FILE}`]: ['name', 'md5', 'sha1', 'sha256'],
  },
};

export const IOCContext = createContext<{
  networkId: string[];
  search?: { value: string; isValid: boolean };
  currentTab: ETabs;
}>({ currentTab: ETabs.ipAddress, networkId: [] });

const IOC = () => {
  const [activeTab, setActiveTab] = useState<ETabs>(tabs[0].key);
  const [search, setSearch] = useState<{ value: string; isValid: boolean }>({
    value: '',
    isValid: true,
  });
  const [networkId, setNetworkId] = useState<string[]>([]);

  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);
  const [contentVisible, setContentVisible] = useState(false);
  const [form] = Form.useForm();

  const handleSearchChange: ChangeEventHandler<HTMLInputElement> = (e) => {
    setContentVisible(false);
    form.setFieldsValue({ value: e.target.value.trim() });
    setSearch((prev) => {
      return {
        ...prev,
        value: e.target.value.trim(),
        isValid: validator[activeTab].method(e.target.value),
      };
    });
  };

  const handleTabChange = (nextTab: string) => {
    setActiveTab(nextTab as ETabs);
    setContentVisible(false);
    form.setFieldsValue({ value: '' });
    setSearch({ value: '', isValid: true });
  };

  const handleQuery = () => {
    setContentVisible(true);
  };

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(
          result.map((item) => ({
            ...item,
            value: `${item.value}`,
          })),
        );
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  }, []);

  const content = useMemo(() => {
    if (search.isValid && search.value !== '' && contentVisible) {
      return tabContent[activeTab];
    }
    return null;
  }, [activeTab, contentVisible, search.isValid, search.value]);

  return (
    <div>
      <Form form={form}>
        <Tabs activeKey={activeTab} onChange={handleTabChange}>
          {tabs.map((tab) => {
            return <TabPane tab={tab.title} key={tab.key} />;
          })}
        </Tabs>

        <Input.Group className={styles.fullWidth}>
          <Row>
            <Col span={5}>
              <TreeSelect
                onChange={(value) => {
                  if (value.includes('ALL')) {
                    setNetworkId([]);
                  } else {
                    setNetworkId(value || []);
                  }
                }}
                allowClear
                treeDefaultExpandAll
                treeData={[
                  {
                    title: '全选',
                    value: 'ALL',
                    key: 'ALL',
                    children: networkDataSet,
                  },
                ]}
                treeCheckable={true}
                loading={networkTreeLoading}
                placeholder={'请选择网络'}
                showCheckedStrategy={'SHOW_PARENT'}
                style={{ width: '100%' }}
              />
            </Col>
            <Col span={5}>
              <Form.Item
                name="value"
                validateTrigger={['onChange']}
                rules={[
                  {
                    validator(_, value) {
                      if (validator[activeTab].method(value)) {
                        return Promise.resolve();
                      }
                      return Promise.reject(validator[activeTab].message);
                    },
                  },
                ]}
              >
                <Input
                  placeholder={validator[activeTab].message}
                  {...(search.isValid ? {} : { status: 'error' })}
                  value={search.value}
                  onChange={handleSearchChange}
                />
              </Form.Item>
            </Col>
            <Col span={2}>
              <Button type="primary" onClick={handleQuery}>
                查询
              </Button>
            </Col>
          </Row>
        </Input.Group>
      </Form>
      <Divider style={{ marginTop: 0, marginBottom: 6 }} />
      <IOCContext.Provider value={{ networkId, search, currentTab: activeTab }}>
        {content}
      </IOCContext.Provider>
    </div>
  );
};

export default IOC;
