package api.mappings;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ErrorResponse {

    //TODO: update this with the error Response from your API
    private Timestamp timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}
