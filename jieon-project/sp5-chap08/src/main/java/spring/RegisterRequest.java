package spring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;

    public boolean isPasswordEqualToConfirmPassword(){
        return password.equals(confirmPassword);
    }
}
