package com.vonage.smsjourney.adapter.out.persistence.adapter;

import com.vonage.smsjourney.adapter.out.persistence.repository.SpringDataSmsRepository;
import com.vonage.smsjourney.adapter.out.persistence.entity.SmsJpaEntity;
import com.vonage.smsjourney.adapter.out.persistence.mapper.SmsPersistenceMapper;
import com.vonage.smsjourney.application.port.out.repository.SmsRepositoryPort;
import com.vonage.smsjourney.domain.model.Sms;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Data
@RequiredArgsConstructor
public class SmsPersistenceAdapter implements SmsRepositoryPort {

    private final SpringDataSmsRepository repository;
    private final SmsPersistenceMapper mapper;

    @Override
    public Optional<Sms> findActiveById(Long smsId) {
        return repository.findBySmsIdAndDeletedIsFalse(smsId).map(mapper::toDomain);
    }

    @Override
    public List<Sms> findAllActive() {
        return repository.findAllByDeletedIsFalse().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Sms save(Sms sms) {
        SmsJpaEntity entity = mapper.toEntity(sms);
        return mapper.toDomain(repository.save(entity));
    }
}
