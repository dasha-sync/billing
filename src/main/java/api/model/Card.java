package api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "cards")
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // ID карты
    private String paymentMethodId;

    @Column
    private String brand; // Visa, MasterCard

    @Column
    private String last4; // последние 4 цифры карты

    @Column
    private Long expMonth;

    @Column
    private Long expYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
