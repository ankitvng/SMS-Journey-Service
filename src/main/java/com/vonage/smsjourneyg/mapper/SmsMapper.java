package com.vonage.smsjourneyg.mapper;

import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.enums.SmsStatus;
import com.vonage.smsjourneyg.dto.SmsReceivedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {SmsStatus.class})
public interface SmsMapper {

    @Mapping(target = "smsId", ignore = true) // Primary key (generated)
    @Mapping(target = "externalSmsId", expression = "java(String.valueOf(event.getSmsId()))")
    @Mapping(target = "createdAt", source = "receivedAt")
    @Mapping(target = "status", expression = "java(SmsStatus.SCHEDULED)")
    Sms toEntity(SmsReceivedEvent event);
}