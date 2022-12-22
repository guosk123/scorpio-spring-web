declare namespace Spl2SqlConverter {
  interface IParseOptions {
    /**
     * 结果是否返回json
     */
    json?: boolean;
    /**
     * 应用
     */
    applications?: Record<string | number, string>;
  }
  interface IParseResult {
    result: {
      source: string;
      target: string;
      params: Record<string, string | (string | number)[]>;
      dev: {
        expression: {
          WHERE: string;
          ORDER_BY: string;
          LIMIT: string;
          GENTIMES: {
            time_field: string;
            time_from: number;
            time_to: number;
          };
          COLUMNS: string;
        };
        fields: string[];
      };
    };
  }

  interface IParseErrorMsg {
    expected: { type: 'end' | 'start' }[];
    found: string;
    location: {
      start: { offset: number; line: number; column: number };
      end: { offset: number; line: number; column: number };
    };
    message: string;
    name: string;
    stack: string;
  }

  function parse(spl: string, options?: IParseOptions): IParseResult;
}

// or export default foo; 导出npm模块
export = Spl2SqlConverter;
// 全局导出
export as namespace Spl2SqlConverter;
