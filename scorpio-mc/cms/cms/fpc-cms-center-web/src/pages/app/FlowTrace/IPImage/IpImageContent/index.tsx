import React, { useCallback, useMemo, useState } from 'react';
// import IpImageRatioHistogram from './components/IpImageRatioHistogram';
// import Map from './components/Map';
import type { IShowCategory, PositionDetail } from '../typings';
import type { ISearchBoxInfo } from '../SearchBox';
import { IpImageWindows } from './components/constant';
import {
  Responsive,
  ResponsiveProps,
  WidthProvider,
  Responsive as ResponsiveGridLayout,
} from 'react-grid-layout';
import { withSize } from 'react-sizeme';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
// const ResponsiveReactGridLayout = WidthProvider(Responsive);
const cols = { lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 };
export const DROPPING_ELEM_ID = '__dropping-elem__';
export const SingleWindowWidth = React.createContext<any>([]);
interface SearchProps {
  searchInfo: ISearchBoxInfo;
  onChangeInfo?: any;
  // 新增加到底展示多少个的信息
  windowsIds: IShowCategory[];
  initPositionMessags: Record<IShowCategory, PositionDetail>;
  // changePositionMessages: (positions: Record<IShowCategory, PositionDetail>) => void;
  changePositionMessagesCache: any;
  readonly: boolean;
  size?: any;
}

const IPImageContent: React.FC<SearchProps> = ({
  searchInfo,
  windowsIds,
  initPositionMessags,
  // changePositionMessages,
  changePositionMessagesCache,
  readonly,
  size,
}) => {
  const layouts = useMemo(() => {
    // console.log(windowsIds,'windowsIds');
    // console.log(initPositionMessags, 'initPositionMessags');
    return windowsIds.map((item: IShowCategory) => {
      return { ...initPositionMessags[item], i: item };
    });
  }, [initPositionMessags, windowsIds]);

  const layoutsFormatter = useMemo(() => {
    return layouts.map((layout) => ({
      ...layout,
      isDraggable: readonly,
      isResizable: false,
    }));
  }, [layouts, readonly]);

  const generateDOM = useCallback(() => {
    return layoutsFormatter.map((l) => {
      if (IpImageWindows[l.i]) {
        return (
          // 将每个元素都一一映射放到界面上
          <div key={l.i} data-grid={l} style={{ border: 'none', padding: 0 }}>
            {IpImageWindows[l.i]}
          </div>
        );
      }
      return <div />;
    });
  }, [layoutsFormatter]);
  const handleDragStart = (a: any, b: any, c: any, d: any, e: any) => {
    // console.log(a, 'a');
    e.stopPropagation();
  };

  const handleLayoutChange = (currentLayout: any, allLayouts: any) => {
    console.log(currentLayout, 'currentLayout');
    // console.log(allLayouts, 'allLayouts');
    // setCurrentLayoutMessages(currentLayout);
    // changePositionLayout(currentLayout);
    currentLayout.map((layout: any) => {
      const layoutId = layout.i;
      changePositionMessagesCache((prev: Record<IShowCategory, PositionDetail>) => {
        return { ...prev, [layoutId]: { x: layout.x, y: layout.y, w: layout.w, h: layout.h } };
      });
    });
  };
  const [windowWidth, setWindowWidth] = useState(500);

  return (
    <SingleWindowWidth.Provider value={[windowWidth]}>
      {searchInfo.IpAddress ? (
        <>
          <ResponsiveGridLayout
            breakpoint="lg"
            layouts={{
              lg: layoutsFormatter,
            }}
            autoSize={true}
            width={size.width}
            cols={cols}
            preventCollision={false}
            rowHeight={52}
            // style={{ height: '500px' }}
            // margin={[15,15]}
            // 允许拖拽
            // @see: https://github.com/react-grid-layout/react-grid-layout/blob/master/test/examples/15-drag-from-outside.jsx
            isDroppable={readonly}
            onDragStart={handleDragStart}
            // onDrop={handleDrop}
            // https://github.com/react-grid-layout/react-grid-layout/blob/master/lib/ReactGridLayoutPropTypes.js#L222
            droppingItem={{ i: DROPPING_ELEM_ID, w: 4, h: 8 }}
            // onDragStop={handleDragStop}
            onLayoutChange={handleLayoutChange}
            onWidthChange={(containerWidth: number, margin: [number, number]) => {
              // console.log(containerWidth, 'containerWidth');
              // console.log(margin, 'margin');
              const widthMargin = margin[0] * 4;
              const singleWindowWith = (containerWidth - widthMargin) / 3;
              setWindowWidth(singleWindowWith);
            }}
          >
            {generateDOM()}
          </ResponsiveGridLayout>
        </>
      ) : null}
    </SingleWindowWidth.Provider>
  );
};
export default withSize({ refreshMode: 'debounce', refreshRate: 60 })(IPImageContent);
