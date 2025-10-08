package me.trihung.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import me.trihung.dto.CheckInDTO;
import me.trihung.entity.CheckIn;

@Mapper(componentModel = "spring")
public interface CheckInMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "zone.name", target = "zoneName")
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.fullName", target = "buyerName")
    @Mapping(source = "buyer.email", target = "buyerEmail")
    CheckInDTO toDTO(CheckIn checkIn); 

    List<CheckInDTO> toDTOList(List<CheckIn> checkIns);
}