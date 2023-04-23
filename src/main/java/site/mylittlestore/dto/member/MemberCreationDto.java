package site.mylittlestore.dto.member;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import site.mylittlestore.domain.Address;
import site.mylittlestore.domain.Store;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
public class MemberCreationDto {
    @NotBlank
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String city;
    @NotBlank
    private String street;
    @NotBlank
    private String zipcode;

    @Builder
    protected MemberCreationDto(String name, String email, String password, String city, String street, String zipcode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
