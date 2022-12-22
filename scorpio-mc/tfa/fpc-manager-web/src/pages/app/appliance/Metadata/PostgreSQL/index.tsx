import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const tableColumns = [
  {
    title: '用户名',
    dataIndex: 'username',
    searchable: true,
  },
  // {
  //   title: '密码',
  //   dataIndex: 'password',
  // },
  {
    title: '数据库名称',
    dataIndex: 'databaseName',
    searchable: true,
  },
  {
    title: 'sql命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '错误',
    dataIndex: 'error',
    searchable: true,
  },
  {
    title: '响应时间(ms)',
    dataIndex: 'delaytime',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  // {
  //   title: 'sql语句参数',
  //   dataIndex: 'parameters',
  // },
  // {
  //   title: 'sql语句value',
  //   dataIndex: 'parametersValue',
  // },
  // {
  //   title: '执行结果',
  //   dataIndex: 'result',
  // },
  // {
  //   title: '服务器名称',
  //   dataIndex: 'serverName',
  // },
  // {
  //   title: '客户端指纹',
  //   dataIndex: 'ja3Client',
  // },
  // {
  //   title: '服务端指纹',
  //   dataIndex: 'ja3Server',
  // },
  // {
  //   title: 'SSL版本',
  //   dataIndex: 'sslVersion',
  // },
  // {
  //   title: 'SSL加密套件',
  //   dataIndex: 'cipherSuite',
  // },
  // {
  //   title: '证书签名算法',
  //   dataIndex: 'signatureAlgorithm',
  // },
  // {
  //   title: '证书发布者',
  //   dataIndex: 'issuer',
  // },
  // {
  //   title: '证书使用者',
  //   dataIndex: 'commonName',
  // },
  // {
  //   title: '证书有效期',
  //   dataIndex: 'validity',
  // },
];

const MetadataPostgreSQL = () => {
  const { pathname } = history.location;
  return <Template entry = {getEntryTag(pathname)} protocol={EMetadataProtocol.POSTGRESQL} tableColumns={tableColumns} />;
};

export default MetadataPostgreSQL;
