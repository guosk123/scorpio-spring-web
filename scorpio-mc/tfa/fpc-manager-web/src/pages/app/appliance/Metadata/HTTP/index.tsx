import { EHttpAuthType, HTTP_AUTH_TYPE_LIST, OS_VERSION_LIST } from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IpAddressGroup } from '@/pages/app/configuration/IpAddressGroup/typings';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { getDvaApp, history, useDispatch } from 'umi';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import { beautifyText } from '../Mail';
import type { IMetadataHttp } from '../typings';
import { EMetadataProtocol, FILE_FLAG_MAP, HTTP_DECRYPTED_MAP, LORToText } from '../typings';
import { getEntryTag } from '../utils/entryTools';

export const tableColumns: () => IColumnProps<IMetadataHttp>[] = () => {
  const { allIpAddressGroupList, allIpAddressGroupMap } =
    getDvaApp()._store.getState().ipAddressGroupModel;

  const ipAddressGroupList = (allIpAddressGroupList as IpAddressGroup[])
    .map((data) => ({
      text: data.name,
      value: data.id,
    }))
    .concat({
      text: '其他',
      value: '',
    });

  return [
    {
      title: 'URL',
      dataIndex: 'uri',
      searchable: true,
      width: 300,
      render: (text) => beautifyText(text),
    },
    {
      title: '请求方法',
      dataIndex: 'method',
      searchable: true,
    },
    {
      title: 'status',
      dataIndex: 'status',
      searchable: true,
    },
    {
      title: 'Accept-Encoding',
      dataIndex: 'acceptEncoding',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'Accept-Language',
      dataIndex: 'acceptLanguage',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'cookie',
      dataIndex: 'cookie',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'host',
      dataIndex: 'host',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'origin',
      dataIndex: 'origin',
      searchable: true,
    },
    {
      title: 'referer',
      dataIndex: 'referer',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'HTTP Proxy 连接状态',
      dataIndex: 'channelState',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: Object.keys(LORToText).map((key) => ({
        text: LORToText[key],
        value: key,
      })),
      render: (text) => {
        return LORToText[String(text)];
      },
    },
    {
      title: 'xff',
      dataIndex: 'xff',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: '头部xff',
      dataIndex: 'xffFirst',
      searchable: true,
      operandType: EFieldOperandType.IP,
      fieldType: EFieldType.IP,
    },
    {
      title: '尾部xff',
      dataIndex: 'xffLast',
      searchable: true,
      operandType: EFieldOperandType.IP,
      fieldType: EFieldType.IP,
    },
    {
      title: '头部xff-地址组',
      dataIndex: 'xffFirstAlias',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: ipAddressGroupList,
      render: (xffFirstAlias) => {
        return allIpAddressGroupMap[xffFirstAlias]?.name || xffFirstAlias || '其他';
      },
    },
    {
      title: '尾部xff-地址组',
      dataIndex: 'xffLastAlias',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: ipAddressGroupList,
      render: (xffLastAlias) => {
        return allIpAddressGroupMap[xffLastAlias]?.name || xffLastAlias || '其他';
      },
    },
    {
      title: 'HTTP请求头',
      dataIndex: 'requestHeader',
      searchable: true,
      render: (text) => {
        if (!text) {
          return '';
        }
        return beautifyText(text);
      },
    },
    {
      title: 'Content-Type',
      dataIndex: 'contentType',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'User-Agent',
      dataIndex: 'userAgent',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: '操作系统',
      dataIndex: 'osVersion',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: OS_VERSION_LIST.map((version) => ({
        text: version,
        value: version,
      })),
    },
    {
      title: '重定向地址',
      dataIndex: 'location',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: 'set_cookie',
      dataIndex: 'setCookie',
      searchable: true,
      render: (text) => beautifyText(text),
    },

    {
      title: '文件传输方式',
      dataIndex: 'fileFlag',
      render: (fileFlag) => FILE_FLAG_MAP[fileFlag],
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: Object.keys(FILE_FLAG_MAP).map((key) => ({
        text: FILE_FLAG_MAP[key],
        value: key,
      })),
    },
    {
      title: '传输文件名称',
      dataIndex: 'fileName',
      searchable: true,
    },
    {
      title: '传输文件类型',
      dataIndex: 'fileType',
      searchable: true,
    },
    {
      title: 'Authorization',
      dataIndex: 'authorization',
      searchable: true,
      render: (text) => beautifyText(text),
    },
    {
      title: '认证方式',
      dataIndex: 'authType',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: HTTP_AUTH_TYPE_LIST,

      render: (authType) => EHttpAuthType[authType] || authType,
    },
    {
      title: '加密方式',
      dataIndex: 'decrypted',
      align: 'center',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: Object.keys(HTTP_DECRYPTED_MAP).map((key) => ({
        text: HTTP_DECRYPTED_MAP[key],
        value: key,
      })),
      render: (decrypted) => HTTP_DECRYPTED_MAP[decrypted],
    },
  ];
};

const MetadataHttp = () => {
  const { pathname } = history.location;

  const dispatch = useDispatch<Dispatch>();
  useEffect(() => {
    dispatch({
      type: 'ipAddressGroupModel/queryAllIpAddressGroup',
    });
  }, [dispatch]);

  return (
    <Template<IMetadataHttp>
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.HTTP}
      tableColumns={tableColumns()}
    />
  );
};

export default MetadataHttp;
