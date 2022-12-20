package de.ossi.pojo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Getter
@Setter
public class Employee {
    private String firstname;
    private String lastname;
    private String setProperty;
    private String city;
    private Integer id;
    private Long technical_id;
    private LocalDateTime startTime;
    private LocalDate startDay;
    private double salary;
    private Float salaryf;
    private boolean active;
    private Employee supervisor;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String noPrefix;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String differentPrefix;

    public String holeDifferentPrefix() {
        return differentPrefix;
    }

    public void setzeDifferentPrefix(String differentPrefix) {
        this.differentPrefix = differentPrefix;
    }

    public String noPrefix() {
        return noPrefix;
    }

    public void noPrefix(String noPrefix) {
        this.noPrefix = noPrefix;
    }
}
