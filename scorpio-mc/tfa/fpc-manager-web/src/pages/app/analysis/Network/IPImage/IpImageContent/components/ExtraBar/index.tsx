import {
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  TableOutlined,
} from '@ant-design/icons';
import { Select, Space, Tooltip } from 'antd';
import { useCallback, useMemo } from 'react';
import { DataShowedType, DataShowedType_Enum, tableTop } from '../../../typings';

interface Props {
  hasTopSelectionBar: boolean;
  top?: number;
  changeTop?: any;
  hasTypeChangeBar: boolean;
  types?: DataShowedType[];
  showedType?: string;
  changeShowedType?: any;
  hasNewTypeChangeBar?: boolean;
}

export default function ExtraBar(props: Props) {
  const {
    hasTopSelectionBar = true,
    top,
    changeTop,
    hasTypeChangeBar = true,
    types,
    showedType,
    changeShowedType,
    hasNewTypeChangeBar = true,
  } = props;

  const SelectionTopTenBar = useMemo(() => {
    if (changeTop) {
      return (
        <Select
          size="small"
          defaultValue={top}
          onChange={(key) => {
            changeTop(key);
          }}
        >
          {tableTop.map((item) => (
            <Select.Option value={item} key={item}>{`Top${item}`}</Select.Option>
          ))}
        </Select>
      );
    }
    return null;
  }, [top]);

  const getIcon = useCallback(
    (icon: DataShowedType) => {
      const nowStyle = {
        fontSize: 16,
        color: showedType === icon ? '#198ce1' : '',
      };
      switch (icon) {
        case DataShowedType.PIECHART:
          return <PieChartOutlined style={nowStyle} />;
        case DataShowedType.TABLE:
          return <TableOutlined style={nowStyle} />;
        case DataShowedType.BARLINECHART:
          return <LineChartOutlined style={nowStyle} />;
        case DataShowedType.BARCHART:
          return <BarChartOutlined style={nowStyle} />;
        default:
          return null;
      }
    },
    [showedType],
  );

  const SelectedShowTypeBar = useMemo(() => {
    if (types) {
      return types?.map((i: DataShowedType) => {
        const tipTitle = `打开${DataShowedType_Enum[i]}预览`;
        const icon = getIcon(i);
        return (
          <Tooltip title={tipTitle}>
            <span
              key={i}
              onClick={() => {
                if (changeShowedType) {
                  changeShowedType(i);
                }
              }}
            >
              {icon}
            </span>
          </Tooltip>
        );
      });
    }
    return null;
  }, [changeShowedType, getIcon, types]);

  const SingleSelect = useMemo(() => {
    return (
      <Tooltip title={showedType !== DataShowedType.PIECHART && '打开饼状图预览'}>
        <span
          onClick={() => {
            if (showedType === DataShowedType.PIECHART) {
              changeShowedType(DataShowedType.TABLE);
            } else {
              changeShowedType(DataShowedType.PIECHART);
            }
          }}
        >
          <PieChartOutlined
            style={{ fontSize: 16, color: showedType === DataShowedType.PIECHART ? '#198ce1' : '' }}
          />
        </span>
      </Tooltip>
    );
  }, [changeShowedType, showedType]);

  return (
    <Space>
      {hasTopSelectionBar && SelectionTopTenBar}
      {hasTypeChangeBar && SelectedShowTypeBar}
      {hasNewTypeChangeBar && SingleSelect}
    </Space>
  );
}
