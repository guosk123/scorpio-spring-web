import { Button, TimePicker } from 'antd';
import moment from 'moment';

export default function SimpleTime(props: any) {
  const { onChange, value, show, addTimeItem, moveTimeItem } = props;
  return (
    <div>
      <TimePicker.RangePicker
        onChange={(selectedTime, selectedTimeStr) => {
          onChange(selectedTimeStr);
        }}
        defaultValue={value && value?.map((item: string) => moment(item, 'HH:mm:ss'))}
      />
      {show && (
        <Button type="primary" onClick={addTimeItem}>
          添加
        </Button>
      )}
      {!show && <Button onClick={moveTimeItem}>删除</Button>}
    </div>
  );
}
