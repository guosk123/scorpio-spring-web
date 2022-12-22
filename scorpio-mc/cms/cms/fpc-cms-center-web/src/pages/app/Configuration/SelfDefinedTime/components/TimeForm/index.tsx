import type { RadioChangeEvent } from 'antd';
import { Button, DatePicker, Form, Input, message, Modal, Radio, Select } from 'antd';
import moment from 'moment';
import React, { Fragment, useCallback, useEffect, useState } from 'react';
import { history } from 'umi';
import { createSingleSelfdefinedTime, updateSelfDefinedTimeDetail } from '../../services';
import type { TimeConfigItem } from '../../typings';
import { ECustomTimeType, timeTypeOptions, week_Enum } from './../../typings';
import SimpleTime from '../SimpleTime';
import { v4 as uuidv4 } from 'uuid';
import { enumObj2List } from '@/utils/utils';
import { EPageMode } from '@/pages/app/GlobalSearch/PacketRetrieval/components/TransmitTaskForm';

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

export interface TimeConfigItemFormSharedProps {
  timeDetail?: TimeConfigItem;
  pageMode?: any;
}

export const FormDataRangePicker = (props: any) => {
  const { onChange, value } = props;

  return (
    <DatePicker.RangePicker
      showTime
      onChange={(selectedTime, selectedTimeStr) => {
        onChange(selectedTimeStr);
      }}
      value={value && value?.map((item: string) => moment(item, 'YYYY-MM-DD HH:mm:ss'))}
    />
  );
};

