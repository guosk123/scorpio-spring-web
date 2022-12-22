import { QuestionCircleOutlined } from '@ant-design/icons';
import { Mentions, Modal, message } from 'antd';
import React, { useState, useEffect, useCallback, Fragment } from 'react';
import { debounce } from 'lodash';
import Usage from './components/Usage';
import { connect } from 'dva';
import type { Dispatch } from 'redux';
import styles from './index.less';

const converter = require('./libs/splunk-spl-to-es-dsl-converter.min');

interface FieldItemType {
  label: string;
  value: string;
}

interface IStandardProtocol {
  id: string;
  ipProtocol: string;
  l7Protocol: string;
  port: string;
  source: string;
  sourceText: string;
}

interface ConverterProps {
  // 是否必须限制起止时间
  limitTime?: boolean;
  fields: FieldItemType[];
  rows?: number; // 输入框的高度
  onlyFilter: boolean; // 是否只支持过滤条件
  onParser?: (dslParse: EsDslType) => void;
  onChange?: (value: any) => void;
  value?: {
    spl: string;
    esDsl: Record<string, any>;
  };
  dispatch: Dispatch;
  standardProtocolModel: {
    allStandardProtocols: IStandardProtocol[];
  };
  allStandardProtocols: IStandardProtocol[];
}

export interface EsDslType {
  dev?: Record<string, any>;
  target?: Record<string, any>;
}

let allStandardProtocols: {
  id: string;
  port: string;
}[] = [];

