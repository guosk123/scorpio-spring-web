import { Space, Spin, Tabs } from 'antd';
import type { CSSProperties, Dispatch, ReactNode } from 'react';
import { useRef } from 'react';
import { useState } from 'react';
import { useEffect } from 'react';
import React from 'react';
import { useCallback, useReducer } from 'react';
import { v1 as uuidv1 } from 'uuid';
import TabSettingTool from './components/TabSettingTool';
import './index.less';
import storage from '@/utils/frame/storage';
import { abortAllQuery } from '@/utils/utils';

export interface IState {
  panes: IPane[];
  activeKey: string | undefined;
  shareInfo: any;
}

export interface IPane {
  title?: ReactNode;
  key?: string;
  closable?: boolean;
  content: ReactNode;
  tmpContent?: ReactNode;
  filterObj?: any;
  isNewTab?: boolean;
  paneDetail?: any;
}

export enum EReducerType {
  // 操作activitykey
  CHANGEACTIVEKEY = 'changeActiveKey',
  // 操作panes
  CHANGEPANES = 'changePanes',
  // 更新需要传递的信息，读取后清空
  CHANGESHAREINFO = 'changeShareInfo',
}

export interface ITabsState {
  panes: IPane[];
  activeKey: string | null;
}

export interface ITab {
  title: ReactNode;
  defShow: boolean;
  content: ReactNode;
  isNewTab?: boolean;
  detail?: any;
}

const { TabPane } = Tabs;

const reducer = (state: any, action: any) => {
  switch (action.type) {
    case EReducerType.CHANGEACTIVEKEY:
      return {
        ...state,
        activeKey: action.value,
      };
    case EReducerType.CHANGEPANES:
      return {
        ...state,
        panes: action.value,
      };
    case EReducerType.CHANGESHAREINFO:
      return {
        ...state,
        shareInfo: action.value,
      };
    default:
      return state;
  }
};

const getInitState = (tabs: any, tabsSetting?: string[], showTabSettingTool?: boolean) => {
  const safeTabKeysFlag = tabsSetting?.length && !tabsSetting?.some((item: any) => !item.length);
  const panesObj = tabs
    ? Object.keys(tabs)
        .filter((ele) => {
          // 有本地存储
          if (showTabSettingTool && safeTabKeysFlag) {
            return tabsSetting.includes(ele);
          }
          // 无本地存储
          return tabs[ele].defShow;
        })
        .map((item) => {
          const element = tabs[item];
          return {
            ...element,
            title: element.title,
            key: `${item}^${uuidv1()}`,
            closable: !element.defShow,
            content: element.content,
            // content: !index ? element.content : <div />,
            tmpContent: element.content,
          } as IPane;
        })
    : [];
  return {
    panes: panesObj,
    activeKey: panesObj[0]?.key,
    shareInfo: null,
  };
};

type IReducer = React.Reducer<IState, any>;

/**
 *
 * @param addItem 新增tab对象
 * @param dispatch 获取的dispatch
 * @param state
 * @param tabType 将会加入tab ID作为表示一部分
 * @param shareInfo 要共享的对象 共享的内容实用后要清除 fn: clearShareInfo
 */
export const newPanesFn = (
  addItem: ITab | ITab[],
  dispatch: Dispatch<any>,
  state: ITabsState,
  tabType: string | string[],
  shareInfo?: any,
) => {
  console.log('addItem', addItem);
  if (!Array.isArray(addItem)) {
    const tmpPane: IPane = {
      ...addItem,
      title: addItem.title,
      key: `${tabType || 'newTab'}^${uuidv1()}`,
      closable: !addItem.defShow,
      content: addItem.content,
      tmpContent: addItem.content,
      paneDetail: addItem.detail,
    };
    console.log('tmpPane', tmpPane);
    dispatch({
      type: EReducerType.CHANGEPANES,
      value: state.panes.concat(tmpPane),
    });
    dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: tmpPane.key });
    dispatch({ type: EReducerType.CHANGESHAREINFO, value: shareInfo });
    // dispatch({ type: EReducerType.CHANGESHAREINFO, value: null });
  } else {
    const tmpPanes = addItem.map((item, index) => {
      return {
        ...item,
        title: item.title,
        key: `${tabType[index] || 'newTab'}^${uuidv1()}`,
        closable: !item.defShow,
        content: item.content,
        tmpContent: item.content,
        paneDetail: item.detail,
      };
    });
    dispatch({
      type: EReducerType.CHANGEPANES,
      value: state.panes.concat(tmpPanes),
    });
    dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: tmpPanes[0]?.key });
    dispatch({ type: EReducerType.CHANGESHAREINFO, value: shareInfo });
  }
};

