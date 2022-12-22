import { Button, Divider, Select, Spin } from 'antd';
import Item from 'antd/lib/list/Item';
import { useCallback, useEffect, useState } from 'react';

const { Option } = Select;
const DEFAULT_PAGE_SIZE = 10;

interface IBaseSelectProps {
  id: string;
  name: string;
}

interface Props<T> {
  /** 是否禁用选择器 */
  disabled: boolean;
  /** 初始化列表项目 */
  initialItem?: T;
  /** placeholder */
  placeholder?: string;
  /** 查询函数 */
  fetch: (params: Record<string, any>) => Promise<any>;
  /** 分页大小 */
  pageSize?: number;
  /** 选中回调 */
  onSelect?: (pcappFile: T) => void;
}

/** 选择器模式 */
enum ESelectMode {
  /** 模糊搜索 */
  'DIM' = 'dim',
  /** 常规搜索 */
  'REGULAR' = 'regular',
}

export default function TransmitTaskSelect<T extends IBaseSelectProps>({
  disabled = false,
  initialItem,
  placeholder,
  fetch,
  pageSize = DEFAULT_PAGE_SIZE,
  onSelect,
}: Props<T>) {
  /** loading */
  const [loading, setLoading] = useState<boolean>(false);
  /** 选择器模式 */
  const [selectMode, setSelectMode] = useState<ESelectMode>(ESelectMode.REGULAR);
  /** 模糊搜索结果列表 */
  const [dimList, setDimList] = useState<T[]>([]);
  /** 常规搜索列表 */
  const [regularList, setRegularList] = useState<T[]>([]);
  /** 模糊搜索页面 */
  const [dimPage, setDimPage] = useState<number>(0);
  /** 常规搜索页面 */
  const [regularPage, setRegularPage] = useState<number>(0);
  /** 模糊搜索值 */
  const [dimVal, setDimVal] = useState<string>('');
  /** 选择的value */
  const [selectedValue, setSelectedValue] = useState<string | undefined>(undefined);
  /** 更改选择器模式 */
  const changeSelectMode = (mode: ESelectMode) => {
    setSelectMode(mode);
  };

  /** 渲染选择器内容 */
  const renderSelectOptions = useCallback(() => {
    if (selectMode === ESelectMode.DIM) {
      return dimList.map((item) => (
        <Option value={item.id} key={item.id}>
          {item.name}
        </Option>
      ));
    } else if (selectMode === ESelectMode.REGULAR) {
      return regularList.map((item) => (
        <Option value={item.id} key={item.id}>
          {item.name}
        </Option>
      ));
    } else {
      return [];
    }
  }, [dimList, regularList, selectMode]);

  /** 更新常规查询列表 */
  const updateRegularList = async () => {
    setLoading(true);
    const { success, result } = await fetch({
      name: '',
      pageSize,
      page: regularPage,
    });
    setLoading(false);
    if (success) {
      const { content, number, totalPages } = result;
      if ((number === 0 && totalPages === 1) || number < totalPages - 1) {
        setRegularList((prev) => [
          ...prev,
          ...(content as T[])?.filter((f) => {
            if (f.id === initialItem?.id) {
              return false;
            }
            if (prev.findIndex((item) => item?.id === f.id) >= 0) {
              return false;
            }
            return true;
          }),
        ]);
        /** 更新常规搜索页面序号 */
        setRegularPage((prev) => prev + 1);
      }
    }
  };
  /** 查询模糊搜索列表
   * @append: 是否加在末尾
   */
  const updateDimList = useCallback(
    async (append = false) => {
      setLoading(true);
      const { success, result } = await fetch({
        name: dimVal,
        pageSize,
        page: !append ? 0 : dimPage,
      });
      setLoading(false);
      if (success) {
        const { content, number, totalPages } = result;
        if (append) {
          if ((number === 0 && totalPages === 1) || number < totalPages - 1) {
            setDimList((prev) => [
              ...prev,
              ...(content as T[])?.filter((f) => f.id !== initialItem?.id),
            ]);
            /** 更新常规搜索页面序号 */
            setDimPage((prev) => prev + 1);
          }
        } else {
          if (
            (number === 0 && totalPages === 1) ||
            (number === 0 && totalPages === 1) ||
            number < totalPages - 1
          ) {
            const newList = [];
            if (initialItem) {
              newList.push(initialItem);
            }
            newList.push(...(content as T[])?.filter((f) => f.id !== initialItem?.id));
            setDimPage(1);
            setDimList(newList);
          }
        }
      }
    },
    [fetch, dimVal, pageSize, dimPage, initialItem],
  );

  /** 处理获得更多按钮 */
  const hanleFetchMore = () => {
    if (selectMode === ESelectMode.REGULAR) {
      updateRegularList();
    } else if (selectMode === ESelectMode.DIM) {
      updateDimList(true);
    }
  };

  useEffect(() => {
    if (regularList?.length === 0) {
      /** 初始化 将初始化项目加入列表 */
      if (initialItem?.id) {
        setDimList([initialItem]);
        setRegularList([initialItem]);
        setSelectedValue(initialItem?.id);
        setDimVal(initialItem?.name);
      }
      /** 更新常规搜索数据 */
      updateRegularList();
    }
  }, [initialItem]);

  /** 模糊搜索后更新list */
  useEffect(() => {
    updateDimList(false);
  }, [updateDimList]);

  return (
    <>
      <Select
        showSearch
        disabled={disabled}
        placeholder={placeholder}
        optionFilterProp="children"
        onSelect={(id: string) => {
          if (onSelect) {
            if (selectMode === ESelectMode.DIM) {
              const pcapFile = dimList.find((item) => item.id === id);
              if (pcapFile) {
                onSelect(pcapFile);
              }
            } else if (selectMode === ESelectMode.REGULAR) {
              const pcapFile = regularList.find((item) => item.id === id);
              if (pcapFile) {
                onSelect(pcapFile);
              }
            }
          }
          setSelectedValue(id);
        }}
        allowClear
        onClear={() => {
          changeSelectMode(ESelectMode.REGULAR);
          setSelectedValue('');
          setDimPage(0);
          setRegularPage(0);
          setDimVal('');
          setTimeout(() => {
            updateRegularList();
          }, 2000);
        }}
        onSearch={(e) => {
          /** 修改 选择框模式 */
          if (e) {
            changeSelectMode(ESelectMode.DIM);
          } else {
            changeSelectMode(ESelectMode.REGULAR);
          }
          setDimVal(e);
        }}
        value={selectedValue}
        dropdownRender={(menu) => {
          return (
            <>
              {loading ? <Spin tip="Loading...">{menu}</Spin> : menu}
              <Divider style={{ margin: '8px 0' }} />
              <Button type={'link'} size={'small'} onClick={hanleFetchMore}>
                更多...
              </Button>
            </>
          );
        }}
      >
        {renderSelectOptions()}
      </Select>
    </>
  );
}
