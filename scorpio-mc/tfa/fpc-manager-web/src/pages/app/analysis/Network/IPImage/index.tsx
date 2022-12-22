import { useCallback, useState } from 'react';
import SearchBox from './SearchBox';
import IpImageContent from './IpImageContent';
import { useLocation } from 'umi';
import { history } from 'umi';
import AssetProperty from './AssetProperty';
import React from 'react';
import ConfigImage from './IpImageContent/components/ConfigImage';
import { message, Row } from 'antd';
import { useEffect } from 'react';
import type { PositionDetail } from './typings';
import { IShowCategory } from './typings';
import { getSettings, updateSettings } from './service';
import defaultLayouts from './positionMessage/layouts.json';
const defaultPositionMessages = defaultLayouts;

export const defaultWindowIds: IShowCategory[] = [
  IShowCategory.VISITINGIP,
  IShowCategory.VISITEDIP,
  IShowCategory.SHARINGPORT,
  IShowCategory.VISITINGPORT,
  IShowCategory.SHARINGDOMAINNAME,
  IShowCategory.VISITINGDOMAINNAME,
  IShowCategory.SECURITYALERTS,
  IShowCategory.CONNECTIONSOURCE,
  IShowCategory.CONNECTIONTARGET,
  // IShowCategory.APPLICATIONTREND,
  // IShowCategory.LOCATIONTREND
];
// const currentdefaultWindowIds = Object.values(IShowCategory);

export const SearchIpImageContext = React.createContext<any>([]);
const IPImage = () => {
  //跳转参数，解构跳转参数
  const { query } = useLocation() as any as { query: { ipAddress: string; networkIds: string } };
  const [searchInfo, setSearchInfo] = useState(() => {
    if (query.ipAddress) {
      const tempSearchInfo = {
        IpAddress: query.ipAddress,
        networkIds: query.networkIds ? query.networkIds : 'ALL',
      };
      const queryObj = history.location.query || {};
      delete queryObj.IpAddress;
      delete queryObj.networkIds;
      console.log(tempSearchInfo);
      return tempSearchInfo;
    } else {
      return {};
    }
  });

  const [windowIds, setWindowIds] = useState<IShowCategory[]>(defaultWindowIds);

  const [windowIdsCache, setWindowIdsCache] = useState<IShowCategory[]>(defaultWindowIds);

  const [positionMessages, setPositionMessages] =
    useState<Record<IShowCategory, PositionDetail>>(defaultPositionMessages);

  const [positionMessageCache, setPositionMessageCache] =
    useState<Record<IShowCategory, PositionDetail>>(defaultPositionMessages);

  const [loading, setLoading] = useState(false);

  const [isConfig, setIsConfig] = useState(false);

  const queryEditContent = useCallback(async () => {
    setLoading(true);
    getSettings().then((res) => {
      if (JSON.stringify(res) != '{}') {
        console.log(res, 'res {}');
        const { success, result } = res;
        setLoading(false);
        if (success) {
          const { layouts } = result;
          if (layouts) {
            const nowLayouts = JSON.parse(layouts);
            // console.log(nowLayouts, 'nowLayouts?');
            const { currentLayouts, currentIds } = nowLayouts;
            // let handledLayouts: Record<IShowCategory, PositionDetail>;
            // currentdefaultWindowIds.forEach((item: IShowCategory)=>{
            //   handledLayouts[item] = currentLayouts[item];
            // })
            setPositionMessages(currentLayouts);
            setPositionMessageCache(currentLayouts);
            setWindowIds(currentIds);
            setWindowIdsCache(currentIds);
          }
        }
      }
    });
  }, []);

  useEffect(() => {
    queryEditContent();
  }, [queryEditContent]);

  const updateEditContent = async () => {
    const submitData: any = {
      currentLayouts: positionMessageCache,
      currentIds: windowIds,
    };
    const { success } = await updateSettings({ layouts: JSON.stringify(submitData) });
    if (success) {
      message.success('更新成功!');
      setPositionMessages(positionMessageCache);
      queryEditContent();
    } else {
      message.error('更新失败!');
    }
  };

  useEffect(() => {
    if (isConfig === false) {
      setWindowIds(windowIdsCache);
    }
  }, [isConfig, windowIdsCache]);

  return (
    <SearchIpImageContext.Provider value={[searchInfo, windowIds, setWindowIds, positionMessages]}>
      <Row justify={'space-between'}>
        <SearchBox
          onSubmit={(e: any) => {
            setSearchInfo(e);
          }}
          initSearchInfo={(searchInfo || {}) as any}
        />
        <ConfigImage
          searchInfo={(searchInfo || {}) as any}
          windowIds={windowIds}
          changeWindowIds={setWindowIds}
          isEdit={isConfig}
          configurePositions={setIsConfig}
          refreshConfig={updateEditContent}
        />
      </Row>
      <AssetProperty searchInfo={(searchInfo || {}) as any} />
      <IpImageContent
        searchInfo={(searchInfo || {}) as any}
        windowsIds={windowIds}
        initPositionMessags={positionMessages}
        changePositionMessagesCache={setPositionMessageCache}
        readonly={isConfig}
      />
    </SearchIpImageContext.Provider>
  );
};
export default IPImage;
