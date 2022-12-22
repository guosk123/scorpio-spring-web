import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import EditTabs from '@/pages/app/Network/components/EditTabs';
import { createContext, Fragment, useState } from 'react';
import { connect } from 'umi';
import type { ISearchBoxInfo } from '../components/SearchBox';
import LinkToSearchTab from './components/LinkToSearchTab';
import TransformTitle from './components/TransformTitle';
import { DimensionsSearchTabs } from './constant';

export const DimensionsSearchContext = createContext([]);

interface Props {
  searchInfo?: ISearchBoxInfo;
  onChangeInfo?: any;
  globalSelectedTime: IGlobalTime;
}

function SeartchTabs(props: Props) {
  const { searchInfo, onChangeInfo, globalSelectedTime } = props;
  const [timeInfo, setTimeInfo] = useState({ startTime: globalSelectedTime.startTime });
  const [tmpInfo, setTmpInfo] = useState(searchInfo);

  return (
    <Fragment>
      <EditTabs
        tabs={DimensionsSearchTabs}
        consumerContext={DimensionsSearchContext}
        tabsKey="networkAnalysis"
        linkToTab={
          JSON.stringify(tmpInfo) === JSON.stringify(searchInfo) ? (
            <LinkToSearchTab searchInfo={searchInfo} onChangeInfo={onChangeInfo} />
          ) : (
            <div style={{ display: 'none' }} />
          )
        }
        destroyInactiveTabPane={true}
        resetTabsState={(reset) => {
          if (JSON.stringify(tmpInfo) === '{}') {
            setTmpInfo(searchInfo);
            return;
          }
          if (JSON.stringify(tmpInfo) !== JSON.stringify(searchInfo)) {
            reset();
            setTmpInfo(searchInfo);
          }
        }}
      />
    </Fragment>
  );
}

export default connect((state: any) => {
  const {
    appModel: { globalSelectedTime },
  } = state;
  return { globalSelectedTime };
})(SeartchTabs);
