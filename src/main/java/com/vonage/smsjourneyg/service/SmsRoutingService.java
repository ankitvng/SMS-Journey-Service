package com.vonage.smsjourneyg.service;

import com.vonage.smsjourneyg.entity.Sms;
import com.vonage.smsjourneyg.provider.SmsProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class SmsRoutingService {

        private final Map<String, SmsProvider> providers = new HashMap<>();
        private final List<SmsProvider> providerList;

        public SmsRoutingService(List<SmsProvider> providerList) {
            this.providerList = providerList;
        }

        @PostConstruct
        public void initializeProviders() {
            for (SmsProvider provider : providerList) {
                providers.put(provider.getProviderName(), provider);
            }
        }

        public boolean sendSms(String route, Sms sms) {
            if (route == null) {
                throw new IllegalArgumentException("Route cannot be null");
            }

            SmsProvider provider = providers.get(route.toUpperCase());
            if (provider == null) {
                throw new IllegalArgumentException("Unknown SMS route: " + route);
            }

            return provider.send(sms);
        }



//    public boolean sendSms(String route, Sms sms) {
//
//        log.info(
//                "Attempting to send SMS {} through route {}",
//                sms.getSmsId(),
//                route
//        );
//
//        // Simulate provider
//
//        if ("VONAGE".equals(route)) {
//            return true;
//        }
//
//        return false;
//    }
}
