package org.example.shared.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name="Department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "departmentId", length = 36, columnDefinition = "VARCHAR(36)")
    private UUID departmentId;
    private String departmentName;
    private int numOfProcessedRequest;
    private int maxConcurrentRequestInDay;
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "currentRequestId", length = 36, columnDefinition = "VARCHAR(36)")
    private UUID currentRequestId;

    public Department() {
    }

    public Department(String departmentName, int numOfProcessedRequest, int maxConcurrentRequestInDay) {
        this.departmentName = departmentName;
        this.numOfProcessedRequest = numOfProcessedRequest;
        this.maxConcurrentRequestInDay = maxConcurrentRequestInDay;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getNumOfProcessedRequest() {
        return numOfProcessedRequest;
    }

    public void setNumOfProcessedRequest(int numOfProcessedRequest) {
        this.numOfProcessedRequest = numOfProcessedRequest;
    }

    public int getMaxConcurrentRequestInDay() {
        return maxConcurrentRequestInDay;
    }

    public void setMaxConcurrentRequestInDay(int maxConcurrentRequestInDay) {
        this.maxConcurrentRequestInDay = maxConcurrentRequestInDay;
    }

    public UUID getCurrentRequestId() {
        return currentRequestId;
    }

    public void setCurrentRequestId(UUID currentRequestId) {
        this.currentRequestId = currentRequestId;
    }
}
