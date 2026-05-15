package com.tfg.rentalplatform.config;

import com.tfg.rentalplatform.entity.*;
import com.tfg.rentalplatform.repository.ItemRepository;
import com.tfg.rentalplatform.repository.NotificationRepository;
import com.tfg.rentalplatform.repository.PaymentRepository;
import com.tfg.rentalplatform.repository.ReservationRepository;
import com.tfg.rentalplatform.repository.ReviewRepository;
import com.tfg.rentalplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            // Solo se cargan datos de demostracion en bases nuevas.
            // Asi no se pisan usuarios ni reservas reales si la BD ya tiene contenido.
            if (userRepository.count() > 0) {
                return;
            }

            // Usuarios de prueba para probar rapidamente los roles principales:
            // administrador, propietarios y arrendatarios.
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin = userRepository.save(admin);

            User owner = new User();
            owner.setName("Ana");
            owner.setEmail("ana@test.com");
            owner.setPassword(passwordEncoder.encode("1234"));
            owner.setRole(UserRole.USER);
            owner = userRepository.save(owner);

            User renter = new User();
            renter.setName("Luis");
            renter.setEmail("luis@test.com");
            renter.setPassword(passwordEncoder.encode("1234"));
            renter.setRole(UserRole.USER);
            renter = userRepository.save(renter);

            User owner2 = new User();
            owner2.setName("Marta");
            owner2.setEmail("marta@test.com");
            owner2.setPassword(passwordEncoder.encode("1234"));
            owner2.setRole(UserRole.USER);
            owner2 = userRepository.save(owner2);

            User renter2 = new User();
            renter2.setName("Carlos");
            renter2.setEmail("carlos@test.com");
            renter2.setPassword(passwordEncoder.encode("1234"));
            renter2.setRole(UserRole.USER);
            renter2 = userRepository.save(renter2);

            // Catalogo inicial con objetos en distintos estados para mostrar casos reales en la interfaz.
            Item drill = new Item();
            drill.setOwner(owner);
            drill.setTitle("Taladro Bosch");
            drill.setDescription("Taladro percutor con maletín, brocas de pared y batería cargada. Ideal para montar muebles o pequeñas reformas de fin de semana.");
            drill.setCategory("Herramientas");
            drill.setPricePerDay(new BigDecimal("8.50"));
            drill.setCity("Madrid");
            drill.setMunicipality("Madrid");
            drill.setImageUrl("https://images.unsplash.com/photo-1504148455328-c376907d081c");
            drill.setCreatedAt(LocalDateTime.now().minusDays(8));
            drill.setUpdatedAt(LocalDateTime.now().minusDays(1));
            drill = itemRepository.save(drill);

            Item camera = new Item();
            camera.setOwner(owner);
            camera.setTitle("Cámara GoPro");
            camera.setDescription("Cámara de acción 4K con carcasa protectora, soporte para casco y tarjeta de memoria incluida.");
            camera.setCategory("Electrónica");
            camera.setPricePerDay(new BigDecimal("15.00"));
            camera.setCity("Madrid");
            camera.setMunicipality("Madrid");
            camera.setImageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32");
            camera.setCreatedAt(LocalDateTime.now().minusDays(6));
            camera.setUpdatedAt(LocalDateTime.now().minusDays(2));
            camera = itemRepository.save(camera);

            Item projector = seedItem(owner2, "Proyector Epson Full HD",
                    "Proyector Full HD con HDMI, mando y altavoz integrado. Perfecto para cine en casa, clases o presentaciones.",
                    "Electrónica", "22.00", "Valencia/València", "València",
                    "https://images.unsplash.com/photo-1601944179066-29786cb9d32a", true, 9);
            Item bike = seedItem(owner2, "Bicicleta urbana",
                    "Bicicleta ligera con candado, luces y cesta delantera. Revisada y lista para moverse por ciudad.",
                    "Deporte", "12.00", "Barcelona", "Barcelona",
                    "https://images.unsplash.com/photo-1485965120184-e220f721d03e", true, 5);
            Item sound = seedItem(owner, "Altavoz JBL PartyBox",
                    "Altavoz potente con Bluetooth, luces y batería. Buena opción para eventos pequeños.",
                    "Audio", "18.00", "Sevilla", "Sevilla",
                    "https://images.unsplash.com/photo-1545454675-3531b543be5d", true, 4);
            Item tent = seedItem(owner2, "Tienda de campaña 4 personas",
                    "Tienda impermeable para cuatro personas, fácil de montar y con doble techo.",
                    "Camping", "10.00", "Madrid", "Madrid",
                    "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4", true, 12);
            Item stroller = seedItem(owner, "Carrito de bebé plegable",
                    "Carrito ligero y plegable, ideal para visitas familiares o viajes cortos.",
                    "Infantil", "9.00", "Málaga", "Málaga",
                    "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1", false, 2);

            // Reservas, pagos, resenas y notificaciones permiten probar todos los paneles sin crear datos a mano.
            Reservation reservation = new Reservation();
            reservation.setItem(drill);
            reservation.setRenter(renter);
            reservation.setStartDate(LocalDate.now().plusDays(2));
            reservation.setEndDate(LocalDate.now().plusDays(4));
            reservation.setStatus(ReservationStatus.PENDING);
            reservation.setPlatformFeePercent(new BigDecimal("10.00"));
            reservation.setTotalPrice(new BigDecimal("18.70"));
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            Reservation accepted = seedReservation(camera, renter, LocalDate.now().plusDays(6), LocalDate.now().plusDays(8), ReservationStatus.ACCEPTED, "33.00", 3);
            Reservation completed = seedReservation(projector, renter, LocalDate.now().minusDays(12), LocalDate.now().minusDays(10), ReservationStatus.COMPLETED, "48.40", 12);
            Reservation canceled = seedReservation(bike, renter2, LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), ReservationStatus.CANCELED, "26.40", 7);
            Reservation rejected = seedReservation(sound, renter, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), ReservationStatus.REJECTED, "39.60", 1);
            Reservation ownerPending = seedReservation(tent, renter2, LocalDate.now().plusDays(3), LocalDate.now().plusDays(6), ReservationStatus.PENDING, "33.00", 1);

            seedPayment(accepted, renter, owner, "33.00", PaymentStatus.CAPTURED, "PAY-DEMO-ACCEPTED");
            seedPayment(completed, renter, owner2, "48.40", PaymentStatus.CAPTURED, "PAY-DEMO-COMPLETED");
            seedPayment(canceled, renter2, owner2, "26.40", PaymentStatus.REFUNDED, "PAY-DEMO-REFUNDED");

            seedReview(completed, projector, renter, 5, "Muy buena experiencia. El proyector estaba perfecto y Marta fue muy puntual.");

            seedNotification(owner, NotificationType.RESERVATION_CREATED, "Luis ha solicitado reservar tu Taladro Bosch.", false, 1);
            seedNotification(owner, NotificationType.PAYMENT_CAPTURED, "Pago capturado por la reserva de la Cámara GoPro.", true, 3);
            seedNotification(renter, NotificationType.RESERVATION_ACCEPTED, "Ana ha aceptado tu reserva de la Cámara GoPro.", false, 2);
            seedNotification(renter, NotificationType.RESERVATION_COMPLETED, "Tu reserva del Proyector Epson Full HD se ha completado.", true, 9);
            seedNotification(owner2, NotificationType.RESERVATION_CREATED, "Carlos quiere reservar tu Tienda de campaña.", false, 1);
            seedNotification(owner2, NotificationType.REVIEW_CREATED, "Luis ha publicado una reseña sobre el Proyector Epson Full HD.", false, 8);
            seedNotification(admin, NotificationType.ADMIN_ALERT, "Datos de demostración cargados correctamente.", false, 0);
        };
    }

    private Item seedItem(User owner, String title, String description, String category, String price, String city,
                          String municipality, String imageUrl, boolean active, int createdDaysAgo) {
        Item item = new Item();
        item.setOwner(owner);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setPricePerDay(new BigDecimal(price));
        item.setCity(city);
        item.setMunicipality(municipality);
        item.setImageUrl(imageUrl);
        item.setActive(active);
        item.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        item.setUpdatedAt(LocalDateTime.now().minusDays(Math.max(1, createdDaysAgo / 2)));
        return itemRepository.save(item);
    }

    private Reservation seedReservation(Item item, User renter, LocalDate start, LocalDate end,
                                        ReservationStatus status, String totalPrice, int createdDaysAgo) {
        Reservation reservation = new Reservation();
        reservation.setItem(item);
        reservation.setRenter(renter);
        reservation.setStartDate(start);
        reservation.setEndDate(end);
        reservation.setStatus(status);
        reservation.setPlatformFeePercent(new BigDecimal("10.00"));
        reservation.setTotalPrice(new BigDecimal(totalPrice));
        reservation.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        reservation.setUpdatedAt(LocalDateTime.now().minusDays(Math.max(0, createdDaysAgo - 1)));
        return reservationRepository.save(reservation);
    }

    private void seedPayment(Reservation reservation, User payer, User receiver, String amount,
                             PaymentStatus status, String referenceCode) {
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPayer(payer);
        payment.setReceiver(receiver);
        payment.setAmount(new BigDecimal(amount));
        payment.setStatus(status);
        payment.setMethod("SIMULATED_CARD");
        payment.setReferenceCode(referenceCode);
        payment.setCreatedAt(reservation.getUpdatedAt());
        payment.setUpdatedAt(LocalDateTime.now().minusDays(1));
        paymentRepository.save(payment);
    }

    private void seedNotification(User user, NotificationType type, String message, boolean read, int createdDaysAgo) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        notificationRepository.save(notification);
    }

    private void seedReview(Reservation reservation, Item item, User author, int rating, String comment) {
        Review review = new Review();
        review.setReservation(reservation);
        review.setItem(item);
        review.setAuthor(author);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now().minusDays(8));
        reviewRepository.save(review);
    }
}
