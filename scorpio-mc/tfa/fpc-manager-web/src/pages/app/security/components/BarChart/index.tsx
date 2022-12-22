import AnyWhereContainer from '@/components/AnyWhereContainer';
import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import EChartsMessage from '@/components/Message';
import { CHART_COLORS } from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import { queryMetadataLogs } from '@/pages/app/appliance/Metadata/service';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import {
  querySuricataRuleDetail,
  updateSuricataRule,
} from '@/pages/app/configuration/Suricata/service';
import { ERuleState } from '@/pages/app/configuration/Suricata/typings';
import { getLinkUrl, isIpAddress, isIpv4, jumpNewPage } from '@/utils/utils';
import { useClickAway } from 'ahooks';
import { Card, Menu, message } from 'antd';
import { isNumber } from 'lodash';
import numeral from 'numeral';
import { stringify } from 'qs';
import type { CSSProperties } from 'react';
import { useCallback, useMemo, useRef, useState } from 'react';
import type { IGlobalSelectedTime } from 'umi';
import { useSelector } from 'umi';
import { CHART_HEIGHT } from '../../Dashboard';
import type { SuricataStatisticsType } from '../../typings';
import styles from './index.less';

export type JUMP_MENU_KEYS = 'flow' | 'DNS' | 'alarm' | 'surRule' | 'stopRule';

const menuList: { key: JUMP_MENU_KEYS; text: string; path?: string }[] = [
  {
    key: 'flow',
    text: '会话详单',
    path: '/analysis/trace/flow-record',
  },
  {
    key: 'alarm',
    text: '告警分析',
    path: '/analysis/security/alert',
  },
  {
    key: 'surRule',
    text: '规则详情',
    path: 'configuration/safety-analysis/suricata/rule',
  },
  { key: 'stopRule', text: '禁用规则' },
  {
    key: 'DNS',
    text: 'DNS详单',
    path: '/analysis/trace/metadata/record',
  },
];

enum ChartCategoryType {
  IP = 'ip',
  SID = 'sid',
  DOMAIN = 'domain',
}

interface Props {
  chartType: SuricataStatisticsType;
  title: string;
  data: { key: string; count: number; label?: string }[];
  menus?: JUMP_MENU_KEYS[];
  labelRotate?: boolean;
  loading?: boolean;
}

