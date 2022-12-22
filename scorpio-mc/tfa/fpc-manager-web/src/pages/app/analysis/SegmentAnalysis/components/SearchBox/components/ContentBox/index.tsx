import network from '@/models/app/network';
import type { ApplicationItem } from '@/pages/app/configuration/SAKnowledge/typings';
import { queryAllServices } from '@/pages/app/configuration/Service/service';
import type { IService } from '@/pages/app/configuration/Service/typings';
import { ipV4Regex, ipV6Regex, isCidr } from '@/utils/utils';
import { Form, Input } from 'antd';
import { connect } from 'dva';
import type { Rule } from 'rc-field-form/lib/interface';
import type { CSSProperties } from 'react';
import { useEffect } from 'react';
import { useState } from 'react';
import { useMemo } from 'react';
import { EsegmentAnalysisSearchType } from '../../../../typings';
import SeartchSelect from '../SeartchSelect';

// const { Option } = Select;
interface Props {
  beforeSelections?: any;
  style?: CSSProperties | undefined;
  searchType?: string;
  contentRule?: any;
  allApplicationList: ApplicationItem[];
}
const FormItem = Form.Item;

function ContentBox(props: Props) {
  const { searchType, style, allApplicationList, beforeSelections } = props;
  const { networkIds } = beforeSelections;
  // useEffect(()=>{
  //   console.log(networkIds,'networkIds');
  // },[networkIds]);
  const [allServices, setAllServices] = useState<IService[]>();
  const [serLoading, setSerLoading] = useState(false);
  useEffect(() => {
    setSerLoading(true);
    queryAllServices().then((res) => {
      const { success, result } = res;
      if (success) {
        setAllServices(result);
        setSerLoading(false);
      }
    });
  }, []);
  const [contentRule, setContentRule] = useState<any>([{ required: true, message: '请输入值' }]);
  const content = useMemo(() => {
    let tmpRenderDom = <Input style={style} disabled placeholder={'不可用'} />;
    setContentRule([{ required: true, message: '请输入值' }]);
    if (searchType === EsegmentAnalysisSearchType.IPADDRESS) {
      const rules: Rule[] = [
        {
          validator: async (rule, value) => {
            if (!value) {
              throw new Error('请输入值');
            }
            // 先不支持掩码
            if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
              throw new Error('请输入正确的IPv4或IPv6地址');
            }
          },
          required: true,
        },
      ];
      setContentRule(rules);
      tmpRenderDom = <Input style={style} />;
    } else if (searchType === EsegmentAnalysisSearchType.APPLICATION) {
      tmpRenderDom = (
        <SeartchSelect
          style={style}
          key={EsegmentAnalysisSearchType.APPLICATION}
          listItems={allApplicationList.map((item) => ({
            text: item.nameText,
            value: item.applicationId,
          }))}
        />
      );
    } else if (searchType === EsegmentAnalysisSearchType.PORT) {
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
    } else if (searchType === EsegmentAnalysisSearchType.IPCONVERSATION) {
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
              if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
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
    } else if (searchType === EsegmentAnalysisSearchType.BUSINESS) {
      const serviceLists = allServices
        ?.filter((item: IService) => {
          const allNetworkIds = (item.networkIds || '').split(',');
          return networkIds.some((id: any)=> allNetworkIds.includes(id));
        })
        .map((item: IService) => ({ text: item.name, value: item.id }));
      tmpRenderDom = (
        <SeartchSelect
          style={style}
          key={EsegmentAnalysisSearchType.BUSINESS}
          loading={serLoading}
          listItems={serviceLists || []}
        />
      );
    }
    return tmpRenderDom;
  }, [allApplicationList, allServices, networkIds, searchType, serLoading, style]);
  return (
    <FormItem name="content" rules={contentRule} style={style}>
      {content}
    </FormItem>
  );
}

export default connect((state: any) => {
  const {
    SAKnowledgeModel: { allApplicationList },
  } = state;
  return { allApplicationList };
})(ContentBox);
