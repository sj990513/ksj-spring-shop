package springshopksj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springshopksj.entity.Authnumber;
import springshopksj.repository.AuthnumberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthnumberRepository authnumberRepository;

    public void saveAuthNumber(String phoneNumber, String code) {
        Authnumber authenticationNumber = Authnumber.builder()
                .phonenumber(phoneNumber)
                .code(code)
                .build();
        authnumberRepository.save(authenticationNumber);
    }

    public Authnumber findByPhoneNumber(String phoneNumber) {
        return authnumberRepository.findByPhonenumber(phoneNumber);
    }

    public void deleteAuthNumber(String phoneNumber) {
        authnumberRepository.deleteByPhonenumber(phoneNumber);
    }
}