const Chart = (props: Props) => {
  const { chartType, data, title, menus = [], loading } = props;

  const [menuPos, setMenuPos] = useState<{ x: number; y: number }>({ x: -999, y: -999 });
  const [menuDisplay, setMenuDisplay] = useState(false);
  const chartRef = useRef<HTMLDivElement>(null);

  const globalSelectedTime = useSelector<ConnectState, IGlobalSelectedTime>(
    (state) => state.appModel.globalSelectedTime,
  );

  // 最大的值
  let maxValue = 0;
  // 计算标签的最大宽度
  let maxLengthLabel: string = '';
  for (let index = 0; index < data.length; index += 1) {
    const element = data[index];
    if (element.key.length > maxLengthLabel.length) {
      maxLengthLabel = element.key;
    }
    if (element.count > maxValue) {
      maxValue = element.count;
    }
  }

  const filterRef = useRef<IFilter>();

  const handleBarClick = (key: string) => {
    filterRef.current = {
      field: 'tmp',
      operator: EFilterOperatorTypes.EQ,
      operand: key,
    };
    if (isIpAddress(key)) {
      filterRef.current.field = ChartCategoryType.IP;
    } else if (isNumber(parseInt(key)) && parseInt(key).toString().length === key.length) {
      // 其次如果是个纯数字 就作为sid
      filterRef.current.field = ChartCategoryType.SID;
    } else {
      // 最后是域名
      filterRef.current.field = ChartCategoryType.DOMAIN;
    }
    // 显示菜单
    setMenuDisplay(true);
  };

  const handleMenuClick = useCallback(
    async (info: any) => {
      const { key } = info;
      const filter: IFilterCondition = [];
      const otherParams: Record<string, any> = {};
      const { path } = menuList.find((item) => item.key === key)!;
      switch (key) {
        case 'flow': {
          if (filterRef.current?.field === ChartCategoryType.IP) {
            const ip = filterRef.current.operand as string;
            const isV4 = isIpv4(ip);
            const srcFilter: IFilter = {
              field: isV4 ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: ip,
            };
            const destFilter: IFilter = {
              field: isV4 ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: ip,
            };
            if (chartType === 'top_mining_host') {
              filter.push(srcFilter);
            } else if (chartType === 'top_mining_pool_address') {
              filter.push(destFilter);
            } else {
              filter.push({
                operator: EFilterGroupOperatorTypes.OR,
                group: [srcFilter, destFilter],
              });
            }
          }
          if (filterRef.current?.field === ChartCategoryType.DOMAIN) {
            // 查询该域名对应ip,再跳转会话详单
            const domain = filterRef.current.operand as string;
            const hideLoading = message.loading('查询路由中', 0);
            queryMetadataLogs({
              protocol: EMetadataProtocol.DNS,
              startTime: globalSelectedTime.originStartTime,
              endTime: globalSelectedTime.originEndTime,
              dsl: `(domain = "${domain}") and (dns_rcode = 0) | gentimes start_time start="${globalSelectedTime.originStartTime}" end="${globalSelectedTime.originEndTime}"`,
            }).then((res) => {
              hideLoading();
              const {
                success,
                result: { content },
              } = res;
              if (success && content.length > 0) {
                const domainIpArr: string[] = [
                  ...new Set(
                    (content as { domainAddress: string[] }[]).reduce((total, curr) => {
                      return [...curr.domainAddress, ...total];
                    }, [] as string[]),
                  ),
                ];
                if (domainIpArr.length < 1) {
                  message.warning('未查询到该域名的相关地址，不能进行跳转');
                  return;
                }
                const domainIpFilter: IFilter[] = domainIpArr.slice(0, 10).map((domainIp) => {
                  const domainIpIsV4 = isIpv4(domainIp);
                  return {
                    field: domainIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                    operator: EFilterOperatorTypes.EQ,
                    operand: domainIp,
                  };
                });
                filter.push({
                  operator: EFilterGroupOperatorTypes.OR,
                  group: domainIpFilter,
                });

                const targetUrl = getLinkUrl(
                  `${path}?filter=${encodeURIComponent(JSON.stringify(filter))}&from=${
                    globalSelectedTime.startTimestamp
                  }&to=${globalSelectedTime.endTimestamp}&relative=${
                    globalSelectedTime.relative
                  }&timeType=${ETimeType.CUSTOM}`,
                );
                // history.push(targetUrl);
                jumpNewPage(targetUrl);
              } else {
                message.warning('未查询到该域名的相关地址，不能进行跳转');
              }
            });

            return;
          }
          if (filterRef.current?.field === ChartCategoryType.SID) {
            const sid = filterRef.current.operand as number;
            otherParams.sid = sid;
          }
          break;
        }
        case 'DNS': {
          if (filterRef.current?.field === ChartCategoryType.IP) {
            const ip = filterRef.current.operand as string;
            const isV4 = isIpv4(ip);
            const srcFilter: IFilter = {
              field: isV4 ? 'src_ipv4' : 'src_ipv6',
              operator: EFilterOperatorTypes.EQ,
              operand: ip,
            };
            const destFilter: IFilter = {
              field: isV4 ? 'dest_ipv4' : 'dest_ipv6',
              operator: EFilterOperatorTypes.EQ,
              operand: ip,
            };
            if (chartType === 'top_mining_host') {
              filter.push(srcFilter);
            } else {
              filter.push({
                operator: EFilterGroupOperatorTypes.OR,
                group: [srcFilter, destFilter],
              });
            }
          }
          if (filterRef.current?.field === ChartCategoryType.DOMAIN) {
            const domain = filterRef.current.operand as string;
            filter.push({
              field: 'domain',
              operator: EFilterOperatorTypes.LIKE,
              operand: domain,
            });
          }
          break;
        }
        case 'alarm': {
          if (filterRef.current?.field === ChartCategoryType.IP) {
            const ip = filterRef.current.operand as string;
            const isV4 = isIpv4(ip);
            filter.push({
              operator: EFilterGroupOperatorTypes.OR,
              group: [
                {
                  field: isV4 ? 'src_ipv4' : 'src_ipv6',
                  operator: EFilterOperatorTypes.EQ,
                  operand: ip,
                },
                {
                  field: isV4 ? 'dest_ipv4' : 'dest_ipv6',
                  operator: EFilterOperatorTypes.EQ,
                  operand: ip,
                },
              ],
            });
          }
          if (filterRef.current?.field === ChartCategoryType.DOMAIN) {
            const domain = filterRef.current.operand as string;
            filter.push({
              field: 'domain',
              operator: EFilterOperatorTypes.EQ,
              operand: domain,
            });
          }
          if (filterRef.current?.field === ChartCategoryType.SID) {
            const sid = filterRef.current.operand as number;
            filter.push({
              field: 'sid',
              operator: EFilterOperatorTypes.EQ,
              operand: sid,
            });
          }
          break;
        }
        case 'surRule': {
          if (filterRef.current?.field === ChartCategoryType.SID) {
            const sid = filterRef.current.operand as number;
            const targetUrl = getLinkUrl(`${path}/${sid}/update`);
            jumpNewPage(targetUrl);
            return;
          }
          break;
        }

        case 'stopRule': {
          if (filterRef.current?.field === ChartCategoryType.SID) {
            const sid = filterRef.current.operand as number;
            const { success, result } = await querySuricataRuleDetail(sid);
            if (success) {
              if (result.state === ERuleState.启用) {
                updateSuricataRule({
                  ...result,
                  state: ERuleState.停用,
                }).then((res) => {
                  if (res.success) {
                    message.success('成功禁止');
                  } else {
                    message.error('未禁止');
                  }
                });
              } else if (result.state === ERuleState.停用) {
                message.info('已禁用');
              }
            }
            return;
          }
          break;
        }
      }

      let url = getLinkUrl(
        `${path}?${stringify({
          ...otherParams,
          filter: encodeURIComponent(JSON.stringify(filter)),
          from: globalSelectedTime.startTimestamp,
          to: globalSelectedTime.endTimestamp,
          timeType: ETimeType.CUSTOM,
        })}`,
      );

      if (key === 'DNS') {
        url = `${url}&jumpTabs=dns`;
      }

      jumpNewPage(url);
    },
    [chartType, globalSelectedTime],
  );

  useClickAway(() => {
    setMenuDisplay(false);
  }, [chartRef]);

  const menu = useMemo(() => {
    const showMenus = menuList.filter((item) => menus?.includes(item.key));

    return (
      <Menu onClick={handleMenuClick}>
        {showMenus.map((item) => {
          return <Menu.Item key={item.key}>{item.text}</Menu.Item>;
        })}
      </Menu>
    );
  }, [handleMenuClick, menus]);

  if (loading) {
    return <EChartsMessage height={CHART_HEIGHT} message="loading" title={title} />;
  }

  return (
    <>
      <Card title={title} size="small">
        <div
          className={styles.bar}
          style={{ height: CHART_HEIGHT, position: 'relative' }}
          ref={chartRef}
          onClick={() => {
            setMenuDisplay(false);
          }}
        >
          {data.length === 0 ? (
            <div className={styles.message}>
              <span>暂无数据</span>
            </div>
          ) : (
            data.map((item, index) => {
              // 柱子的颜色
              const pillarColor = CHART_COLORS[index];
              // 计算柱子的宽度
              let pillarWidth: number | string = maxValue ? (item.count / maxValue) * 100 : 0;
              if (pillarWidth === 0) {
                pillarWidth = '5px';
              } else if (pillarWidth <= 2) {
                pillarWidth = '10px';
              }

              pillarWidth = typeof pillarWidth === 'number' ? `${pillarWidth}%` : pillarWidth;

              const value = numeral(item.count).format('0,0');

              const valueTextWidth = value.toString().length * 9;
              // 比较空区域是否可以放下数值
              const valueStyles: CSSProperties = {
                width: valueTextWidth,
              };

              const labelStyles: CSSProperties = {
                right: valueTextWidth + 9 || 0,
              };

              return (
                <>
                  <div className={styles.row} key={item.key}>
                    <div className={styles.content}>
                      {/* css 设置了pointer-event:nont 使 label 不接受事件， 防止该元素挡在绘制的柱子上， */}
                      <span className={styles.label} style={{ ...labelStyles }}>
                        {item.label || item.key}
                      </span>
                      <span
                        className={styles.pillar}
                        style={{ background: pillarColor, width: pillarWidth }}
                        onClick={(e) => {
                          // 阻止事件冒泡, 外层元素负载取消菜单显示
                          e.stopPropagation();
                          // 组装基本的过滤条件
                          handleBarClick(item.key);

                          // 弹出菜单定位
                          const parentRect = chartRef.current!.getBoundingClientRect();
                          const { top, left } = parentRect;
                          const { clientX, clientY } = e;

                          setMenuPos({
                            x: clientX - left,
                            y: clientY - top,
                          });
                        }}
                      />
                      <span className={styles.value} style={{ ...valueStyles }}>
                        {value}
                      </span>
                    </div>
                  </div>
                </>
              );
            })
          )}
          <AnyWhereContainer
            style={{ padding: 0 }}
            top={menuPos.y}
            left={menuPos.x}
            display={menuDisplay}
          >
            {menu}
          </AnyWhereContainer>
        </div>
      </Card>
    </>
  );
};

export default Chart;
