package jp.co.metateam.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_manage")
public class RentalManage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate expectedRentalOn;

    private LocalDate expectedReturnOn;

    private String employeeId;

    private String stockId;

    private Integer status;
}