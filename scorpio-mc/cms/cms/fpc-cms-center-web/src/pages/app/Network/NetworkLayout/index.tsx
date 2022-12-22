import TimeRangeSlider from '@/components/TimeRangeSlider';
import { Card } from 'antd';
import { connect } from 'dva';
import type { ReactNode } from 'react';
import React, { useEffect, useState } from 'react';
import type { Dispatch} from 'umi';
import { history } from 'umi';

interface Props {
  children: ReactNode;
  dispatch: Dispatch;
}

export const LayoutContext = React.createContext<any>(null);
function NetworkLayout(props: Props) {
  const { children, dispatch } = props;
  useEffect(() => {
    dispatch({
      type: 'SAKnowledgeModel/refreshSaKnowledge',
    });
  }, [dispatch]);
  const disableShowTime = history.location.pathname.includes(
    '/performance/dimensions-search/search',
  );

  const [layoutTitleName, setLayoutTitleName] = useState('');
  const [showTimeSelect, setShowTimeSelect] = useState(true);
  return (
    <LayoutContext.Provider value={[layoutTitleName, setLayoutTitleName, setShowTimeSelect]}>
      <Card bordered={false} bodyStyle={{ padding: 0 }} style={{ height: '100%', padding: 0 }}>
        <div style={{ display: 'flex', flexDirection: 'row' }}>
          <span style={{ lineHeight: '30px', margin: '0 10px' }}>{layoutTitleName}</span>
          {showTimeSelect ? (
            <TimeRangeSlider style={disableShowTime ? { display: 'none' } : {}} />
          ) : (
            <div />
          )}
        </div>
        {children}
      </Card>
    </LayoutContext.Provider>
  );
}
export default connect()(NetworkLayout);
