import { Tabs } from 'antd';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { connect } from 'dva';
import { v1 as uuidv1 } from 'uuid';
import { EDimensionsSearchType, DimensionsSearchMapping, EDRILLDOWN } from '../../typing';
import TabContent from './components/TabContent';
import type { ISearchBoxInfo } from '../SearchBox';
import type {
  ICityMap,
  ICountryMap,
  ICustomCountryMap,
  IProvinceMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';

const { TabPane } = Tabs;

interface Props {
  searchBoxInfo?: ISearchBoxInfo;
  allCountryMap: ICountryMap;
  allProvinceMap: IProvinceMap;
  allCityMap: ICityMap;
  allCustomCountryMap: ICustomCountryMap;
  allApplicationMap: IApplicationMap;
}

interface IShareRow {
  data: any;
  type: string;
}

function EditTab(props: Props) {
  const {
    searchBoxInfo,
    allCountryMap,
    allProvinceMap,
    allCityMap,
    allApplicationMap,
    allCustomCountryMap,
  } = props;
  const [panes, setPanes] = useState<any>([]);
  const [activeKey, setActiveKey] = useState<string>();
  const [dirllDownInfo, setDirllDownInfo] = useState<ISearchBoxInfo>();
  const [shareRow, setShareRow] = useState<IShareRow>();

  const getTabName = useCallback(
    (item) => {
      let tmpFunc: any = () => item.content;
      const translationType = searchBoxInfo?.dimensionsSearchType;
      if (translationType === EDimensionsSearchType.LOCATION) {
        tmpFunc = (ele: ISearchBoxInfo) => {
          const locationCodeArr = ele.content.split('_');
          let seriesName = locationCodeArr[locationCodeArr.length];
          if (ele.content.length === 20) {
            seriesName = Object.values(allCustomCountryMap).find((sub) => sub.id === ele.content)
              ?.fullName as string;
          } else if (locationCodeArr.length === 3) {
            seriesName = allCityMap[locationCodeArr.pop() || 0]?.fullName as string;
          } else if (locationCodeArr.length === 2) {
            seriesName = allProvinceMap[locationCodeArr.pop() || 0]?.fullName as string;
          } else if (locationCodeArr.length === 1) {
            seriesName = allCountryMap[locationCodeArr.pop() || 0]?.fullName as string;
          }
          return seriesName || '[--]';
        };
      } else if (translationType === EDimensionsSearchType.APPLICATION) {
        tmpFunc = (ele: ISearchBoxInfo) => {
          return allApplicationMap[ele.content]?.nameText || (ele.content as string);
        };
      }

      return tmpFunc(item);
    },
    [
      allApplicationMap,
      allCityMap,
      allCountryMap,
      allCustomCountryMap,
      allProvinceMap,
      searchBoxInfo?.dimensionsSearchType,
    ],
  );

  useEffect(() => {
    if (searchBoxInfo) {
      const tmpActiveKey = `newTab${uuidv1()}`;
      setActiveKey(tmpActiveKey);
      setPanes([
        {
          title: `${getTabName(searchBoxInfo)}: ${
            DimensionsSearchMapping[searchBoxInfo?.dimensionsSearchType]?.title
          }`,
          key: tmpActiveKey,
          content: (
            <TabContent
              searchBoxInfo={searchBoxInfo}
              onClickDirllDown={(even: any) => {
                const { e, selectedRow, tabType } = even;
                setShareRow({
                  data: selectedRow?.type ? selectedRow.data : selectedRow,
                  type: tabType,
                });
                setDirllDownInfo(e);
              }}
            />
          ),
        },
      ]);
    }
  }, [searchBoxInfo, getTabName]);

  const onChange = (key: string) => {
    setActiveKey(key);
  };

  const add = useCallback(
    (addItem: any, drilldown) => {
      const tmpActiveKey = `newTab${uuidv1()}`;
      setActiveKey(tmpActiveKey);
      setPanes([
        ...panes,
        {
          title: `${getTabName(searchBoxInfo)}: ${
            DimensionsSearchMapping[addItem?.dimensionsSearchType]?.title
          }`,
          key: tmpActiveKey,
          content: (
            <TabContent
              shareRow={shareRow}
              searchBoxInfo={{ ...addItem, content: searchBoxInfo?.content }}
              drilldown={drilldown}
              onClickDirllDown={(even: any) => {
                const { e, selectedRow, tabType } = even;
                setShareRow({
                  data: selectedRow?.type ? selectedRow.data : selectedRow,
                  type: selectedRow?.type ? selectedRow.type : tabType,
                });
                setDirllDownInfo(e);
              }}
            />
          ),
        },
      ]);
    },
    [searchBoxInfo, getTabName, panes, shareRow],
  );

  const remove = useCallback(
    (targetKey: any) => {
      setPanes(
        panes.filter((item: any, index: number) => {
          if (item.key === targetKey && activeKey !== targetKey) {
            setActiveKey(activeKey);
          } else if (item.key === targetKey && index) {
            const tmpIndex = index - 1;
            setActiveKey(panes[tmpIndex]?.key || '');
          } else if (item.key === targetKey && !index && panes.length > index) {
            const tmpIndex = index + 1;
            setActiveKey(panes[tmpIndex]?.key || '');
          }
          return item.key !== targetKey;
        }),
      );
    },
    [activeKey, panes],
  );

  // 新建tab，下钻
  useEffect(() => {
    if (dirllDownInfo) {
      add(dirllDownInfo, EDRILLDOWN.ISDRILLDOWN);
      setShareRow(undefined);
      setDirllDownInfo(undefined);
    }
  }, [add, dirllDownInfo]);

  const onEdit = useCallback(
    (targetKey: any, action: any) => {
      if (action === 'remove') {
        remove(targetKey);
      }
    },
    [remove],
  );

  return (
    <Fragment>
      <Tabs
        type="editable-card"
        hideAdd
        size={'small'}
        onChange={onChange}
        activeKey={activeKey}
        onEdit={onEdit}
      >
        {panes?.map((pane: any) => (
          <TabPane tab={pane.title} key={pane.key} closable={pane.closable}>
            {pane.content}
          </TabPane>
        ))}
      </Tabs>
    </Fragment>
  );
}
export default connect((state: any) => {
  const {
    appModel: { globalSelectedTime },
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap, allCustomCountryMap },
    SAKnowledgeModel: { allApplicationMap },
  } = state;
  return {
    allCountryMap,
    allProvinceMap,
    allCityMap,
    allApplicationMap,
    allCustomCountryMap,
    globalSelectedTime,
  };
})(EditTab);
