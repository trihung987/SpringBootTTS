package me.trihung.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import me.trihung.dto.OrderDto;
import me.trihung.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "priceZone", source = "zone.price")
    @Mapping(target = "nameZone", source = "zone.name")
    @Mapping(target = "nameEvent", source = "zone.event.eventName")
    @Mapping(target = "urlPayment", expression = "java(url)")
    OrderDto toDto(Order order, @Context String url);
     
//    @AfterMapping
//    default void addNote(@MappingTarget OrderDto dto, @Context String url) {
//        dto.setUrlPayment(url);
//        System.out.println("after map url "+url);
//    } 
}
