package id.my.tudemaha.lobos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpResponse<T> {
    private boolean success;
    private String code;
    private T data;
    private List<String> errors;

    public HttpResponse(boolean success, String code, T data, List<String> errors) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.errors = errors;
    }

    public static <T> HttpResponse<T> success(T data) {
        return new HttpResponse<>(true, null, data, null);
    }

    public static <T> HttpResponse<T> success() {
        return new HttpResponse<>(true, null, null, null);
    }

    public static <T> HttpResponse<T> error(String code, List<String> errors) {
        return new HttpResponse<>(false, code, null, errors);
    }

    public static <T> HttpResponse<T> error(String code, String error) {
        return new HttpResponse<>(false, code, null, error != null ? List.of(error) : null);
    }
}
