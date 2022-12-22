// import React from 'react';
import { useCallback, useEffect, useState } from 'react';
import SearchBox from './SearchBox';
import IpImageContent from './IpImageContent';
import React from 'react';
import defaultLayouts from './positionMessage/layouts.json';
import { IShowCategory } from './typings';
import type { PositionDetail } from './typings';
import { getSettings, updateSettings } from './service';
import { message, Row } from 'antd';
import ConfigImage from './IpImageContent/components/ConfigImage';
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
];

export const SearchIpImageContext = React.createContext<any>([]);
const IPImage = () => {
  const [searchInfo, setSearchInfo] = useState();
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
            console.log(nowLayouts, 'nowLayouts?');
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
    // <SearchInfoContext.Provider value={[searchInfo, setSearchInfo]}>
    //   <SearchBox
    //     onSubmit={(e: any) => {
    //       setSearchInfo(e);
    //       // searchState(e);
    //     }}
    //   />
    //   <IpImageContent searchInfo={(searchInfo || {}) as any}/>
    // </SearchInfoContext.Provider>
    <SearchIpImageContext.Provider value={[searchInfo, windowIds, setWindowIds, positionMessages]}>
      <Row justify={'space-between'}>
        <SearchBox
          onSubmit={(e: any) => {
            setSearchInfo(e);
            // searchState(e);
            // setSearchStatus(true);
          }}
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
