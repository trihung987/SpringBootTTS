package me.trihung.mapper;

import javax.annotation.processing.Generated;
import me.trihung.dto.UserDTO;
import me.trihung.dto.UserDTO.UserDTOBuilder;
import me.trihung.dto.request.SignUpRequest;
import me.trihung.entity.User;
import me.trihung.entity.User.UserBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-14T20:52:38+0700",
    comments = "version: 1.4.1.Final, compiler: Eclipse JDT (IDE) 3.39.0.v20240820-0604, environment: Java 23.0.1 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTOBuilder<?, ?> userDTO = UserDTO.builder();

        userDTO.createdBy( user.getCreatedBy() );
        userDTO.createdDate( user.getCreatedDate() );
        userDTO.lastModifiedBy( user.getLastModifiedBy() );
        userDTO.updatedDate( user.getUpdatedDate() );
        userDTO.avatarUrl( user.getAvatarUrl() );
        userDTO.email( user.getEmail() );
        userDTO.fullName( user.getFullName() );
        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );

        return userDTO.build();
    }

    @Override
    public User toEntity(SignUpRequest signUpRequest) {
        if ( signUpRequest == null ) {
            return null;
        }

        UserBuilder user = User.builder();

        user.fullName( signUpRequest.getFullname() );
        user.email( signUpRequest.getEmail() );
        user.password( signUpRequest.getPassword() );
        user.username( signUpRequest.getUsername() );

        return user.build();
    }
}
