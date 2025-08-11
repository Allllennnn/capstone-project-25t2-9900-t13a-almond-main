package demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;  // response code: 1 = success, 0 = failure
    private String msg;    // response message
    private Object data;   // response payload

    /** Success response for create, update, delete operations */
    public static Result success() {
        return new Result(1, "success", null);
    }

    /** Success response with data payload */
    public static Result success(Object data) {
        return new Result(1, "success", data);
    }

    /** Error response with custom message */
    public static Result error(String msg) {
        return new Result(0, msg, null);
    }
}
