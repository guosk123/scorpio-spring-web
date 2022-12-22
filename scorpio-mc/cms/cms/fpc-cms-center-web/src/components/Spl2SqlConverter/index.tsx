import { QuestionCircleOutlined } from '@ant-design/icons';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Mentions, Modal, message } from 'antd';
import React, { useState, useEffect, useCallback, Fragment } from 'react';
import { debounce } from 'lodash';
import Usage from './components/Usage';
import { connect } from 'dva';
import type { Dispatch } from 'redux';
import converter from './libs/converter';
import styles from './index.less';
import type { ApplicationItem } from '@/pages/app/Configuration/SAKnowledge/typings';

export type IFieldType = 'IPv4' | 'IPv6' | 'Array' | 'Array<IPv4>' | 'Array<IPv6>';
/**
 * 可搜索的字段
 */
export interface ISearchabldField {
  label: string;
  value: string;
  type?: IFieldType;
  description?: string;
}

interface IStandardProtocol {
  id: string;
  ipProtocol: string;
  l7Protocol: string;
  port: string;
  source: string;
  sourceText: string;
}

interface IDsl extends converter.IParseResult {}

interface SqlConverterProps {
  // 是否必须限制起止时间
  limitTime?: boolean;
  fields: ISearchabldField[];
  rows?: number; // 输入框的高度
  onlyFilter: boolean; // 是否只支持过滤条件
  onChange?: (value: any) => void;
  value?: {
    spl: string;
    dsl?: IDsl;
  };
  dispatch: Dispatch;
  standardProtocolModel: {
    allStandardProtocols: IStandardProtocol[];
  };
  allStandardProtocols: IStandardProtocol[];
}

let allStandardProtocols: {
  id: string;
  port: string;
}[] = [];

const allApplicationObj: Record<string, string> = {};

export const getFullSpl = (text: string) => {
  if (!text) {
    return '';
  }
  // 替换占位符
  const nextText = text.replace(
    /<标准端口>/g,
    allStandardProtocols.map((item: any) => item.port).join(','),
  );
  return nextText.replace(/@/g, '').replace(/#/g, '');
};

/**
 * DSL查询语句
 */
export const DEFAULT_SPL = '#| gentimes @start_time start=now-30m end=now';

export const DEFAULT_DSL = converter.parse(getFullSpl(`${DEFAULT_SPL}`), { json: true });

/**
 * 获取DSL转换结果
 * @param spl
 * @param replace 是否需要经历特殊符号的替换
 */
export const getSqlConverterResult = (spl: string, replace: boolean = true) => {
  try {
    const converterResult = converter.parse(replace ? getFullSpl(`${spl}`) : spl, { json: true });
    return {
      spl,
      dsl: converterResult,
    };
  } catch (error) {
    message.error('表达式解析失败，请按使用说明进行修改。');
    return {
      spl,
      dsl: '',
    };
  }
};

const SqlConverter: React.FC<SqlConverterProps> = ({
  dispatch,
  limitTime = true,
  onlyFilter = false,
  fields = [],
  value = { spl: '', dsl: {} as IDsl },
  onChange,
  rows = 3,
}) => {
  // 用户输入的查询语言
  const [spl, setSpl] = useState(value.spl);
  // 转换后的结果
  const [dsl, setDsl] = useState<IDsl | undefined>(value.dsl);
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
        value: '| search a = 1 AND b >= 2 AND c != 3',
        label: '搜索命令：查询字段。语法：<字段名> <操作符> <字段值>',
      },
      {
        value: '| gentimes start_time start=now-2h end=now',
        label: '搜索命令：时间范围。语法：| gentimes <时间字段> start=<开始时间> end=<结束时间>',
      },
    ];
  }

  useEffect(() => {
    if (!spl) {
      setDsl(undefined);
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

      ((dispatch({
        type: 'SAKnowledgeModel/queryAllApplications',
      }) as unknown) as Promise<any>).then(({ allApplicationList }) => {
        allApplicationList.forEach((app: ApplicationItem) => {
          allApplicationObj[app.applicationId] = app.nameText;
        });
      });
    }
  }, []);

  useEffect(() => {
    setSpl(value.spl);
  }, [value.spl]);

  useEffect(() => {
    debounceChange({ spl, dsl });
  }, [dsl]);

  const computeDsl = (splText: string) => {
    try {
      const fullSpl = getFullSpl(`${splText}`);
      const convertResult: IDsl = converter.parse(fullSpl, {
        json: true,
        applications: allApplicationObj,
      });
      const { result } = convertResult;
      const { fields: inputFields } = result.dev;
      const { GENTIMES: timeRange } = result.dev.expression;
      // 判断下有没有限制时间
      if (limitTime) {
        if (!timeRange.time_from || !timeRange.time_to) {
          setErrMsg('请限制时间范围');
          setDsl(undefined);
          return;
        }
      }

      // 判断字段是否在提示字段内
      if (fields.length > 0) {
        const autoCompleteFieldValues = fields.map(
          ({ value, type }) => value + (type ? `<${type}>` : ''),
        );
        const illegalFields: string[] = inputFields.filter(
          (field: string) => !autoCompleteFieldValues.includes(field),
        );

        if (illegalFields.length > 0) {
          setErrMsg(`${illegalFields.length}个字段不在允许范围内: ${illegalFields.join(',')}`);
          setDsl(undefined);
          return;
        }
      }

      setErrMsg('');
      setDsl(convertResult);
    } catch (error) {
      console.error('spl conver error', error);
      setDsl(undefined);
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

  const triggerChange = (changedValue: object) => {
    if (onChange) {
      onChange({
        ...value,
        ...changedValue,
      });
    }
  };

  const debounceChange = useCallback(
    // 移除防抖，防抖回导致光标直接跳转到最后
    debounce((q: object) => triggerChange(q), 0),
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
    <Form>
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
        autoFocus
      >
        {(dataSource[prefix] || []).map((item: ISearchabldField) => (
          <Mentions.Option
            key={item.value}
            value={item.value + (item.type ? `<${item.type}>` : '')}
          >
            <span>{item.value + (item.type ? `<${item.type}>` : '')}</span>
            <span className={styles.desc}>[{item.label}]</span>
            {item.description && <div className={styles.desc}>{item.description}</div>}
          </Mentions.Option>
        ))}
      </Mentions>
      {errMsg && <p className={styles.error}>{errMsg}</p>}
    </Form>
  );
};

export default connect()(SqlConverter);
