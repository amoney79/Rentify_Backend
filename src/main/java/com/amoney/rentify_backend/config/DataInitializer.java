package com.amoney.rentify_backend.config;

import com.amoney.rentify_backend.model.*;
import com.amoney.rentify_backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final VerificationProfileRepository profileRepository;
    private final OwnershipVerificationRepository ownershipRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ViewingRequestRepository viewingRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            VerificationProfileRepository profileRepository,
            OwnershipVerificationRepository ownershipRepository,
            ChatMessageRepository chatMessageRepository,
            ViewingRequestRepository viewingRequestRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.profileRepository = profileRepository;
        this.ownershipRepository = ownershipRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.viewingRequestRepository = viewingRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 1. Admin & Verification Officer
        User admin = User.builder()
                .email("admin@rentify.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Chief Verification Officer")
                .role(Role.ROLE_ADMIN)
                .identityStatus(IdentityStatus.VERIFIED_IDENTITY)
                .avatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuD68fJB-SrzHOyOdCdgNYcJm9qKNPc-0Qt9Z937_5eFZgwhXY8OUccaceUqVLxAn6pez9xeFx-G0gsM8jwbCzI3TaeyxD08-OgIGzyTxeg2R6zaMF_Zff1C50RxgfXiJOh90JTGn9qb-YZkoO0qwYRpvMVA5aufouOtXMKlko1N5W_GTyjdGjK7K04MD6Ha6V0Hb-tHtn8AbdeJDBfH2AvTYq1LuyQMuu0Zhg9ZqL8lQxcXXc11uVau3KTbYpo9fcdCgZbO-duwsdg")
                .build();
        userRepository.save(admin);

        // 2. Real Estate Agent (Sarah Jenkins / Johnson)
        User agent = User.builder()
                .email("sarah.agent@rentify.com")
                .password(passwordEncoder.encode("agent123"))
                .fullName("Sarah Johnson")
                .phone("+254 712 345 678")
                .role(Role.ROLE_AGENT)
                .identityStatus(IdentityStatus.VERIFIED_IDENTITY)
                .agentStatus(AgentStatus.VERIFIED_AGENT)
                .idNumber("28945102")
                .county("Nairobi")
                .agencyName("Prime Habitat Realtors")
                .agencyLicenseNumber("EARB-2024-4492")
                .avatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuDuEATf7dZYNF-h_g3rZE-xxG7ykTdRZFUG6PiEwuhbQeRUfwPQkHmvj8Ltl3hcUF-iTKvo02OOnqk6ipPT7UPSrQmSTpiUBfbt4VlVxBJJoBslFpPaeVjHb83PU-pzyJMnu4a1vO671EaEGBXgPgwc5xl2NqqBVZAdQUynXGt5bSymKFwz-iCx-YuCm45pKTfJ9ihGVcthBX__cFtvHQ8Rgx-xySw4UzDB0lFyx7MiDYdF9TueVB2yTOPY2p-fzDE1-_6xS3A6HR8")
                .build();
        userRepository.save(agent);

        // 3. Private Landlord (David Omwamba)
        User landlord = User.builder()
                .email("david.landlord@rentify.com")
                .password(passwordEncoder.encode("landlord123"))
                .fullName("David Omwamba")
                .phone("+254 722 987 654")
                .role(Role.ROLE_LANDLORD)
                .identityStatus(IdentityStatus.VERIFIED_IDENTITY)
                .idNumber("18492044")
                .county("Nairobi")
                .avatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuAU0WBI3Zsk8CoNujzvZLFNYa55VlCKsC-PgS8OykghBr2dIoDRpmDhsZZGFeCSAAljCS1SY3iYHPblE9nFHhSkLLjVlqzuw-VVNfJbQ62bjUurZFCCuDJGw4d-_awUQgCtLtOZ0eWc2x6LPe9epEt6dfhVCcOgJHrQmPUDVJ5MvXLDL1L8BNQJS3qIFgu9QOfUUZeZltgI1GzA_C5nmxpSoX8KKlrjWlw-9NT4KmbLXbYknDgsJSHziHJ6AwFGYPRj_VY6rwzJ0sM")
                .build();
        userRepository.save(landlord);

        // 4. Tenant (James Mwangi)
        User tenant = User.builder()
                .email("james.tenant@rentify.com")
                .password(passwordEncoder.encode("tenant123"))
                .fullName("James Mwangi")
                .phone("+254 733 111 222")
                .role(Role.ROLE_TENANT)
                .identityStatus(IdentityStatus.VERIFIED_IDENTITY)
                .idNumber("33104958")
                .avatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuAU0WBI3Zsk8CoNujzvZLFNYa55VlCKsC-PgS8OykghBr2dIoDRpmDhsZZGFeCSAAljCS1SY3iYHPblE9nFHhSkLLjVlqzuw-VVNfJbQ62bjUurZFCCuDJGw4d-_awUQgCtLtOZ0eWc2x6LPe9epEt6dfhVCcOgJHrQmPUDVJ5MvXLDL1L8BNQJS3qIFgu9QOfUUZeZltgI1GzA_C5nmxpSoX8KKlrjWlw-9NT4KmbLXbYknDgsJSHziHJ6AwFGYPRj_VY6rwzJ0sM")
                .build();
        userRepository.save(tenant);

        // 5. Sample Properties
        Property p1 = Property.builder()
                .landlord(agent)
                .title("Silver Towers Unit 4B")
                .description("Luxurious and serene 2 bedroom master en-suite apartment in the heart of Kilimani with high-speed elevators, backup generator, heated pool, and 24/7 biometric security.")
                .propertyType("Apartment")
                .county("Nairobi")
                .sublocation("Kilimani")
                .address("Silver Towers, Denis Pritt Road, Kilimani")
                .price(new BigDecimal("75000"))
                .bedrooms(2)
                .bathrooms(2)
                .imageUrl("https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=1200&q=80")
                .verificationStatus(PropertyVerificationStatus.ELIGIBLE_FOR_REVIEW)
                .isPubliclyVisible(true)
                .build();
        propertyRepository.save(p1);

        Property p2 = Property.builder()
                .landlord(landlord)
                .title("Kileleshwa Executive Penthouse")
                .description("Panoramic views of Nairobi skyline with wrap-around balconies and private rooftop terrace.")
                .propertyType("Penthouse")
                .county("Nairobi")
                .sublocation("Kileleshwa")
                .address("Mandera Road, Kileleshwa")
                .price(new BigDecimal("120000"))
                .bedrooms(3)
                .bathrooms(3)
                .imageUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=1200&q=80")
                .verificationStatus(PropertyVerificationStatus.PENDING_REVIEW)
                .isPubliclyVisible(false)
                .build();
        propertyRepository.save(p2);

        // 6. Verification Profiles
        VerificationProfile agentProfile = VerificationProfile.builder()
                .user(agent)
                .status(IdentityStatus.VERIFIED_IDENTITY)
                .idDocumentType(DocumentType.NATIONAL_ID_FRONT)
                .idNumber("28945102")
                .extractedFullName("Sarah Johnson")
                .extractedDob(LocalDate.of(1993, 4, 18))
                .faceMatchScore(0.984)
                .livenessScore(0.992)
                .overallConfidence(0.988)
                .isLive(true)
                .spoofDetected(false)
                .antiSpoofDetails("Liveness verified. Genuine live human face.")
                .submittedAt(LocalDateTime.now().minusHours(4))
                .reviewedAt(LocalDateTime.now().minusHours(2))
                .reviewer(admin)
                .build();
        profileRepository.save(agentProfile);

        VerificationProfile landlordProfile = VerificationProfile.builder()
                .user(landlord)
                .status(IdentityStatus.VERIFIED_IDENTITY)
                .idDocumentType(DocumentType.NATIONAL_ID_FRONT)
                .idNumber("18492044")
                .extractedFullName("David Omwamba")
                .extractedDob(LocalDate.of(1982, 11, 5))
                .faceMatchScore(0.976)
                .livenessScore(0.985)
                .overallConfidence(0.980)
                .isLive(true)
                .spoofDetected(false)
                .submittedAt(LocalDateTime.now().minusHours(6))
                .build();
        profileRepository.save(landlordProfile);

        // 7. Ownership Verifications (for Admin Approval Queue)
        OwnershipVerification ov1 = OwnershipVerification.builder()
                .property(p1)
                .applicantUser(agent)
                .ownershipType(OwnershipType.AGENT)
                .extractedOwnerName("Silver Towers Holdings Ltd")
                .extractedTitleNumber("NAIROBI/BLOCK82/4492")
                .extractedPlotNumber("Plot 4492")
                .extractedParcelNumber("Parcel 82/4492")
                .extractedCounty("Nairobi")
                .nameMatchScore(0.96)
                .ownershipConfidenceScore(0.94)
                .specialScenarioNotes("EARB agent contract and owner management agreement attached.")
                .status(PropertyVerificationStatus.ELIGIBLE_FOR_REVIEW)
                .submittedAt(LocalDateTime.now().minusHours(2))
                .build();
        ownershipRepository.save(ov1);

        OwnershipVerification ov2 = OwnershipVerification.builder()
                .property(p2)
                .applicantUser(landlord)
                .ownershipType(OwnershipType.INDIVIDUAL)
                .extractedOwnerName("David Omwamba")
                .extractedTitleNumber("NAIROBI/BLOCK51/104")
                .extractedPlotNumber("Plot 104")
                .extractedParcelNumber("Parcel 51/104")
                .extractedCounty("Nairobi")
                .nameMatchScore(0.98)
                .ownershipConfidenceScore(0.96)
                .status(PropertyVerificationStatus.PENDING_REVIEW)
                .submittedAt(LocalDateTime.now().minusHours(5))
                .build();
        ownershipRepository.save(ov2);

        // 8. Sample Viewing Request and Chat History
        ViewingRequest vr = ViewingRequest.builder()
                .property(p1)
                .tenant(tenant)
                .landlord(agent)
                .viewingDate(LocalDate.now().plusDays(2))
                .viewingTime("10:00 AM")
                .notes("Confirming Saturday morning viewing.")
                .status(ViewingStatus.ACCEPTED)
                .build();
        viewingRequestRepository.save(vr);

        String convId = Math.min(tenant.getId(), agent.getId()) + "_" + Math.max(tenant.getId(), agent.getId());

        ChatMessage m1 = ChatMessage.builder()
                .conversationId(convId)
                .sender(agent)
                .recipient(tenant)
                .content("Hello James! The apartment in Kilimani is available for viewing this weekend. Shall we schedule a time? 🏢")
                .messageType(MessageType.CHAT)
                .isRead(true)
                .timestamp(LocalDateTime.now().minusMinutes(45))
                .build();
        chatMessageRepository.save(m1);

        ChatMessage m2 = ChatMessage.builder()
                .conversationId(convId)
                .sender(tenant)
                .recipient(agent)
                .content("Yes, Saturday morning at 10 AM works perfectly for me. Can you confirm the location Pin? 📍")
                .messageType(MessageType.CHAT)
                .isRead(true)
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .build();
        chatMessageRepository.save(m2);

        ChatMessage m3 = ChatMessage.builder()
                .conversationId(convId)
                .sender(tenant)
                .recipient(agent)
                .content("📅 Viewing Request for Silver Towers Unit 4B on Saturday at 10:00 AM.")
                .messageType(MessageType.VIEWING_REQUEST)
                .viewingRequest(vr)
                .isRead(true)
                .timestamp(LocalDateTime.now().minusMinutes(25))
                .build();
        chatMessageRepository.save(m3);
    }
}
