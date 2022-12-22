import {
  ESslAuthType,
  ESslReuseTag,
  SEC_PROTO,
  SEC_PROTO_LIST,
  SSL_AUTH_TYPE_LIST,
  SSL_REUSE_LIST,
} from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataSsl } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';
import { beautifyText, ellipsisText } from '../Mail';

// export use for IOC
export const tableColumns: IColumnProps<IMetadataSsl>[] = [
  {
    title: '服务器名称',
    dataIndex: 'serverName',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '客户端指纹',
    dataIndex: 'ja3Client',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '服务端指纹',
    dataIndex: 'ja3Server',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: 'SSL版本',
    dataIndex: 'version',
    searchable: true,
  },
  {
    title: 'SSL加密套件',
    dataIndex: 'cipherSuite',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '证书签名算法',
    dataIndex: 'signatureAlgorithm',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '证书发布者',
    dataIndex: 'issuer',
    searchable: true,
    render: (text) => ellipsisText(text),
  },
  {
    title: '证书使用者',
    dataIndex: 'commonName',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '证书有效期',
    dataIndex: 'validity',
    searchable: true,
    render: (text) => ellipsisText(text),
  },
  {
    title: '证书状态服务器',
    dataIndex: 'ocspUrls',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '颁发者证书链接',
    dataIndex: 'issuerUrls',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '证书吊销列表链接',
    dataIndex: 'crlUrls',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '认证方式',
    dataIndex: 'authType',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: SSL_AUTH_TYPE_LIST,
    render: (authType) => ESslAuthType[authType] || authType,
  },
  {
    title: '证书指纹',
    dataIndex: 'serverCertsSha1',
    fieldType: EFieldType.ARRAY,
    searchable: true,
    render: (text) => ellipsisText(text.join(',')),
  },
  {
    title: '会话复用',
    dataIndex: 'isReuse',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: SSL_REUSE_LIST,
    render: (reuse) => ESslReuseTag[reuse] || reuse,
  },
  {
    title: 'DTLS标识',
    dataIndex: 'secProto',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: SEC_PROTO_LIST,
    render: (reuse) => SEC_PROTO[reuse] || reuse,
  },
];

const MetadataSsl = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.SSL}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataSsl;
