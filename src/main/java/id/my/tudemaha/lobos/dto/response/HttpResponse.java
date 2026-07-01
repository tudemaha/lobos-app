package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

@Data
public class HttpResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String[] errors;

    public HttpResponse(boolean success, String message, T data, String[] errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> HttpResponse<T> success(T data) {
        return new HttpResponse<>(true, "request successfully", data, null);
    }

    public static <T> HttpResponse<T> success(String message, T data) {
        return new HttpResponse<>(true, message, data, null);
    }

    public static <T> HttpResponse<T> error(String message, String[] errors) {
        return new HttpResponse<>(false, message, null, errors);
    }
}
