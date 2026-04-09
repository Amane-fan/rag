package com.usts.rag.common.api;

/**
 * 统一接口返回体。
 *
 * @param success 是否成功
 * @param code    业务状态码
 * @param message 提示信息
 * @param data    返回数据
 * @param <T>     数据类型
 */
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "OK", data);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