// 需要传递的信息读取后清空
export const clearShareInfo = (dispatch: Dispatch<any>) => {
  if (!dispatch) {
    return;
  }
  dispatch({ type: EReducerType.CHANGESHAREINFO, value: null });
};

export const handleActiveKey = (
  dispatch: Dispatch<any>,
  state: ITabsState,
  activeKey: string,
  shareInfo?: any,
) => {
  const tmpKey = state.panes.find((item) => {
    return item.key?.split('^').includes(activeKey);
  });
  if (tmpKey) {
    dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: tmpKey?.key });
    dispatch({ type: EReducerType.CHANGESHAREINFO, value: shareInfo });
  }
};

export const getTabDetail = (state: ITabsState) => {
  const tmpTabDetail = state.panes.find((item) => item.key === state.activeKey);
  return tmpTabDetail?.paneDetail;
};

interface Props {
  /** 需要加载的所有tab */
  tabs: any;
  /** 建议传入一个新的context，默认会给一个，使用默认的content在双层tabs时会导致状态混乱 */
  consumerContext?: React.Context<any>;
  shareData?: any;
  /** 建议传入一个0像素的dom，这里用于监听从地址栏直接跳转数据包的连接 */
  linkToTab?: JSX.Element;
  /** 双层tab时，用于tab的content内部获取多层tab的title名称，如：title父-title子 */
  dirTabName?: string;
  /** 传入的方法会在切换tab时触发 */
  onChangeTab?: (key?: string) => void;
  /** 重置tab，回调中会给一个函数，调用即重置tab */
  resetTabsState?: (reset: () => void) => void;
  /** 回调函数中会给一个值，表示tab中是否存在可以关闭的标签页 */
  haveClosableTab?: (flag: boolean) => void;
  destroyInactiveTabPane?: boolean;
  style?: CSSProperties;
  tabsKey?: string;
  showTabSettingTool?: boolean;
  editTab?: any;
  fixHeight?: number;
  loading?: boolean;
  cancelQueryOnChange?: boolean;
}

