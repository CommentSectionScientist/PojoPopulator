package de.ossi.pojo;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class Employee {
    private String firstname;
    private String lastname;
    private String setProperty;
    private String city;
    private Integer id;
    private LocalDateTime startTime;
    private double salary;
    private boolean active;
    private String noPrefix;
    private String differentPrefix;
    private Employee supervisor;

    public Employee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Employee supervisor) {
        this.supervisor = supervisor;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSetProperty() {
        return setProperty;
    }

    public void setSetProperty(String setProperty) {
        this.setProperty = setProperty;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}