const fullString = (text: string) => {
  if (!text) {
    return '';
  }

  // 替换占位符
  // const { allStandardProtocols = [] } =
  // window.g_app && window.g_app._store.getState().standardProtocolModel;
  // eslint-disable-next-line no-param-reassign
  text = text.replace(/<标准端口>/g, allStandardProtocols.map((item: any) => item.port).join(','));

  return text.replace(/@/g, '').replace(/#/g, '');
};

/**
 * 前缀。这里的表名随便写即可
 */
const SOURCE = 'source=tableName';
/**
 * DSL查询语句
 */
export const DEFAULT_SPL =
  '#| sort -@start_time #| gentimes @start_time start=now-2d end=now #| head 20';

export const DEFAULT_DSL = converter.parse(fullString(`${SOURCE} ${DEFAULT_SPL}`));

export const getSplParser = (spl: string) => {
  try {
    const result = converter.parse(fullString(`${SOURCE} ${spl}`));
    return result;
  } catch (error) {
    message.error('表达式解析失败，请按使用说明进行修改。');
    return null;
  }
};

const Converter: React.FC<ConverterProps> = ({
  dispatch,
  limitTime = true,
  onlyFilter = false,
  fields = [],
  onParser,
  value = { spl: '', esDsl: {} },
  onChange,
  rows = 5,
}) => {
  // 用户输入的查询语言
  const [spl, setSpl] = useState(value.spl);
  // 转换后的结果
  const [esDsl, setEsDsl] = useState(value.esDsl);
  // 错误信息
  const [errMsg, setErrMsg] = useState('');
  // 搜索前缀
  const [prefix, setPrefix] = useState('@');

  // 自动提示数据
  const dataSource = {
    '@': fields,
  };

  if (!onlyFilter) {
    dataSource['#'] = [
      {
        value: 'a = 1 AND b >= 2 AND c != 3',
        label: '搜索命令：查询字段。语法：<字段名> <操作符> <字段值>',
      },
      { value: '| head 30', label: '搜索命令：返回前N个结果。语法：| head <数字>' },
      { value: '| sort -start_time', label: '搜索命令：排序。语法：| sort <+/-><字段名>' },
      {
        value: '| gentimes start_time start=now-2h end=now',
        label: '搜索命令：时间范围。语法：| gentimes <时间字段> start=<开始时间> end=<结束时间>',
      },
      {
        value: '| timeout 30s',
        label: '搜索命令：设置查询超时时间。语法：| timeout <数字><单位: m/ms>',
      },
      {
        value: '| terminate_after 100000000',
        label: '搜索命令：设置每分片查询结果上限。语法：| terminate_after <数字>',
      },
    ];
  }

  useEffect(() => {
    if (!spl) {
      setEsDsl({});
      setErrMsg('请输入查询表达式');
    } else {
      computeDsl(spl);
    }
  }, [spl]);

  useEffect(() => {
    if (dispatch) {
      ((dispatch({
        type: 'standardProtocolModel/queryAllStandardProtocols',
      }) as unknown) as Promise<any>).then((result) => {
        allStandardProtocols = result;
      });
    }
  }, []);

  useEffect(() => {
    setSpl(value.spl);
  }, [value.spl]);

  useEffect(() => {
    if (onParser) {
      onParser(esDsl);
    }

    debounceChange({ spl, esDsl });
  }, [esDsl]);

  const computeDsl = (splText: string) => {
    try {
      const fullSpl = fullString(`${SOURCE} ${splText}`);
      const result = converter.parse(fullSpl);
      const { time_range: timeRange, fields: inputFields } = result.dev;
      // 判断下有没有限制时间
      if (limitTime) {
        if (!timeRange.time_from || !timeRange.time_to) {
          setErrMsg('请限制时间范围');
          setEsDsl({});
          return;
        }
      }

      // 判断字段是否在提示字段内
      if (fields.length > 0) {
        const autoCompleteFieldValues = fields.map((item) => item.value);
        const illegalFields: string[] = inputFields.filter(
          (field: string) => !autoCompleteFieldValues.includes(field),
        );

        if (illegalFields.length > 0) {
          setErrMsg(`${illegalFields.length}个字段不在允许范围内: ${illegalFields.join(',')}`);
          setEsDsl({});
          return;
        }
      }

      setErrMsg('');
      setEsDsl({ ...result });
    } catch (error) {
      setEsDsl({});
      setErrMsg(`表达式解析失败，请按使用说明进行修改。错误：${error.message}`);
    }
  };

  const handleSplChange = (newSpl: string) => {
    setSpl(newSpl);
    computeDsl(newSpl);
  };

  const handleSearch = (_: string, curPrefix: string) => {
    setPrefix(curPrefix);
  };

  const triggerChange = (changedValue: Record<string, any>) => {
    if (onChange) {
      onChange({
        ...value,
        ...changedValue,
      });
    }
  };

  const debounceChange = useCallback(
    debounce((q: Record<string, any>) => triggerChange(q), 300),
    [],
  );

  const handleOpenExplanModal = () => {
    Modal.info({
      title: (
        <Fragment>
          查询表达式使用说明 <span className={styles.desc}>[按 ESC 退出]</span>
        </Fragment>
      ),
      width: '80%',
      content: (
        <div>
          <Usage onlyFilter={onlyFilter} />
        </div>
      ),
    });
  };

  return (
    <>
      <div className={styles.explanation}>
        {!onlyFilter && (
          <Fragment>
            <QuestionCircleOutlined /> 输入【<code>#</code>】提示搜索命令，
          </Fragment>
        )}
        输入【
        <code>@</code>】提示可用字段。点击
        <span style={{ color: 'red', cursor: 'pointer' }} onClick={handleOpenExplanModal}>
          {' '}
          这里{' '}
        </span>
        查看详细的使用说明。
      </div>
      <Mentions
        placeholder="请输入查询表达式"
        prefix={['@', '#']}
        split=""
        rows={rows}
        value={spl}
        onChange={handleSplChange}
        onSearch={handleSearch}
      >
        {(dataSource[prefix] || []).map((item: FieldItemType) => (
          <Mentions.Option value={item.value}>
            <span>{item.value}</span>
            <span className={styles.desc}>[{item.label}]</span>
          </Mentions.Option>
        ))}
      </Mentions>
      <p className={styles.error}>{errMsg}</p>
    </>
  );
};

export default connect()(Converter);
