package com.example.expo.Config;

import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Demo data for the live demo: a handful of visitors with TODAY's plans, several published to
// "Find a group". The crowd layer is derived from the day's plans, so these plans fill it too.
// Idempotent: runs only once (guarded by the "sara_demo" account). Delete these accounts to reseed.
@Component
@Order(2)
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final VisitorRepository visitorRepository;
    private final PassportRepository passportRepository;
    private final RoadmapRepository roadmapRepository;
    private final VisitRepository visitRepository;
    private final PavilionRepository pavilionRepository;
    private final EntranceRepository entranceRepository;
    private final InterestRepository interestRepository;
    private final ReviewRepository reviewRepository;
    private final CrowdAlertRepository crowdAlertRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // The demo accounts and their plans are seeded once, the named admin is the marker.
        // To reset everything again later, delete the "reem_admin" account and restart.
        if(userRepository.findUserByUsername("reem_admin") == null){
            seedDemo();
        } else {
            repairOwnerlessDemoPlans();
        }
        // an empty passport gets a few past days, so a repeated stamp is there to see
        seedMissingPassportHistory();
    }

    private void seedDemo(){
        cleanExistingVisitorData(); // full reset: accounts, plans, visits, reviews, requests, alerts
        ensureAdmin();              // the named admin account

        List<Pavilion> pavilions = pavilionRepository.findAll();
        List<Interest> interests = interestRepository.findAll();
        List<Entrance> entrances = entranceRepository.findAll();
        if(pavilions.size() < 8 || entrances.isEmpty()){
            return; // base data not ready yet, try again next start
        }
        Entrance gate = entrances.get(0);
        LocalDate today = LocalDate.now();

        // a few "hot" pavilions most plans pass through, so the crowd layer lights up
        List<Pavilion> hot = pavilions.subList(0, 4);
        List<Pavilion> midA = pavilions.subList(4, 9);
        List<Pavilion> midB = pavilions.subList(9, 14);

        // 1) Sara — shared, shows WhatsApp, ALL
        Visitor sara = visitor("Sara Al-Qahtani", "sara_demo", "+966500000001", "FEMALE", "Saudi", 24, pick(interests, 0, 3));
        roadmap(sara, gate, join(hot, midA.subList(0, 2)), LocalTime.of(16, 0), LocalTime.of(20, 0),
                true, "ALL", true, "نحب التقنية والثقافة، نبدأ من البوابة الشمالية ٤ العصر 🌟");

        // 2) Noura — shared, SAME_GENDER, no contact
        Visitor noura = visitor("Noura Al-Harbi", "noura_demo", "+966500000002", "FEMALE", "Saudi", 22, pick(interests, 1, 4));
        roadmap(noura, gate, join(hot.subList(0, 2), midB.subList(0, 3)), LocalTime.of(16, 30), LocalTime.of(21, 0),
                true, "SAME_GENDER", false, "رحلة هادئة نركز على الفن والثقافة 🎨");

        // 3) Haya — shared, ALL, shows contact
        Visitor haya = visitor("Haya Al-Mutairi", "haya_demo", "+966500000003", "FEMALE", "Kuwaiti", 27, pick(interests, 2, 5));
        roadmap(haya, gate, join(hot, midB.subList(2, 4)), LocalTime.of(17, 0), LocalTime.of(21, 30),
                true, "ALL", true, "مجموعة بنات نكمل اليوم سوا، الكل مرحّب 💜");

        // 4) Fahad — shared, ALL, shows contact
        Visitor fahad = visitor("Fahad Al-Dossari", "fahad_demo", "+966500000004", "MALE", "Saudi", 29, pick(interests, 0, 2));
        roadmap(fahad, gate, join(hot, midA.subList(2, 4)), LocalTime.of(16, 0), LocalTime.of(20, 30),
                true, "ALL", true, "مهتم بالتقنية والرياضة، نلتقي ونزور الأجنحة سوا 🚀");

        // 5) Abdullah — NOT shared (adds to the crowd only)
        Visitor abdullah = visitor("Abdullah Al-Shehri", "abdullah_demo", "+966500000005", "MALE", "Emirati", 31, pick(interests, 3, 5));
        roadmap(abdullah, gate, join(hot, midA), LocalTime.of(17, 30), LocalTime.of(22, 0),
                false, "ALL", false, null);

        // 6) Layan — shared, ALL
        Visitor layan = visitor("Layan Al-Otaibi", "layan_demo", "+966500000006", "FEMALE", "Bahraini", 20, pick(interests, 1, 3));
        roadmap(layan, gate, join(hot.subList(1, 3), midB), LocalTime.of(15, 30), LocalTime.of(19, 30),
                true, "ALL", false, "أول زيارة لي، أدور أنضم لمجموعة لطيفة 😊");
    }

    // the named admin account (login: reem_admin / Admin@1234)
    private void ensureAdmin(){
        if(userRepository.findUserByUsername("reem_admin") != null){
            return;
        }
        User admin = new User();
        admin.setFullName("ريم الشهري");
        admin.setUsername("reem_admin");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setEmail("reem.alshehri@wijhah.app");
        admin.setPhone("+966500000000");
        admin.setRole("ADMIN");
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }

    // wipes every visitor and everything attached (plans, visits, passports, reviews, join requests, alerts),
    // and the VISITOR user accounts — in child -> parent order so foreign keys never block. ADMIN users stay.
    private void cleanExistingVisitorData(){
        joinRequestRepository.deleteAll();
        crowdAlertRepository.deleteAll();
        reviewRepository.deleteAll();
        visitRepository.deleteAll();
        roadmapRepository.deleteAll();
        passportRepository.deleteAll();
        visitorRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ===== helpers =====

    private Visitor visitor(String fullName, String username, String phone, String gender, String nationality,
                            int age, Set<Interest> interests){
        // skip if this account already exists (e.g. it was promoted to admin and kept by the cleanup)
        if(userRepository.findUserByUsername(username) != null || userRepository.findUserByEmail(username + "@wijhah.demo") != null){
            return null;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("Demo@1234"));
        user.setEmail(username + "@wijhah.demo");
        user.setPhone(phone);
        user.setRole("VISITOR");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        Visitor visitor = new Visitor();
        visitor.setAge(age);
        visitor.setGender(gender);
        visitor.setPreferredLanguage("AR");
        visitor.setNationality(nationality);
        visitor.setInterests(interests);
        visitor.setUser(user);
        Visitor saved = visitorRepository.save(visitor);

        Passport passport = new Passport();
        passport.setPassportNumber(String.format("EXPO30-%06d", saved.getId()));
        passport.setVisitor(saved);
        passportRepository.save(passport);
        return saved;
    }

    private void roadmap(Visitor visitor, Entrance gate, List<Pavilion> stops, LocalTime arrival, LocalTime departure,
                         boolean shared, String audience, boolean showContact, String note){
        if(visitor == null){
            return; // the visitor was skipped (already exists)
        }
        Roadmap roadmap = new Roadmap();
        roadmap.setVisitor(visitor);   // without an owner the plan never shows in "Find a group"
        roadmap.setVisitDate(LocalDate.now());
        roadmap.setPartyType("SOLO");
        roadmap.setPartySize(1);
        roadmap.setArrivalTime(arrival);
        roadmap.setDepartureTime(departure);
        roadmap.setExperienceTypes(new HashSet<>(List.of("CULTURAL", "ENTERTAINMENT")));
        roadmap.setVisitPace("RELAXED");
        roadmap.setAllowRevisits(false);
        roadmap.setAccessibilityNeeds(new HashSet<>());
        roadmap.setWhatsappAlerts(false);
        roadmap.setEntrance(gate);
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setAiSummary("خطة تجريبية لليوم");
        if(shared){
            roadmap.setIsShared(true);
            roadmap.setSharedAt(LocalDateTime.now());
            roadmap.setShareAudience(audience);
            roadmap.setShowContact(showContact);
            roadmap.setShareNote(note);
        }
        roadmapRepository.save(roadmap);

        int order = 0;
        for(Pavilion pavilion : stops){
            Visit visit = new Visit();
            visit.setRoadmap(roadmap);
            visit.setPavilion(pavilion);
            visit.setStopOrder(++order);
            visit.setIsVisited(false);
            visitRepository.save(visit);
        }
    }

    // ---- repair ----------------------------------------------------------------------
    // The first demo seed saved the six plans without an owner, and the "Find a group" query
    // only returns a plan that has one, so those plans were invisible. They were created in
    // this order, so they are handed back to their owners in it.
    private static final List<String> DEMO_USERNAMES = List.of(
            "sara_demo", "noura_demo", "haya_demo", "fahad_demo", "abdullah_demo", "layan_demo");

    private void repairOwnerlessDemoPlans(){
        List<Roadmap> ownerless = new ArrayList<>();
        for(Roadmap roadmap : roadmapRepository.findAll()){
            if(roadmap.getVisitor() == null){
                ownerless.add(roadmap);
            }
        }
        if(ownerless.isEmpty()){
            return;
        }
        ownerless.sort(Comparator.comparing(Roadmap::getId));
        for(int i = 0; i < ownerless.size(); i++){
            Roadmap roadmap = ownerless.get(i);
            Visitor owner = i < DEMO_USERNAMES.size() ? demoVisitor(DEMO_USERNAMES.get(i)) : null;
            if(owner == null){
                deleteRoadmap(roadmap); // no demo account to own it, and it is invisible anyway
            } else {
                roadmap.setVisitor(owner);
                roadmapRepository.save(roadmap);
            }
        }
    }

    private Visitor demoVisitor(String username){
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            return null;
        }
        for(Visitor visitor : visitorRepository.findAll()){
            if(visitor.getUser() != null && visitor.getUser().getId().equals(user.getId())){
                return visitor;
            }
        }
        return null;
    }

    private void deleteRoadmap(Roadmap roadmap){
        joinRequestRepository.deleteAll(joinRequestRepository.findByRoadmapOrderByCreatedAtDesc(roadmap));
        visitRepository.deleteAll(visitRepository.findVisitsByRoadmapOrderByStopOrderAsc(roadmap));
        roadmapRepository.delete(roadmap);
    }

    // ---- passport history ------------------------------------------------------------
    // A stamp repeats only when the SAME pavilion was checked into on two DIFFERENT days, and a
    // fresh database has no past day at all. So every passport that is still empty gets a short
    // history: one pavilion twice (the repeated stamp), one more, and one that is in no plan.
    private void seedMissingPassportHistory(){
        List<Pavilion> pavilions = pavilionRepository.findAll();
        if(pavilions.size() < 8){
            return; // base data not ready yet, try again next start
        }
        Pavilion repeated = pavilions.get(0);
        Pavilion second = pavilions.get(1);
        Pavilion offPlan = pavilions.get(pavilions.size() - 1); // no demo plan passes through this one
        for(Passport passport : passportRepository.findAll()){
            if(!visitRepository.findVisitsByPassportAndIsVisitedTrue(passport).isEmpty()){
                continue; // this passport already has visits, leave it alone
            }
            pastVisit(passport, repeated, 6, LocalTime.of(17, 20));
            pastVisit(passport, repeated, 3, LocalTime.of(19, 10)); // same pavilion again -> two stamps
            pastVisit(passport, second, 4, LocalTime.of(18, 40));
            pastVisit(passport, offPlan, 2, LocalTime.of(16, 15));
        }
    }

    // one past check-in, saved exactly the way a real check-in is saved: no roadmap, so this
    // also covers a pavilion that was never part of a plan
    private void pastVisit(Passport passport, Pavilion pavilion, int daysAgo, LocalTime time){
        Visit visit = new Visit();
        visit.setPassport(passport);
        visit.setPavilion(pavilion);
        visit.setIsVisited(true);
        visit.setVisitedAt(LocalDate.now().minusDays(daysAgo).atTime(time));
        visitRepository.save(visit);
    }

    // a safe sub-set of interests
    private Set<Interest> pick(List<Interest> interests, int from, int to){
        if(interests.isEmpty()){
            return new HashSet<>();
        }
        int a = Math.max(0, Math.min(from, interests.size()));
        int b = Math.max(a, Math.min(to, interests.size()));
        return new HashSet<>(interests.subList(a, b));
    }

    @SafeVarargs
    private final List<Pavilion> join(List<Pavilion>... groups){
        List<Pavilion> all = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for(List<Pavilion> group : groups){
            for(Pavilion pavilion : group){
                if(seen.add(pavilion.getId())){
                    all.add(pavilion);
                }
            }
        }
        return all;
    }
}
