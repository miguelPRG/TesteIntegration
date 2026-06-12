package api.validators;

import api.mappings.ErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseValidator {

    public static void assertErrorResponse(ErrorResponse errorResponse, String message, String path) {
        //TODO: update this with the parameters you want to validate on error responses
        assertThat(String.format("Message should be %s", message), errorResponse.getMessage(), is(message));
        assertThat(String.format("path should be %s", message), errorResponse.getPath(), is(path));
    }
}
