import {
  EModelAlias,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import type { ICountry } from '@/pages/app/Configuration/Geolocation/typings';
import type { ApplicationItem } from '@/pages/app/Configuration/SAKnowledge/typings';
import { ip2number, ipV4Regex, ipV6Regex, isCidr } from '@/utils/utils';
import { Form, Input, Select, TreeSelect } from 'antd';
import { connect } from 'dva';
import { Address6 } from 'ip-address';
import type { Rule } from 'rc-field-form/lib/interface';
import type { CSSProperties } from 'react';
import { useState } from 'react';
import { useMemo } from 'react';
import { EDimensionsSearchType } from '../../../../typing';
import SeartchSelect from '../SeartchSelect';

const { Option } = Select;
interface Props {
  style?: CSSProperties | undefined;
  searchType?: string;
  contentRule?: any;
  allCountryList: ICountry[];
  allApplicationList: ApplicationItem[];
}
const FormItem = Form.Item;

function ContentBox(props: Props) {
  const { searchType, style, allCountryList, allApplicationList } = props;
  const [contentRule, setContentRule] = useState<any>([{ required: true, message: '请输入值' }]);
  const content = useMemo(() => {
    let tmpRenderDom = <Input style={style} disabled placeholder={'不可用'} />;
    setContentRule([{ required: true, message: '请输入值' }]);
    if (searchType === EDimensionsSearchType.IPADDRESS) {
      const rules: Rule[] = [
        {
          validator: async (rule, value) => {
            if (!value) {
              throw new Error('请输入值');
            }
            if (
              !ipV4Regex.test(value) &&
              !isCidr(value, 'IPv4') &&
              !ipV6Regex.test(value) &&
              !isCidr(value, 'IPv6')
            ) {
              throw new Error('请输入正确的IPv4或IPv6地址');
            }
          },
          required: true,
        },
      ];
      setContentRule(rules);
      tmpRenderDom = <Input style={style} />;
    } else if (searchType === EDimensionsSearchType.APPLICATION) {
      tmpRenderDom = (
        <SeartchSelect
          style={style}
          key={EDimensionsSearchType.APPLICATION}
          listItems={allApplicationList.map((item) => ({
            text: item.nameText,
            value: item.applicationId,
          }))}
        />
      );
    } else if (searchType === EDimensionsSearchType.L7PROTOCOLID) {
      const l7Protocol = getEnumValueFromModelNext(EModelAlias.l7protocol)?.list || [];
      tmpRenderDom = (
        <SeartchSelect
          style={style}
          key={EDimensionsSearchType.L7PROTOCOLID}
          listItems={l7Protocol.map((item) => ({
            text: item.text,
            value: item.value,
          }))}
        />
      );
    } else if (searchType === EDimensionsSearchType.PORT) {
      const portRules: Rule[] = [
        {
          validator: async (rule, value) => {
            if (isNaN(value)) {
              throw new Error('请正确的端口值');
            }
            if (value < 1 || value > 65535) {
              throw new Error('请输入正确的范围内的端口值!');
            }
          },
          required: true,
        },
      ];
      setContentRule(portRules);
      tmpRenderDom = <Input style={style} />;
    } else if (searchType === EDimensionsSearchType.IPCONVERSATION) {
      const ipConversationRules: Rule[] = [
        {
          validator: async (rule, value) => {
            if (!value) {
              throw new Error('请输入值');
            }
            const ips = value?.split('-');
            if (ips.length !== 2) {
              throw new Error('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
            }
            ips.forEach((ip: string) => {
              if (
                !ipV4Regex.test(ip) &&
                !isCidr(ip, 'IPv4') &&
                !ipV6Regex.test(ip) &&
                !isCidr(ip, 'IPv6')
              ) {
                throw new Error('请输入正确的IPv4或IPv6地址');
              }
            });
            const isDuplicate = (arr: string) => {
              return new Set(arr).size != arr.length;
            };
            if (isDuplicate(ips)) {
              throw new Error('请输入两个不同的IP地址');
            }
          },
          required: true,
        },
      ];
      setContentRule(ipConversationRules);
      tmpRenderDom = <Input style={style} />;
    } else if (searchType === EDimensionsSearchType.IPSEGMENT) {
      const rules: Rule[] = [
        {
          validator: async (rule, value) => {
            const item = value;
            const ips = item?.split('-');
            if (!item) {
              throw new Error('请输入值');
            }
            if (ips.length !== 2) {
              throw new Error('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
            }

            const [ip1, ip2] = ips;

            // 2个IPv4
            if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
              const ip1Number = ip2number(ip1);
              const ip2Number = ip2number(ip2);

              // 起止地址是否符合大小要求
              if (ip1Number >= ip2Number) {
                throw new Error('IP地址段范围错误');
              }
            }

            // 2个IPv6
            else if (ipV6Regex.test(ip1) && ipV6Regex.test(ip2)) {
              if (new Address6(ip1).bigInteger() >= new Address6(ip2).bigInteger()) {
                throw new Error('IP地址段范围错误');
              }
            } else {
              throw new Error('请输入正确的IP地址段');
            }
          },
          required: true,
        },
      ];
      setContentRule(rules);
      tmpRenderDom = <Input style={style} />;
    } else if (searchType === EDimensionsSearchType.LOCATION) {
      tmpRenderDom = <TreeSelect treeData={allCountryList} style={style} />;
    }
    return tmpRenderDom;
  }, [allApplicationList, allCountryList, searchType, style]);
  return (
    <FormItem name="content" rules={contentRule} style={style}>
      {content}
    </FormItem>
  );
}

export default connect((state: any) => {
  const {
    geolocationModel: { allCountryList },
    SAKnowledgeModel: { allApplicationList },
  } = state;
  return { allCountryList, allApplicationList };
})(ContentBox);