export const context = React.createContext<any>(null);
export default function EditTabs(props: Props) {
  const {
    tabs,
    consumerContext = context,
    linkToTab = <div />,
    style,
    dirTabName,
    onChangeTab = () => {},
    // 重置当前tabs
    resetTabsState = () => {},
    // 当前tabs是否存在可关闭tab
    haveClosableTab = () => {},
    destroyInactiveTabPane = false,
    tabsKey = 'defTab',
    showTabSettingTool,
    // 传入一个ref，拿到可以操作tabs的对象
    editTab = {},
    fixHeight,
    loading,
    cancelQueryOnChange = false,
  } = props;
  // 自定义设置中的tab
  const [tabsSettingKeys, setTabsSettingKeys] = useState<string[]>([]);
  const [state, dispatch] = useReducer<IReducer>(
    reducer,
    getInitState(tabs, JSON.parse(storage.get(tabsKey) || '[]'), showTabSettingTool),
  );
  const { panes, activeKey } = state;
  useEffect(() => {
    resetTabsState(() => {
      const resetObj = getInitState(tabs, tabsSettingKeys, showTabSettingTool);
      dispatch({ type: EReducerType.CHANGEPANES, value: resetObj.panes });
      dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: resetObj.activeKey });
    });
  }, [resetTabsState, showTabSettingTool, tabs, tabsSettingKeys]);

  const activeKeyRef = useRef('');
  useEffect(() => {
    activeKeyRef.current = activeKey || '';
  }, [activeKey]);

  useEffect(() => {
    const safeTabKeysFlag =
      tabsSettingKeys.length && !tabsSettingKeys.some((item: any) => !item.length);
    // 根据选择的结果重置tab是否显示
    if (tabsSettingKeys.length && showTabSettingTool && safeTabKeysFlag) {
      const nextPanes = Object.keys(tabs)
        .filter((ele) => tabsSettingKeys.includes(ele))
        .map((item) => {
          const element = tabs[item];
          return {
            title: element.title,
            key: `${item}^${uuidv1()}`,
            closable: !element.defShow,
            content: element.content,
            tmpContent: element.content,
          } as IPane;
        });
      // .concat(panes.filter((sub) => sub.isNewTab));
      // const resPane = [...nextPanes, ...oldPanes];
      if (nextPanes.length) {
        dispatch({
          type: EReducerType.CHANGEPANES,
          value: nextPanes,
        });
        dispatch({
          type: EReducerType.CHANGEACTIVEKEY,
          value: nextPanes[0].key,
          // oldPanes.find((item) => item.key === activeKeyRef.current)?.key || nextPanes[0].key,
        });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showTabSettingTool, tabs, tabsSettingKeys]);

  useEffect(() => {
    let flag = false;
    state.panes.forEach((element) => {
      flag = element.closable || flag;
    });
    haveClosableTab(flag);
  }, [haveClosableTab, state.panes]);

  const remove = useCallback(
    (targetKey: any) => {
      let tmpActiveKey: any;
      panes.forEach((item: any, index: any) => {
        if (item.key === targetKey && activeKey !== targetKey) {
          // 删除掉的tab不是当前tab
          tmpActiveKey = activeKey;
        } else if (item.key === targetKey) {
          // 删除掉的tab是当前tab，但不是第一个
          const tmpIndex = index - 1;
          tmpActiveKey = panes[tmpIndex]?.key;
        }
      });
      dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: tmpActiveKey });
      dispatch({
        type: EReducerType.CHANGEPANES,
        value: panes.filter((item: any) => {
          return item.key !== targetKey;
        }),
      });
    },
    [activeKey, panes],
  );

  const onEdit = useCallback(
    (targetKey: any, action: any) => {
      if (action === 'remove') {
        remove(targetKey);
      }
    },
    [remove],
  );

  const onChange = useCallback(
    (key: string) => {
      onChangeTab(key);
      if (cancelQueryOnChange) {
        abortAllQuery();
      }
      dispatch({ type: EReducerType.CHANGEACTIVEKEY, value: key });
    },
    [onChangeTab],
  );
  const [height, setHeight] = useState(0);
  const [currentTopHeight, setCurrentTopHeight] = useState<number>(0);
  const toTopHeight = useRef<HTMLDivElement>(null);

  const handleResize = useCallback(() => {
    setHeight(window.innerHeight - (toTopHeight.current?.getBoundingClientRect().top || 0) - 54);
  }, [toTopHeight, currentTopHeight]);

  /** 监听由于js导致的高度变化 */
  useEffect(() => {
    const interval = setInterval(() => {
      const top = toTopHeight.current?.getBoundingClientRect().top;
      if (top !== currentTopHeight) {
        setCurrentTopHeight(top || 0);
      }
    }, 200);
    return () => {
      clearInterval(interval);
    };
  }, []);

  useEffect(() => {
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, [handleResize]);

  useEffect(() => {
    if (editTab.current) {
      editTab.current.remove = remove;
      editTab.current.state = state;
    }
  }, [editTab, remove, state]);

  return (
    <consumerContext.Provider value={[state, dispatch]}>
      <div ref={toTopHeight} />
      <div style={{ display: 'none' }}>{linkToTab}</div>
      <Tabs
        type="editable-card"
        hideAdd
        style={{
          ...style,
        }}
        size={'small'}
        onChange={onChange}
        activeKey={state.activeKey || undefined}
        destroyInactiveTabPane={destroyInactiveTabPane}
        onEdit={onEdit}
        tabBarExtraContent={
          <Space>
            <TabSettingTool
              tabs={tabs}
              tabsKey={tabsKey}
              tabsSettingState={[tabsSettingKeys, setTabsSettingKeys]}
              style={showTabSettingTool ? {} : { display: 'none' }}
            />
          </Space>
        }
      >
        {panes.map((pane: any) => {
          return (
            <TabPane
              tab={pane.title}
              key={pane.key}
              closable={!!pane.isNewTab || pane.closable}
              style={{
                minHeight: 0,
                height: fixHeight ? height - fixHeight : height,
                overflowY: 'auto',
                overflowX: 'hidden',
              }}
            >
              {loading ? (
                <Spin />
              ) : (
                React.cloneElement(pane.content, {
                  paneKey: pane.key,
                  // 向子pane传递具有层级关系的title
                  paneTitle: dirTabName ? `${dirTabName}-${pane.title}` : pane.title,
                })
              )}
            </TabPane>
          );
        })}
      </Tabs>
    </consumerContext.Provider>
  );
}
