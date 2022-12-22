import { Field } from '@/components/Charts';
import Ellipsis from '@/components/Ellipsis';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { parseArrayJson } from '@/utils/utils';
import { Collapse, Modal, Tooltip } from 'antd';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataDns } from '../typings';
import { DOMAIN_CATEGORY, EMetadataProtocol } from '../typings';

const { Panel } = Collapse;

const DNS_UNKOWN_RCODE = '-1';

const DOMAIN_CATEGORY_TEXT = {
  [DOMAIN_CATEGORY.block]: '黑',
  [DOMAIN_CATEGORY.allow]: '白',
  [DOMAIN_CATEGORY.unknown]: '未知',
};

export const tableColumns: IColumnProps<IMetadataDns>[] = [
  {
    title: 'DNS类型',
    dataIndex: 'dnsType',
    render: (_, record) => {
      const bodys = parseArrayJson(record.dnsQueries);
      if (bodys.length === 0) {
        return '';
      }

      const typeNames = bodys.map((item: any) => item.type_name);

      return (
        <Ellipsis tooltip lines={1}>
          {typeNames.join(',')}
        </Ellipsis>
      );
    },
  },
  {
    title: 'DNS协议返回码',
    dataIndex: 'dnsRcode',
    render: (dnsRcode, record) =>
      dnsRcode !== DNS_UNKOWN_RCODE && <Tooltip title={record.dnsRcodeName}>{dnsRcode}</Tooltip>,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: 'DNS协议返回码名称',
    dataIndex: 'dnsRcodeName',
    searchable: true,
  },
  {
    title: 'DNS查询内容',
    dataIndex: 'dnsQueries',
    searchable: true,
    render: (dnsQueries) => {
      const dnsQueriesParse = parseArrayJson(dnsQueries);
      if (dnsQueriesParse.length === 0) {
        return null;
      }

      return (
        <span
          onClick={() => {
            Modal.info({
              title: 'DNS查询内容',
              width: 600,
              okText: '关闭',
              icon: null,
              content: (
                <Collapse bordered={false}>
                  {dnsQueriesParse.map((item: any) => (
                    <Panel header={item.domain} key={item.domain}>
                      <Field label="[查询域名]" value={item.domain} />
                      <Field label="[查询域名长度]" value={item.len} />
                      <Field label="[查询域名的香农阈值]" value={item.entropy} />
                      <Field label="[查询类值]" value={item.class} />
                      <Field label="[查询类值名称]" value={item.class_name} />
                      <Field label="[查询类型]" value={item.type} />
                      <Field label="[查询类型名称]" value={item.type_name} />
                      <Field label="[是否符合RFC规范]" value={item.valid ? '是' : '否'} />
                      <Field label="[是否是常见的恶意域名]" value={item.abues ? '是' : '否'} />
                    </Panel>
                  ))}
                </Collapse>
              ),
            });
          }}
        >
          [点击查看详情]
        </span>
      );
    },
  },
  {
    title: 'DNS应答',
    dataIndex: 'answer',
    searchable: true,
    render: (answer) => {
      const answerParse = parseArrayJson(answer);
      if (answerParse.length === 0) {
        return null;
      }
      return (
        <span
          onClick={() => {
            Modal.info({
              title: 'DNS应答',
              width: 600,
              okText: '关闭',
              icon: null,
              content: (
                <Collapse bordered={false}>
                  {answerParse.map((item: any) => (
                    <Panel header={item.name} key={item.name}>
                      {Object.keys(item).map((key) => (
                        <Field key={key} label={`[${key}]`} value={item[key]} />
                      ))}
                    </Panel>
                  ))}
                </Collapse>
              ),
            });
          }}
        >
          [点击查看详情]
        </span>
      );
    },
  },
  {
    title: '域名',
    dataIndex: 'domain',
    searchable: true,
  },
  {
    dataIndex: 'domainIntelligence',
    title: '域名分类',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(DOMAIN_CATEGORY_TEXT).map((value) => {
      return {
        text: DOMAIN_CATEGORY_TEXT[value],
        value,
      };
    }),
    render: (dom, record) => {
      const { domainIntelligence } = record;
      return DOMAIN_CATEGORY_TEXT[domainIntelligence] || domainIntelligence;
    },
  },
  {
    title: '子域名数量',
    dataIndex: 'subdomainCount',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '域名解析地址',
    dataIndex: 'domainAddress',
    width: 200,
    fieldType: EFieldType.ARRAY,
    render: (text = []) => {
      return (
        <Ellipsis tooltip lines={1}>
          {text.join(',')}
        </Ellipsis>
      );
    },
  },
  // 可搜索，但是不在表格里面显示的字段
  {
    title: '域名解析地址IPv4',
    dataIndex: 'domainIpv4',
    show: false,
    searchable: true,
    fieldType: EFieldType['ARRAY<IPv4>'],
    operandType: EFieldOperandType.IPV4,
  },
  {
    title: '域名解析地址IPv6',
    dataIndex: 'domainIpv6',
    show: false,
    searchable: true,
    fieldType: EFieldType['ARRAY<IPv6>'],
    operandType: EFieldOperandType.IPV6,
  },
];

interface Props {
  paneTitle?: string;
}

const MetadataDns = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.DNS}
      tableColumns={tableColumns as any}
    />
  );
};

export default MetadataDns;
