package com.scorpio.exception;

public final class ErrorCode {

    public static final String BASE = "100";

    public static final String COMMON_BASE = BASE + "000";
    public static final String COMMON_BASE_EXCEPTION = COMMON_BASE + "0001";// 系统运行期异常
    public static final String COMMON_BASE_OBJECT_NOT_FOUND = COMMON_BASE + "0002"; // 对象不存在
    public static final String COMMON_BASE_OBJECT_DUPLICATE = COMMON_BASE + "0003"; // 同名实体已存在
    public static final String COMMON_BASE_FORMAT_INVALID = COMMON_BASE + "0004"; // 格式错误
    public static final String COMMON_BASE_OPERATION_NOT_SUPPORT = COMMON_BASE + "0005"; // 不支持的操作
    public static final String COMMON_BASE_API_INVOKE_ERROR = COMMON_BASE + "0006"; // API调用异常
    public static final String COMMON_BASE_COMMAND_RUN_ERROR = COMMON_BASE + "0007"; // 命令执行异常

    private ErrorCode() {
        throw new IllegalStateException("Utility class");
    }
}