const TimeSetting: React.FC<TimeConfigItemFormSharedProps> = ({ timeDetail, pageMode }) => {
  const [form] = Form.useForm();
  const [timeArr, setTimeArr] = useState(() => {
    const updateTimeArr = Object.keys(timeDetail || {})
      .filter((key) => key.includes('time'))
      .map((item) => ({ name: item }));
    return updateTimeArr.length > 0 && updateTimeArr.length <= 10
      ? updateTimeArr
      : [{ name: `time1` }];
  });

  const [timeType, settimeType] = useState(ECustomTimeType.PeriodicTime); // 不要用数字，用枚举
  const [isModalVisible, setIsModalVisible] = useState(false);

  useEffect(() => {
    if (timeDetail) {
      settimeType(timeDetail.type);
      form.setFieldsValue({
        ...timeDetail,
      });
    }
  }, [form, timeDetail]);

  const changeTimeTypeSetting = ({ target: { value } }: RadioChangeEvent) => {
    settimeType(value);
  };

  const overlapTimes = (overTime: any) => {
    const intervals: any = overTime;
    intervals.sort((a: any, b: any) => {
      const time1 = moment(a[0], 'hh:mm:ss');
      const time2 = moment(b[0], 'hh:mm:ss');
      const diff1 = time1.isAfter(time2);
      const diff2 = time1.isBefore(time2);
      if (diff1) {
        return 1;
      }
      if (diff2) {
        return -1;
      }
      return 0;
    });

    let flag = false;
    const tmpTimes = intervals.flat();
    tmpTimes.forEach((item: any, index: any) => {
      if (flag) {
        return;
      } else if (!index || index === tmpTimes.length - 1) {
        return;
      } else if (
        moment(tmpTimes[index], 'hh:mm:ss').isAfter(moment(tmpTimes[index + 1], 'hh:mm:ss'))
      ) {
        flag = true;
      }
    });
    return flag;
  };

  const handleCancel = useCallback(() => {
    setIsModalVisible(false);
  }, []);

  const handleSubmit = (data: any) => {
    console.log(data, 'data');
    if (timeType == ECustomTimeType.PeriodicTime) {
      const overTime: any = [];
      Object.keys(data).forEach((key) => {
        if (key.includes('time')) {
          overTime.push(data[key]);
        }
      });
      const isOverlap = overlapTimes(overTime);
      if (isOverlap) {
        message.error('时间有重叠，请重新选择！');
        return;
      }
    }
    setIsModalVisible(true);
  };

  const submitData = async () => {
    const formValue = form.getFieldsValue();
    const customTimeSetting: any = [];
    if (timeType == ECustomTimeType.PeriodicTime) {
      Object.keys(formValue).forEach((key, index) => {
        if (key.includes('time')) {
          const element = formValue[key];
          customTimeSetting.push({
            [`start_time_${index}`]: element[0],
            [`end_time_${index}`]: element[1],
          });
          delete formValue[key];
        }
      });
    }
    if (timeType == ECustomTimeType.DisposableTime) {
      const disposeTimes = formValue.disposeTime;
      const TimeObj = { start_time_1: disposeTimes[0], end_time_1: disposeTimes[1] };
      customTimeSetting.push(TimeObj);
      delete formValue.disposeTime;
    }
    const data: TimeConfigItem = {
      ...formValue,
      type: formValue.type == ECustomTimeType.PeriodicTime ? '0' : '1',
      period: JSON.stringify(formValue.period),
      customTimeSetting: JSON.stringify(customTimeSetting),
    };
    if (EPageMode.Create == pageMode) {
      const { success } = await createSingleSelfdefinedTime(data);
      if (!success) {
        message.error('创建失败!');
        return;
      }
      message.success('创建成功!');
    }
    if (EPageMode.Update == pageMode) {
      const { success } = await updateSelfDefinedTimeDetail({ ...data, id: timeDetail?.id });
      if (!success) {
        message.error('编辑失败!');
      }
      message.success('编辑成功!');
    }
    history.goBack();
  };

  return (
    <>
      <Form
        {...formItemLayout}
        form={form}
        onFinish={handleSubmit}
        initialValues={timeDetail ? { ...timeDetail } : {}}
      >
        <Form.Item label="名称" name="name" rules={[{ required: true, message: '请输入名称' }]}>
          <Input placeholder="请输入名称" />
        </Form.Item>

        <Form.Item label="类型" name="type" rules={[{ required: true }]}>
          <Radio.Group options={timeTypeOptions} onChange={changeTimeTypeSetting} />
        </Form.Item>
        {timeType === ECustomTimeType.PeriodicTime &&
          timeArr.map((item, index) => {
            return (
              <Form.Item
                key={item.name}
                name={item.name}
                label={`请输入开始结束时间 - ${index + 1}`}
                rules={[{ required: true, message: '请输入周期性时间' }]}
              >
                <SimpleTime
                  onChange={() => {}}
                  show={index === 0}
                  addTimeItem={() => {
                    if (timeArr.length < 10) {
                      setTimeArr((prev: any) => {
                        prev.push({ name: `time: ${uuidv4()}` });
                        return [...prev];
                      });
                    } else {
                      message.info('时间段不超过10个!');
                    }
                  }}
                  moveTimeItem={() => {
                    setTimeArr((prev) => {
                      const res = prev.filter((reItem, reIndex) => reIndex !== index);
                      return [...res];
                    });
                  }}
                />
              </Form.Item>
            );
          })}

        {timeType === ECustomTimeType.DisposableTime ? (
          <Fragment>
            <Form.Item label="请选择开始结束时间" name="disposeTime">
              {/* <RangePicker showTime /> */}
              <FormDataRangePicker onChange={() => {}} />
            </Form.Item>
          </Fragment>
        ) : null}

        {timeType === ECustomTimeType.PeriodicTime ? (
          <Fragment>
            <Form.Item label="每周生效时间" name="period" rules={[{ required: true }]}>
              <Select
                placeholder="选择相应的星期数"
                mode="multiple"
                allowClear
                style={{ width: '100%' }}
              >
                {enumObj2List(week_Enum).map((item: any) => {
                  return (
                    <Select.Option key={item.value} value={item.value}>
                      {item.label}
                    </Select.Option>
                  );
                })}
              </Select>
            </Form.Item>
          </Fragment>
        ) : null}

        <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
          <Button type="primary" htmlType="submit" style={{ marginRight: '10px' }}>
            保存
          </Button>
          <Button
            htmlType="button"
            onClick={() => {
              history.push('/configuration/objects/selfDefinedTime');
            }}
          >
            返回
          </Button>
        </Form.Item>
      </Form>
      <Modal
        title="确定保存吗?"
        visible={isModalVisible}
        maskClosable={true}
        onCancel={handleCancel}
        onOk={submitData}
      />
    </>
  );
};

export default TimeSetting;
