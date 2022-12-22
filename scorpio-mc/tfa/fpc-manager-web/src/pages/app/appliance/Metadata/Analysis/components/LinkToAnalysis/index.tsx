import { useContext, useEffect } from 'react';
import { useLocation } from 'umi';
import { MetaDataContext } from '../..';
import { jumpToMetadataTab } from '../../constant';
import { EMetadataTabType } from '../../typings';
import { history } from 'umi';

export default function LinkToAnalysis() {
  const location = useLocation() as any;
  const [state, dispatch] = useContext(MetaDataContext);
  useEffect(() => {
    if (location?.query?.jumpTabs) {
      jumpToMetadataTab(
        state,
        dispatch,
        EMetadataTabType[location.query.jumpTabs.toLocaleUpperCase()],
        { filter: location?.query?.filter },
      );
      const tmpUrlQuery = location?.query;
      delete tmpUrlQuery.filter;
      delete tmpUrlQuery.jumpTabs;
      history.replace({
        query: tmpUrlQuery,
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
}
