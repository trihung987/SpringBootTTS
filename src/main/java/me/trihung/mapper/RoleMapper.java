package me.trihung.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import me.trihung.entity.Role;
import me.trihung.entity.RoleDTO;

@Mapper(componentModel = "spring")
public interface RoleMapper { 
    RoleDTO toDto(Role role);
    Role toEntity(RoleDTO dto);
}
